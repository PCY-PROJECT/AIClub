package com.aicode.module.auth.service.impl;

import com.aicode.common.cache.InMemoryCacheService;
import com.aicode.common.exception.BizException;
import com.aicode.common.util.JwtUtil;
import com.aicode.module.auth.dto.*;
import com.aicode.module.auth.service.AuthService;
import com.aicode.module.point.service.PointService;
import com.aicode.module.user.entity.User;
import com.aicode.module.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final InMemoryCacheService cache;
    private final JavaMailSender mailSender;
    private final PointService pointService;

    @Value("${app.jwt.expiration}")
    private Long jwtExpiration;

    @Value("${spring.mail.username}")
    private String mailFrom;

    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
            "mailinator.com", "10minutemail.com", "tempmail.com",
            "guerrillamail.com", "throwam.com", "yopmail.com",
            "sharklasers.com", "spam4.me", "trashmail.com",
            "guerrillamail.info", "grr.la", "guerrillamailblock.com"
    );

    // ─── 公开接口 ─────────────────────────────────────────────

    @Override
    public CaptchaVO getCaptcha() {
        String captchaId = UUID.randomUUID().toString();
        String code = randomCode(4);
        cache.set("captcha:" + captchaId, code.toLowerCase(), Duration.ofMinutes(5));
        return new CaptchaVO(captchaId, buildCaptchaImage(code));
    }

    @Override
    public void sendEmailCode(SendEmailCodeRequest req, String clientIp) {
        verifyCaptcha(req.getCaptchaId(), req.getCaptchaCode());
        String email = req.getEmail().toLowerCase();
        checkEmailDomain(email);

        String limitKey = "email_code_cooldown:" + email;
        if (cache.hasKey(limitKey)) {
            throw new BizException(429, "验证码已发送，请60秒后重试");
        }

        String code = String.format("%06d", new Random().nextInt(1_000_000));
        cache.set("email:code:" + email, code, Duration.ofMinutes(5));
        cache.set(limitKey, "1", Duration.ofSeconds(60));

        sendMail(email, code);
    }

    @Override
    @Transactional
    public void register(RegisterRequest req, String clientIp) {
        verifyCaptcha(req.getCaptchaId(), req.getCaptchaCode());
        String email = req.getEmail().toLowerCase();
        checkEmailDomain(email);

        // IP 注册频率限制（同IP 24h 内 ≤3次）
        String ipKey = "reg_ip:" + clientIp;
        String ipCount = cache.get(ipKey);
        if (ipCount != null && Integer.parseInt(ipCount) >= 3) {
            throw new BizException(429, "今日注册次数已达上限，请明日再试");
        }

        // 检查邮箱是否已注册
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) != null) {
            throw new BizException(400, "邮箱已被注册");
        }

        // 解析邀请人
        Long inviterId = resolveInviter(req.getInviteCode());

        // 创建用户
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(email.split("@")[0]);
        user.setPoints(0);
        user.setStatus(1);
        user.setInviteCode(genUserInviteCode());
        user.setInviterId(inviterId);
        user.setRegIp(clientIp);
        userMapper.insert(user);

        // 注册赠送 100 积分
        pointService.addPoints(user.getId(), 100, 1, null, "新用户注册奖励");

        // 更新 IP 计数
        if (ipCount == null) {
            cache.set(ipKey, "1", Duration.ofHours(24));
        } else {
            cache.increment(ipKey);
        }

    }

    @Override
    public LoginVO login(LoginRequest req) {
        verifyCaptcha(req.getCaptchaId(), req.getCaptchaCode());
        String email = req.getEmail().toLowerCase();

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BizException(400, "邮箱或密码错误");
        }
        if (user.getStatus() == 2) {
            throw new BizException(403, "账号已被封禁，请联系管理员");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new LoginVO(token, jwtExpiration, user.getId(), user.getNickname(), user.getPoints());
    }

    // ─── 私有方法 ─────────────────────────────────────────────

    private void verifyCaptcha(String captchaId, String captchaCode) {
        String key = "captcha:" + captchaId;
        String stored = cache.get(key);
        if (stored == null || !stored.equalsIgnoreCase(captchaCode)) {
            throw new BizException(400, "图形验证码错误或已过期");
        }
        cache.delete(key); // 一次性使用
    }

    private void checkEmailDomain(String email) {
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
        if (DISPOSABLE_DOMAINS.contains(domain)) {
            throw new BizException(400, "不支持此邮箱域名，请使用常规邮箱注册");
        }
    }

    private Long resolveInviter(String inviteCode) {
        if (inviteCode == null || inviteCode.isBlank()) return null;
        User inviter = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getInviteCode, inviteCode.toUpperCase()));
        return inviter != null ? inviter.getId() : null;
    }

    private String genUserInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        String code;
        do {
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
            code = sb.toString();
        } while (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getInviteCode, code)));
        return code;
    }

    private String randomCode(int len) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 去掉易混淆字符
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < len; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    private String buildCaptchaImage(String code) {
        int w = 120, h = 40;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 背景
        g.setColor(new Color(245, 245, 250));
        g.fillRect(0, 0, w, h);

        // 干扰线
        Random rnd = new Random();
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(rnd.nextInt(150) + 80, rnd.nextInt(150) + 80, rnd.nextInt(150) + 80));
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine(rnd.nextInt(w), rnd.nextInt(h), rnd.nextInt(w), rnd.nextInt(h));
        }

        // 字符
        g.setFont(new Font("Arial", Font.BOLD, 24));
        int x = 8;
        for (char c : code.toCharArray()) {
            g.setColor(new Color(rnd.nextInt(100), rnd.nextInt(80) + 40, rnd.nextInt(140) + 60));
            g.drawString(String.valueOf(c), x, 30 + (rnd.nextInt(6) - 3));
            x += 27;
        }

        // 干扰点
        for (int i = 0; i < 50; i++) {
            g.setColor(new Color(rnd.nextInt(200) + 55, rnd.nextInt(200) + 55, rnd.nextInt(200) + 55));
            g.fillOval(rnd.nextInt(w), rnd.nextInt(h), 2, 2);
        }

        g.dispose();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }

    private void sendMail(String to, String code) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject("AI邀请码 - 邮箱验证码");
            helper.setText(
                    "<html><body style='font-family:Arial,sans-serif;'>" +
                    "<h3>您好！</h3>" +
                    "<p>您正在注册 <b>AI邀请码导航</b>，邮箱验证码为：</p>" +
                    "<h2 style='color:#4f46e5;letter-spacing:6px;'>" + code + "</h2>" +
                    "<p style='color:#888;'>验证码5分钟内有效，请勿泄露给他人。</p>" +
                    "</body></html>", true);
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage());
            throw new BizException("邮件发送失败，请稍后重试");
        }
    }
}
