package com.aicode.module.code.service.impl;

import com.aicode.common.exception.BizException;
import com.aicode.common.util.AesUtil;
import com.aicode.common.util.CodePreviewUtil;
import com.aicode.module.code.dto.ContributeCodeRequest;
import com.aicode.module.code.entity.InviteCode;
import com.aicode.module.code.mapper.InviteCodeMapper;
import com.aicode.module.code.service.InviteCodeService;
import com.aicode.module.code.vo.ClaimedCodeVO;
import com.aicode.module.code.vo.CodeItemVO;
import com.aicode.module.point.service.PointService;
import com.aicode.module.product.entity.Product;
import com.aicode.module.product.mapper.ProductMapper;
import com.aicode.module.user.entity.User;
import com.aicode.module.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InviteCodeServiceImpl implements InviteCodeService {

    private final InviteCodeMapper inviteCodeMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final PointService pointService;
    private final AesUtil aesUtil;

    // 积分常量
    private static final int CLAIM_COST         = 50;  // 获取者花费
    private static final int PROVIDER_REWARD     = 30;  // 提供者确认有效后奖励
    private static final int PROVIDER_PENALTY    = 60;  // 提供者确认无效罚款
    private static final int CLAIM_REFUND        = 50;  // 获取者无效退款

    // 积分流水 biz_type
    private static final int BIZ_CLAIM           = 10;  // 获取者 -50
    private static final int BIZ_PROVIDER_REWARD = 5;   // 提供者 +30
    private static final int BIZ_PROVIDER_PENALTY= 6;   // 提供者 -60
    private static final int BIZ_CLAIM_REFUND    = 11;  // 获取者退款 +50

    // ─── 贡献邀请码 ────────────────────────────────────────────

    @Override
    @Transactional
    public void contributeCode(Long userId, ContributeCodeRequest req) {
        Product product = productMapper.selectById(req.getProductId());
        if (product == null || product.getStatus() != 1) {
            throw new BizException(404, "产品不存在");
        }

        String rawCode = req.getCode().trim();

        InviteCode inviteCode = new InviteCode();
        inviteCode.setProductId(req.getProductId());
        inviteCode.setProviderId(userId);
        inviteCode.setCodeEncrypted(aesUtil.encrypt(rawCode));
        inviteCode.setCodePreview(CodePreviewUtil.generate(rawCode));
        inviteCode.setStatus(5);  // 待审核
        inviteCodeMapper.insert(inviteCode);
    }

    // ─── 从池中获取邀请码（行锁保证并发安全） ────────────────────

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ClaimedCodeVO claimCode(Long userId, Long productId) {
        // 1. 检查用户是否已有该产品待确认的邀请码
        InviteCode existing = inviteCodeMapper.selectOne(
                new LambdaQueryWrapper<InviteCode>()
                        .eq(InviteCode::getClaimantId, userId)
                        .eq(InviteCode::getProductId, productId)
                        .eq(InviteCode::getStatus, 2)
        );
        if (existing != null) {
            return buildClaimedVO(existing, productId);
        }

        // 2. 快速检查积分（不加锁，用于友好提示）
        User viewer = userMapper.selectById(userId);
        if (viewer == null) throw new BizException(404, "用户不存在");
        if (viewer.getPoints() < CLAIM_COST) {
            throw new BizException(400, "积分不足，获取邀请码需要 " + CLAIM_COST + " 积分");
        }

        // 3. 从池中锁定一条可用邀请码（SELECT FOR UPDATE）
        InviteCode code = inviteCodeMapper.selectForClaimForUpdate(productId, userId);
        if (code == null) {
            throw new BizException(400, "暂无可用邀请码，请稍后再试");
        }

        // 4. 原子扣减获取者积分（SQL层兜底防止并发超扣）
        // pointService.addPoints 内部调用 userMapper.addPoints，返回0则抛BizException
        pointService.addPoints(userId, -CLAIM_COST, BIZ_CLAIM,
                String.valueOf(code.getId()), "获取邀请码");

        // 5. 更新邀请码状态为已分配
        LocalDateTime now = LocalDateTime.now();
        inviteCodeMapper.update(null,
                new LambdaUpdateWrapper<InviteCode>()
                        .eq(InviteCode::getId, code.getId())
                        .set(InviteCode::getStatus, 2)
                        .set(InviteCode::getClaimantId, userId)
                        .set(InviteCode::getClaimTime, now)
                        .set(InviteCode::getConfirmDeadline, now.plusHours(12)));

        // 6. 更新产品可用数量
        productMapper.update(null,
                new LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, productId)
                        .setSql("code_count = GREATEST(code_count - 1, 0)"));

        // 7. 返回VO（包含明文）
        code.setStatus(2);
        code.setClaimantId(userId);
        code.setClaimTime(now);
        code.setConfirmDeadline(now.plusHours(12));
        return buildClaimedVO(code, productId);
    }

    // ─── 确认有效性 ────────────────────────────────────────────

    @Override
    @Transactional
    public void confirmCode(Long userId, Long codeId, String result) {
        InviteCode code = inviteCodeMapper.selectForUpdate(codeId);
        if (code == null) throw new BizException(404, "邀请码不存在");
        if (!userId.equals(code.getClaimantId())) throw new BizException(403, "无权操作");
        if (code.getStatus() != 2) throw new BizException(400, "该邀请码当前状态无法确认");

        LocalDateTime now = LocalDateTime.now();

        if ("valid".equals(result)) {
            doConfirmValid(code, now);
        } else if ("invalid".equals(result)) {
            doConfirmInvalid(code, now);
        } else {
            throw new BizException(400, "result 参数有误，请传 valid 或 invalid");
        }
    }

    // ─── 我贡献的 ─────────────────────────────────────────────

    @Override
    public Page<CodeItemVO> myContributed(Long userId, int pageNum, int pageSize) {
        Page<InviteCode> page = inviteCodeMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<InviteCode>()
                        .eq(InviteCode::getProviderId, userId)
                        .orderByDesc(InviteCode::getCreateTime)
        );

        Page<CodeItemVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(code -> {
            CodeItemVO vo = new CodeItemVO();
            vo.setId(code.getId());
            vo.setProductId(code.getProductId());
            vo.setCodePreview(code.getCodePreview());
            vo.setStatus(code.getStatus());
            vo.setCreateTime(code.getCreateTime());
            vo.setClaimTime(code.getClaimTime());
            vo.setConfirmDeadline(code.getConfirmDeadline());
            vo.setConfirmResult(code.getConfirmResult());
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    // ─── 我获取的 ─────────────────────────────────────────────

    @Override
    public Page<ClaimedCodeVO> myClaimed(Long userId, int pageNum, int pageSize) {
        Page<InviteCode> page = inviteCodeMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<InviteCode>()
                        .eq(InviteCode::getClaimantId, userId)
                        .orderByDesc(InviteCode::getClaimTime)
        );

        if (page.getRecords().isEmpty()) {
            return new Page<>(pageNum, pageSize, 0);
        }

        // 批量查产品名
        List<Long> pids = page.getRecords().stream()
                .map(InviteCode::getProductId).distinct().collect(Collectors.toList());
        Map<Long, String> productNames = productMapper.selectBatchIds(pids)
                .stream().collect(Collectors.toMap(Product::getId, Product::getName));

        Page<ClaimedCodeVO> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(code -> {
            ClaimedCodeVO vo = new ClaimedCodeVO();
            vo.setCodeId(code.getId());
            vo.setProductId(code.getProductId());
            vo.setProductName(productNames.get(code.getProductId()));
            // 只在待确认或已确认状态下返回明文
            try {
                vo.setCodeText(aesUtil.decrypt(code.getCodeEncrypted()));
            } catch (Exception e) {
                vo.setCodeText("解密失败");
            }
            vo.setStatus(code.getStatus());
            vo.setClaimTime(code.getClaimTime());
            vo.setConfirmDeadline(code.getConfirmDeadline());
            vo.setConfirmTime(code.getConfirmTime());
            vo.setConfirmResult(code.getConfirmResult());
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    // ─── 定时任务：处理超时未确认 ─────────────────────────────────

    @Override
    @Transactional
    public void processExpiredConfirmations() {
        // 查找 status=2 且 confirm_deadline < now
        List<InviteCode> expired = inviteCodeMapper.selectList(
                new LambdaQueryWrapper<InviteCode>()
                        .eq(InviteCode::getStatus, 2)
                        .lt(InviteCode::getConfirmDeadline, LocalDateTime.now())
        );

        for (InviteCode code : expired) {
            try {
                doConfirmValid(code, LocalDateTime.now());
                log.info("自动确认有效 codeId={}", code.getId());
            } catch (Exception e) {
                log.error("自动确认失败 codeId={}, error={}", code.getId(), e.getMessage());
            }
        }
    }

    // ─── 私有方法 ─────────────────────────────────────────────

    /** 确认有效逻辑（复用于用户主动确认和超时自动确认） */
    private void doConfirmValid(InviteCode code, LocalDateTime now) {
        inviteCodeMapper.update(null,
                new LambdaUpdateWrapper<InviteCode>()
                        .eq(InviteCode::getId, code.getId())
                        .set(InviteCode::getStatus, 3)
                        .set(InviteCode::getConfirmTime, now)
                        .set(InviteCode::getConfirmResult, 1));

        // 奖励提供者 +30
        pointService.addPoints(code.getProviderId(), PROVIDER_REWARD, BIZ_PROVIDER_REWARD,
                String.valueOf(code.getId()), "邀请码确认有效");
    }

    /** 确认无效逻辑 */
    private void doConfirmInvalid(InviteCode code, LocalDateTime now) {
        inviteCodeMapper.update(null,
                new LambdaUpdateWrapper<InviteCode>()
                        .eq(InviteCode::getId, code.getId())
                        .set(InviteCode::getStatus, 4)
                        .set(InviteCode::getConfirmTime, now)
                        .set(InviteCode::getConfirmResult, 2));

        // 惩罚提供者 -60
        pointService.addPoints(code.getProviderId(), -PROVIDER_PENALTY, BIZ_PROVIDER_PENALTY,
                String.valueOf(code.getId()), "邀请码被判定无效");

        // 退款获取者 +50
        pointService.addPoints(code.getClaimantId(), CLAIM_REFUND, BIZ_CLAIM_REFUND,
                String.valueOf(code.getId()), "邀请码无效退款");
    }

    private ClaimedCodeVO buildClaimedVO(InviteCode code, Long productId) {
        ClaimedCodeVO vo = new ClaimedCodeVO();
        vo.setCodeId(code.getId());
        vo.setProductId(productId);
        try {
            vo.setCodeText(aesUtil.decrypt(code.getCodeEncrypted()));
        } catch (Exception e) {
            vo.setCodeText("解密失败");
        }
        vo.setStatus(code.getStatus());
        vo.setClaimTime(code.getClaimTime());
        vo.setConfirmDeadline(code.getConfirmDeadline());
        vo.setConfirmTime(code.getConfirmTime());
        vo.setConfirmResult(code.getConfirmResult());
        return vo;
    }
}
