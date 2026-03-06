-- ============================================================
-- AI邀请码导航 - 数据库初始化脚本
-- ============================================================
CREATE DATABASE IF NOT EXISTS ai_invitecode DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_invitecode;

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    email        VARCHAR(128) NOT NULL UNIQUE          COMMENT '邮箱（登录账号）',
    password     VARCHAR(256) NOT NULL                 COMMENT 'BCrypt 加密密码',
    nickname     VARCHAR(64)                           COMMENT '昵称',
    avatar       VARCHAR(512)                          COMMENT '头像 URL',
    points       INT          NOT NULL DEFAULT 0       COMMENT '当前积分余额',
    status       TINYINT      NOT NULL DEFAULT 1       COMMENT '1正常 2封禁',
    invite_code  VARCHAR(32)  UNIQUE                   COMMENT '用户推广邀请码',
    inviter_id   BIGINT                                COMMENT '邀请人用户 ID',
    reg_ip       VARCHAR(64)                           COMMENT '注册 IP',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_invite_code (invite_code)
) COMMENT '用户表';

-- 积分流水表
CREATE TABLE IF NOT EXISTS t_point_record (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    delta       INT          NOT NULL                  COMMENT '积分变化（正增负减）',
    balance     INT          NOT NULL                  COMMENT '变动后余额快照',
    biz_type    TINYINT      NOT NULL                  COMMENT '1注册 2签到 3查看邀请码 4邀请码被查看 5邀请用户 6任务',
    biz_id      VARCHAR(64)                            COMMENT '关联业务 ID',
    remark      VARCHAR(256)                           COMMENT '备注描述',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) COMMENT '积分流水表';

-- AI产品表
CREATE TABLE IF NOT EXISTS t_product (
    id           BIGINT        PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(128)  NOT NULL               COMMENT '产品名称',
    logo         VARCHAR(512)                         COMMENT 'Logo URL',
    description  VARCHAR(1024)                        COMMENT '产品简介',
    official_url VARCHAR(512)                         COMMENT '官网地址',
    category     TINYINT       NOT NULL               COMMENT '1对话 2绘图 3代码 4视频 5音乐 9其他',
    status       TINYINT       NOT NULL DEFAULT 1     COMMENT '1上线 2下线',
    sort         INT           NOT NULL DEFAULT 0     COMMENT '排序权重（越大越靠前）',
    code_count   INT           NOT NULL DEFAULT 0     COMMENT '当前可用邀请码数量',
    create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_status (category, status, sort)
) COMMENT 'AI 产品表';

-- 邀请码池表
-- status: 1=池中可用 2=已分配(待确认) 3=已确认有效 4=已确认无效 5=待审核 6=审核拒绝
CREATE TABLE IF NOT EXISTS t_invite_code (
    id               BIGINT       PRIMARY KEY AUTO_INCREMENT,
    product_id       BIGINT       NOT NULL               COMMENT '所属产品 ID',
    provider_id      BIGINT       NOT NULL               COMMENT '提供者用户 ID',
    parent_code_id   BIGINT                              COMMENT '保留兼容字段，不再使用',
    code_encrypted   VARCHAR(512) NOT NULL               COMMENT 'AES-256 加密后的邀请码内容',
    code_preview     VARCHAR(32)  NOT NULL               COMMENT '预览文本（如：ab******）',
    status           TINYINT      NOT NULL DEFAULT 5     COMMENT '1=可用 2=已分配 3=有效 4=无效 5=待审核 6=已拒绝',
    viewer_id        BIGINT                              COMMENT '兼容旧字段（已废弃）',
    view_time        DATETIME                            COMMENT '兼容旧字段（已废弃）',
    claimant_id      BIGINT                              COMMENT '获取者用户 ID',
    claim_time       DATETIME                            COMMENT '获取时间',
    confirm_deadline DATETIME                            COMMENT '确认截止时间（获取后12小时）',
    confirm_time     DATETIME                            COMMENT '实际确认时间',
    confirm_result   TINYINT                             COMMENT '确认结果 1=有效 2=无效',
    reviewer_id      BIGINT                              COMMENT '审核管理员 ID',
    review_time      DATETIME                            COMMENT '审核时间',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product_status (product_id, status, create_time),
    INDEX idx_provider_id (provider_id),
    INDEX idx_claimant_id (claimant_id),
    INDEX idx_status_deadline (status, confirm_deadline)
) COMMENT '邀请码池表';

-- 存量数据迁移脚本（如需在已有数据库上运行）：
-- ALTER TABLE t_invite_code
--   ADD COLUMN claimant_id      BIGINT   COMMENT '获取者用户ID',
--   ADD COLUMN claim_time       DATETIME COMMENT '获取时间',
--   ADD COLUMN confirm_deadline DATETIME COMMENT '确认截止时间（获取后12小时）',
--   ADD COLUMN confirm_time     DATETIME COMMENT '实际确认时间',
--   ADD COLUMN confirm_result   TINYINT  COMMENT '确认结果 1=有效 2=无效',
--   ADD COLUMN reviewer_id      BIGINT   COMMENT '审核管理员ID',
--   ADD COLUMN review_time      DATETIME COMMENT '审核时间';
-- -- status重映射：原1(可用)→5(待审核初始状态)，实际已有可用数据保持1即可
-- UPDATE t_invite_code SET status = CASE WHEN status=2 THEN 3 ELSE status END WHERE 1=1;

-- 任务配置表
CREATE TABLE IF NOT EXISTS t_task (
    id            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    name          VARCHAR(128) NOT NULL               COMMENT '任务名称',
    task_type     VARCHAR(64)  NOT NULL UNIQUE        COMMENT '任务类型标识',
    frequency     TINYINT      NOT NULL               COMMENT '1每日 2每周 3一次性',
    points_reward INT          NOT NULL               COMMENT '完成奖励积分',
    status        TINYINT      NOT NULL DEFAULT 1     COMMENT '1启用 2停用',
    sort          INT          NOT NULL DEFAULT 0,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT '任务配置表';

-- 任务完成记录
CREATE TABLE IF NOT EXISTS t_task_record (
    id          BIGINT      PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL,
    task_id     BIGINT      NOT NULL,
    task_type   VARCHAR(64) NOT NULL,
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_task_date (user_id, task_type, create_time)
) COMMENT '任务完成记录';

-- ─── 初始数据 ───────────────────────────────────────────

-- 任务数据
INSERT IGNORE INTO t_task (id, name, task_type, frequency, points_reward, status, sort) VALUES
(1, '每日签到',           'DAILY_CHECKIN',       1, 10, 1, 1),
(2, '首次提交邀请码',     'FIRST_SUBMIT_CODE',   3, 20, 1, 2),
(3, '累计提交5个邀请码',  'SUBMIT_CODE_5_TIMES', 3, 50, 1, 3),
(4, '邀请好友注册',       'INVITE_USER',         2, 50, 1, 4);

-- 示例 AI 产品数据
INSERT IGNORE INTO t_product (id, name, description, official_url, category, status, sort, code_count) VALUES
(1,  'ChatGPT',         'OpenAI 出品的对话式 AI 助手，支持写作、编程、问答等',       'https://chat.openai.com',      1, 1, 100, 0),
(2,  'Claude',          'Anthropic 出品的 AI 助手，擅长分析与长文本处理',           'https://claude.ai',            1, 1, 95,  0),
(3,  'Gemini',          'Google 出品的多模态 AI 助手',                            'https://gemini.google.com',    1, 1, 90,  0),
(4,  'Midjourney',      '强大的 AI 绘图工具，生成高质量艺术图像',                    'https://midjourney.com',       2, 1, 88,  0),
(5,  'Stable Diffusion','开源 AI 绘图模型，可本地部署使用',                         'https://stability.ai',         2, 1, 75,  0),
(6,  'GitHub Copilot',  'GitHub 出品的 AI 代码助手，智能补全与生成代码',             'https://github.com/features/copilot', 3, 1, 92, 0),
(7,  'Cursor',          'AI 驱动的代码编辑器，深度集成 Claude 代码生成能力',         'https://cursor.sh',            3, 1, 88,  0),
(8,  'Sora',            'OpenAI 文本生成视频模型，高质量 AI 视频创作',               'https://openai.com/sora',      4, 1, 80,  0),
(9,  'Suno',            'AI 音乐创作工具，输入文本自动生成完整歌曲',                  'https://suno.ai',              5, 1, 75,  0),
(10, 'Perplexity',      'AI 搜索引擎，实时联网回答问题并标注来源',                   'https://perplexity.ai',        1, 1, 85,  0);

-- OpenClaw 生态产品（product_group=1）
INSERT IGNORE INTO t_product (id, name, description, official_url, category, status, sort, code_count, product_group) VALUES
(11, 'OpenClaw',  '开源自托管 AI Agent 框架，支持 Telegram/Discord 多渠道交互，5490+ 社区技能插件，月均费用仅 -35，GitHub 264k Stars。', 'https://github.com/openclaw/openclaw', 1, 1, 99, 0, 1),
(12, 'ClawHub',   'OpenClaw 官方技能市场，收录 5490+ 社区开发技能，覆盖编程、DevOps、营销、智能家居等 31 个分类。',                       'https://clawhub.com',                 9, 1, 98, 0, 1),
(13, 'ClawDBot',  'OpenClaw 的企业级 AI Agent 实现，强化了垂直整合能力，适合在 Slack/企业微信等平台部署私有 AI 助手。',                  'https://clawhub.com/clawdbot',        1, 1, 97, 0, 1);

-- ============================================================
-- v2.0 新增表
-- ============================================================

-- 资讯表
CREATE TABLE IF NOT EXISTS t_news (
    id           BIGINT        PRIMARY KEY AUTO_INCREMENT,
    title        VARCHAR(256)  NOT NULL                   COMMENT '标题',
    summary      VARCHAR(512)                             COMMENT '摘要（100字内）',
    cover        VARCHAR(512)                             COMMENT '封面图 URL',
    source_url   VARCHAR(512)                             COMMENT '来源链接',
    tags         VARCHAR(256)                             COMMENT '标签，逗号分隔',
    category     TINYINT       NOT NULL DEFAULT 1         COMMENT '1产品发布 2行业动态 3技术突破 4使用技巧 5政策监管',
    author_id    BIGINT                                   COMMENT '投稿用户ID，NULL=编辑发布',
    status       TINYINT       NOT NULL DEFAULT 2         COMMENT '1待审核 2已发布 3已拒绝',
    like_count   INT           NOT NULL DEFAULT 0,
    view_count   INT           NOT NULL DEFAULT 0,
    publish_time DATETIME,
    create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status_category (status, category, publish_time)
) COMMENT '资讯表';

-- 资讯点赞记录
CREATE TABLE IF NOT EXISTS t_news_like (
    id          BIGINT    PRIMARY KEY AUTO_INCREMENT,
    news_id     BIGINT    NOT NULL,
    user_id     BIGINT    NOT NULL,
    create_time DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_news_user (news_id, user_id),
    INDEX idx_user_id (user_id)
) COMMENT '资讯点赞记录';

-- 技能/提示词表
CREATE TABLE IF NOT EXISTS t_skill (
    id            BIGINT        PRIMARY KEY AUTO_INCREMENT,
    title         VARCHAR(256)  NOT NULL,
    `prompt`      TEXT          NOT NULL,
    applicable    VARCHAR(256),
    category      VARCHAR(64),
    tags          VARCHAR(256),
    author_id     BIGINT        NOT NULL,
    status        TINYINT       NOT NULL DEFAULT 1,
    like_count    INT           NOT NULL DEFAULT 0,
    collect_count INT           NOT NULL DEFAULT 0,
    use_count     INT           NOT NULL DEFAULT 0,
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status_category (status, category, collect_count)
) COMMENT '技能/提示词表';

-- 技能点赞记录
CREATE TABLE IF NOT EXISTS t_skill_like (
    id          BIGINT    PRIMARY KEY AUTO_INCREMENT,
    skill_id    BIGINT    NOT NULL,
    user_id     BIGINT    NOT NULL,
    create_time DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_skill_user (skill_id, user_id),
    INDEX idx_user_id (user_id)
) COMMENT '技能点赞记录';

-- 技能收藏记录
CREATE TABLE IF NOT EXISTS t_skill_collect (
    id          BIGINT    PRIMARY KEY AUTO_INCREMENT,
    skill_id    BIGINT    NOT NULL,
    user_id     BIGINT    NOT NULL,
    create_time DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_skill_user (skill_id, user_id),
    INDEX idx_user_id (user_id)
) COMMENT '技能收藏记录';

-- 示例资讯数据
INSERT IGNORE INTO t_news (id, title, summary, source_url, tags, category, author_id, status, like_count, view_count, publish_time) VALUES
(1, 'GPT-4o 迎来重大更新，多模态能力全面升级', 'OpenAI 发布 GPT-4o 最新版本，图像理解、代码生成和实时语音能力均有显著提升，API 调用成本降低 50%。',
 'https://openai.com/blog', 'GPT-4o,OpenAI,多模态', 1, NULL, 2, 12, 340, NOW() - INTERVAL 1 DAY),
(2, 'Claude 3.7 Sonnet 发布，推理能力达到新高度', 'Anthropic 正式推出 Claude 3.7 Sonnet，在数学推理、代码编写和复杂分析方面超越前代，支持 200K token 超长上下文。',
 'https://anthropic.com/blog', 'Claude,Anthropic,推理', 1, NULL, 2, 8, 210, NOW() - INTERVAL 2 DAY),
(3, '国产 AI 崛起：DeepSeek-R2 开源发布', '深度求索发布 DeepSeek-R2 开源大模型，在多项基准测试中达到国际顶尖水平，完全免费可商用，引发全球 AI 社区广泛关注。',
 'https://deepseek.com', 'DeepSeek,开源,国内AI', 1, NULL, 2, 45, 1200, NOW() - INTERVAL 3 DAY),
(4, 'Midjourney v7 开放内测，画质提升令人惊叹', 'Midjourney 最新版本 v7 开放部分用户内测，采用全新架构，生成图像细节、光影和人体结构均有大幅改善，支持更精准的风格控制。',
 'https://midjourney.com', 'Midjourney,绘图AI,v7', 3, NULL, 2, 31, 890, NOW() - INTERVAL 4 DAY),
(5, 'AI 编程工具对比：Cursor vs GitHub Copilot 2026年最新评测', '本文对两款主流 AI 编程助手进行深度横评，从代码补全、错误修复、多文件编辑等维度全面对比，附实际使用场景下的速度测试数据。',
 'https://example.com/ai-coding-tools', '编程AI,Cursor,Copilot,评测', 4, NULL, 2, 56, 2100, NOW() - INTERVAL 5 DAY);

-- 示例技能数据（author_id 使用子查询，确保用户存在才插入）
INSERT IGNORE INTO t_skill (id, title, `prompt`, applicable, category, tags, author_id, status, like_count, collect_count, use_count)
SELECT 1, '爆款小红书文案生成器',
'你是一名擅长小红书内容运营的文案专家。请根据我提供的主题，写一篇爆款小红书笔记，要求：\n1. 标题使用数字+emoji吸引眼球\n2. 开头3行必须钩住读者，引发共鸣或好奇心\n3. 正文分点展开，每点附上具体细节和个人感受\n4. 结尾引导互动\n5. 配上10个精准话题标签\n主题：[填入你的主题]',
'ChatGPT,Claude,Gemini', '营销', '小红书,文案,爆款', id, 2, 89, 234, 3200
FROM t_user WHERE email='admin@test.com' LIMIT 1;

INSERT IGNORE INTO t_skill (id, title, `prompt`, applicable, category, tags, author_id, status, like_count, collect_count, use_count)
SELECT 2, '代码 Review 专家',
'你是一名资深软件工程师，请对下面的代码进行专业Review，从以下维度给出反馈：\n1. 正确性\n2. 性能\n3. 可读性\n4. 安全性\n5. 最佳实践\n请给出修改后的代码示例并解释改动原因。\n代码：\n```\n[粘贴代码]\n```',
'ChatGPT,Claude,Cursor', '编程', '代码审查,代码质量', id, 2, 56, 178, 2800
FROM t_user WHERE email='admin@test.com' LIMIT 1;

INSERT IGNORE INTO t_skill (id, title, `prompt`, applicable, category, tags, author_id, status, like_count, collect_count, use_count)
SELECT 3, '英中互译专业润色',
'你是一名拥有15年经验的专业翻译。请翻译以下内容，准确传达原文含义，符合目标语言表达习惯，并给出更地道的意译版本。\n原文：[填入要翻译的内容]',
'ChatGPT,Claude,DeepSeek', '翻译', '翻译,润色,双语', id, 2, 34, 112, 1900
FROM t_user WHERE email='test@test.com' LIMIT 1;

INSERT IGNORE INTO t_skill (id, title, `prompt`, applicable, category, tags, author_id, status, like_count, collect_count, use_count)
SELECT 4, '职场邮件万能模板',
'你是一名职场沟通专家。请根据以下信息帮我撰写一封专业职场邮件：\n- 邮件类型：[如申请加薪/拒绝需求/跟进项目]\n- 收件人：[姓名及职位]\n- 核心诉求：[一句话描述]\n要求主题行简洁有力，正文逻辑清晰，结尾明确下一步行动。',
'ChatGPT,Claude,Gemini', '职场', '职场,邮件,沟通', id, 2, 45, 156, 2400
FROM t_user WHERE email='admin@test.com' LIMIT 1;

INSERT IGNORE INTO t_skill (id, title, `prompt`, applicable, category, tags, author_id, status, like_count, collect_count, use_count)
SELECT 5, '数据分析报告一键生成',
'你是一名资深数据分析师。请根据我提供的数据生成一份分析报告，包含：执行摘要、数据概览、深度洞察（3条）、问题诊断、行动建议（3条）。\n数据：[粘贴数据或描述数据情况]',
'ChatGPT,Claude,Gemini', '分析', '数据分析,报告,商业分析', id, 2, 23, 89, 1500
FROM t_user WHERE email='test@test.com' LIMIT 1;

INSERT IGNORE INTO t_skill (id, title, `prompt`, applicable, category, tags, author_id, status, like_count, collect_count, use_count)
SELECT 6, '苏格拉底式辩论导师',
'你现在是苏格拉底。你不会直接给出答案，而是通过引导性问题帮助我自己找到答案。每次只提1-2个问题，当我的思路出现矛盾时温和地指出并引导反思。\n今天我想探讨：[填入你想深入思考的问题]',
'ChatGPT,Claude', '教育', '哲学,苏格拉底,深度思考', id, 2, 67, 198, 3100
FROM t_user WHERE email='test@test.com' LIMIT 1;

-- ============================================================
-- v3.0 新增表 & 字段
-- ============================================================

-- t_user 新增字段
ALTER TABLE t_user
    ADD COLUMN IF NOT EXISTS role    TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1普通用户 2编辑员 9超级管理员',
    ADD COLUMN IF NOT EXISTS user_no INT UNSIGNED UNIQUE COMMENT '注册序号（早期用户身份依据，首个注册=1）';

-- t_product 新增字段
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS product_group TINYINT NOT NULL DEFAULT 2 COMMENT '1=OpenClaw生态 2=AI应用 3=其他';

-- 知识库表（t_resource）
-- 定位：策展导航，只存标题+摘要+来源链接，不存全文
CREATE TABLE IF NOT EXISTS t_resource (
    id              BIGINT        PRIMARY KEY AUTO_INCREMENT,
    title           VARCHAR(512)  NOT NULL                     COMMENT '标题',
    summary         VARCHAR(1024)                              COMMENT '摘要（150字内）',
    source_url      VARCHAR(1024) NOT NULL                     COMMENT '来源原文链接（核心字段）',
    cover           VARCHAR(512)                               COMMENT '封面图 URL',
    author          VARCHAR(128)                               COMMENT '原文作者/来源媒体',
    category        TINYINT       NOT NULL DEFAULT 1           COMMENT '1大模型 2Agent框架 3MCP 4教程 5工具评测 6行业洞察',
    difficulty      TINYINT       NOT NULL DEFAULT 1           COMMENT '1入门 2进阶 3专业',
    tags            VARCHAR(256)                               COMMENT '标签，逗号分隔',
    status          TINYINT       NOT NULL DEFAULT 2           COMMENT '1待审核 2已发布 3已拒绝',
    view_count      INT           NOT NULL DEFAULT 0,
    collect_count   INT           NOT NULL DEFAULT 0,
    is_auto_fetched TINYINT       NOT NULL DEFAULT 0           COMMENT '0手动录入 1自动抓取',
    submit_user_id  BIGINT                                     COMMENT '投稿用户ID，NULL=编辑录入',
    publish_time    DATETIME,
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status_category (status, category, publish_time),
    INDEX idx_difficulty (difficulty),
    INDEX idx_submit_user (submit_user_id)
) COMMENT '知识库表（策展导航，跳转原文）';

-- 知识库收藏记录
CREATE TABLE IF NOT EXISTS t_resource_collect (
    id           BIGINT    PRIMARY KEY AUTO_INCREMENT,
    resource_id  BIGINT    NOT NULL,
    user_id      BIGINT    NOT NULL,
    create_time  DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_resource_user (resource_id, user_id),
    INDEX idx_user_id (user_id)
) COMMENT '知识库收藏记录';

-- Agent技能库表（t_mcp_skill）
-- 包含 MCP Server 目录 和 Agent Skill 目录
CREATE TABLE IF NOT EXISTS t_mcp_skill (
    id             BIGINT        PRIMARY KEY AUTO_INCREMENT,
    name           VARCHAR(256)  NOT NULL                     COMMENT '名称',
    description    VARCHAR(2048) NOT NULL                     COMMENT '功能描述（面向普通用户语言）',
    type           TINYINT       NOT NULL DEFAULT 1           COMMENT '1 MCP Server  2 Agent Skill',
    category       VARCHAR(64)                                COMMENT '分类（如 文件/代码/浏览器/通讯/数据库）',
    source_url     VARCHAR(1024)                              COMMENT 'GitHub/官网链接',
    install_guide  TEXT                                       COMMENT '安装/使用指南（Markdown）',
    vendor         VARCHAR(256)                               COMMENT '开发者/组织',
    tags           VARCHAR(256)                               COMMENT '标签，逗号分隔',
    status         TINYINT       NOT NULL DEFAULT 2           COMMENT '1待审核 2已上架 3已下架',
    collect_count  INT           NOT NULL DEFAULT 0,
    use_count      INT           NOT NULL DEFAULT 0,
    sort           INT           NOT NULL DEFAULT 0           COMMENT '排序权重',
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_type_status (type, status, sort),
    INDEX idx_category (category)
) COMMENT 'Agent技能库（MCP Server + Agent Skill 目录）';

-- 内容自动抓取源配置
CREATE TABLE IF NOT EXISTS t_fetch_source (
    id              BIGINT        PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(128)  NOT NULL                    COMMENT '来源名称',
    url             VARCHAR(1024) NOT NULL                    COMMENT 'RSS/Atom 订阅地址',
    feed_type       TINYINT       NOT NULL DEFAULT 1          COMMENT '1 RSS  2 Atom',
    category_hint   TINYINT                                   COMMENT '默认映射分类（对应 t_resource.category）',
    status          TINYINT       NOT NULL DEFAULT 1          COMMENT '1启用 2停用',
    last_fetched_at DATETIME                                  COMMENT '上次成功抓取时间',
    fetch_count     INT           NOT NULL DEFAULT 0          COMMENT '累计抓取条目数',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT '内容自动抓取源配置';

-- ─── v3.0 初始数据 ──────────────────────────────────────────

-- 新增任务：Onboarding 一次性任务
INSERT IGNORE INTO t_task (id, name, task_type, frequency, points_reward, status, sort) VALUES
(5,  '完善个人昵称',     'SET_NICKNAME',        3, 10, 1, 10),
(6,  '首次发表评论',     'FIRST_COMMENT',       3, 15, 1, 11),
(7,  '首次分享内容',     'FIRST_SHARE',         3, 20, 1, 12),
(8,  '首次提交提示词',   'FIRST_SUBMIT_PROMPT', 3, 20, 1, 13),
(9,  '邀请第一位好友',   'INVITE_FIRST_USER',   3, 50, 1, 14),
(10, '连续签到7天',      'CHECKIN_7_STREAK',    3, 30, 1, 20),
(11, '累计邀请5位好友',  'INVITE_5_USERS',      3, 100,1, 21);

-- 示例知识库数据
INSERT IGNORE INTO t_resource (id, title, summary, source_url, author, category, difficulty, tags, status, view_count, collect_count, publish_time) VALUES
(1, '大模型入门：从 Transformer 到 ChatGPT 的演进之路',
 '本文用最通俗的语言解释大语言模型的核心原理，从 Attention 机制到 RLHF，适合完全没有 AI 背景的读者入门。',
 'https://example.com/llm-intro', '机器之心', 1, 1, '大模型,入门,Transformer', 2, 1240, 89, NOW() - INTERVAL 3 DAY),
(2, 'MCP 协议完全指南：让 Claude 连接你的本地工具',
 'Model Context Protocol (MCP) 是 Anthropic 提出的开放标准，让 AI 助手可以安全地访问本地文件、数据库和 API。本文手把手教你配置第一个 MCP Server。',
 'https://example.com/mcp-guide', 'Anthropic Blog', 3, 2, 'MCP,Claude,工具连接', 2, 3400, 256, NOW() - INTERVAL 5 DAY),
(3, 'Agent 框架横评 2026：LangChain vs AutoGen vs OpenClaw',
 '深度对比三大主流 Agent 框架的架构设计、易用性和生态成熟度，附真实项目接入成本评估。',
 'https://example.com/agent-comparison', 'AI产品评测', 2, 2, 'Agent,LangChain,AutoGen,OpenClaw', 2, 5600, 412, NOW() - INTERVAL 7 DAY),
(4, '用 AI 工具每天节省 2 小时：10 个真实工作流程',
 '整理了 10 个职场人士高频使用的 AI 工作流，包括会议纪要、周报生成、邮件回复、数据分析，每个附有实操截图。',
 'https://example.com/ai-workflow', '效率工具研究所', 4, 1, '工作流,效率,提示词', 2, 8900, 678, NOW() - INTERVAL 10 DAY),
(5, 'OpenClaw 上手教程：用 WhatsApp 控制你的 Claude',
 'OpenClaw 是最受欢迎的个人 AI 助手开源框架，通过 MCP 将 Claude 连接到本地系统，并支持 WhatsApp/Telegram 作为对话入口。',
 'https://openclaw.ai/docs/quickstart', 'OpenClaw 官方', 3, 2, 'OpenClaw,Claude,MCP,Agent', 2, 2300, 198, NOW() - INTERVAL 2 DAY);

-- ─── openclaw101.dev 知识库扩充数据（v3.1）────────────────────────────────────
-- 来源：https://openclaw101.dev/zh  共收录374+篇教程资源的中文社区导航站
-- 7天入门教程系列
INSERT IGNORE INTO t_resource (id, title, summary, source_url, author, category, difficulty, tags, status, view_count, collect_count, publish_time) VALUES
(6,  'Day 1：初识 OpenClaw —— 开源私人 AI 助手入门',
 'OpenClaw 是开源自托管个人 AI 助手框架，支持 Telegram/Discord 多渠道、5490+ 社区技能插件、本地完全隐私，月均费用仅 $8-35，7天教程首篇带你全面了解。',
 'https://openclaw101.dev/zh/day/1', 'OpenClaw 101', 2, 1, 'OpenClaw,Agent,入门,AI助手', 2, 3800, 312, NOW() - INTERVAL 14 DAY),

(7,  'Day 2：10 分钟搭建你的专属 AI 助手',
 '一行命令自动安装 OpenClaw，支持云服务器/Mac Mini/本地电脑三种部署方式，配合 Claude API Key 和 Telegram Bot Token 即可完成首次对话，"比泡方便面还简单"。',
 'https://openclaw101.dev/zh/day/2', 'OpenClaw 101', 4, 1, 'OpenClaw,部署,Telegram,Claude', 2, 5200, 428, NOW() - INTERVAL 13 DAY),

(8,  'Day 3：给助手一个灵魂 —— SOUL.md / USER.md / AGENTS.md 配置指南',
 '通过编写 SOUL.md（性格准则）、USER.md（主人档案）、AGENTS.md（工作规范）三个灵魂文件，将通用 AI 变成真正了解你的个人助手，配置可持续迭代优化。',
 'https://openclaw101.dev/zh/day/3', 'OpenClaw 101', 4, 1, 'OpenClaw,个性化,配置,AI助手', 2, 4100, 356, NOW() - INTERVAL 12 DAY),

(9,  'Day 4：接入你的数字生活 —— Gmail / 日历 / 搜索 / 浏览器集成',
 '为 AI 助手安装 Gmail、Google Calendar、Brave Search、Playwright 浏览器四大核心技能，通过 OAuth 授权实现邮件读取、日程管理、联网搜索和网页操作，助手从聊天工具升级为实用助手。',
 'https://openclaw101.dev/zh/day/4', 'OpenClaw 101', 4, 2, 'OpenClaw,Gmail,技能,自动化,浏览器', 2, 3600, 298, NOW() - INTERVAL 11 DAY),

(10, 'Day 5：解锁技能树 —— OpenClaw 技能系统完全指南',
 'OpenClaw 技能相当于 AI 助手的 App Store，每个技能由 SKILL.md+配置文件+脚本组成。精选10个核心技能推荐，并介绍多技能组合实现"1+1>2"自动化工作流的最佳实践。',
 'https://openclaw101.dev/zh/day/5', 'OpenClaw 101', 4, 2, 'OpenClaw,技能,工作流,自动化', 2, 3200, 267, NOW() - INTERVAL 10 DAY),

(11, 'Day 6：让助手主动工作 —— 心跳机制与 Cron 自动化',
 '介绍 OpenClaw 心跳机制（每30分钟主动唤醒）和 Cron 定时任务，配合三层记忆系统（每日笔记/长期记忆/灵魂记忆），实现晨报推送、邮件监控、数据告警等主动式 AI 管家能力。',
 'https://openclaw101.dev/zh/day/6', 'OpenClaw 101', 4, 2, 'OpenClaw,自动化,Cron,心跳,记忆', 2, 2900, 241, NOW() - INTERVAL 9 DAY),

(12, 'Day 7：进阶玩法与未来展望 —— 自定义技能开发与多节点协作',
 '7天课程收官篇：自定义技能包开发方法、多设备节点跨平台协作（手机/电脑/树莓派）、完整安全清单，以及 AI 助手多模态交互与 Agent 协作网络的未来展望。',
 'https://openclaw101.dev/zh/day/7', 'OpenClaw 101', 4, 3, 'OpenClaw,进阶,自定义,多节点,安全', 2, 2700, 218, NOW() - INTERVAL 8 DAY),

-- 云平台部署教程
(13, '阿里云一键部署 OpenClaw AI 助手完整教程',
 '阿里云官方出品的 OpenClaw 轻量应用服务器快速部署指南，含环境配置、安全组规则、域名绑定全流程，适合国内用户低延迟自托管 AI 助手。',
 'https://help.aliyun.com/zh/simple-application-server/use-cases/quickly-deploy-and-use-openclaw', '阿里云', 4, 1, 'OpenClaw,阿里云,部署,云服务器', 2, 4500, 389, NOW() - INTERVAL 20 DAY),

(14, '腾讯云部署 OpenClaw 实战教程',
 '腾讯云开发者社区出品的 OpenClaw 部署教程，从云服务器选购到 OpenClaw 安装配置，再到 Telegram 机器人绑定，国内用户友好，含常见报错解决方案。',
 'https://cloud.tencent.com/developer/article/2625073', '腾讯云开发者', 4, 1, 'OpenClaw,腾讯云,部署,Telegram', 2, 3800, 312, NOW() - INTERVAL 25 DAY),

(15, 'DigitalOcean 部署 OpenClaw 官方教程（英文）',
 'DigitalOcean 官方社区教程，使用 Droplet 一键部署 OpenClaw，适合海外服务器低延迟访问 Claude API，含 Docker 配置和防火墙设置。',
 'https://www.digitalocean.com/community/tutorials/how-to-run-openclaw', 'DigitalOcean', 4, 2, 'OpenClaw,DigitalOcean,部署,Docker', 2, 2100, 167, NOW() - INTERVAL 30 DAY),

-- 中文社区资源
(16, '菜鸟教程：OpenClaw/ClawDBot 新手完全指南',
 '面向零基础用户的 OpenClaw 图文教程，覆盖安装、基础配置、常用命令、FAQ，语言通俗易懂，国内访问速度快，适合第一次接触 AI 助手的读者。',
 'https://www.runoob.com/ai-agent/openclaw-clawdbot-tutorial.html', '菜鸟教程', 4, 1, 'OpenClaw,入门,新手,中文教程', 2, 6200, 541, NOW() - INTERVAL 18 DAY),

(17, '知乎：我用 OpenClaw 搭了个私人 AI 助手，体验分享',
 '知乎作者真实使用体验分享，包括搭建过程踩坑记录、与 ChatGPT/Claude 直接使用的对比、适合人群分析，评论区有大量用户互动讨论，干货满满。',
 'https://zhuanlan.zhihu.com/p/2000850539936765122', '知乎专栏', 4, 1, 'OpenClaw,使用体验,AI助手,测评', 2, 7800, 623, NOW() - INTERVAL 15 DAY),

(18, '36氪：OpenClaw 凭什么成为 GitHub 最热 AI 项目',
 '36氪深度报道 OpenClaw 在 GitHub 获得 264k Star 的原因，分析开源私人 AI 助手赛道崛起背景、与商业 AI 产品的差异化竞争，以及国内用户使用现状。',
 'https://36kr.com/p/3671941309260675', '36氪', 6, 1, 'OpenClaw,开源,AI助手,行业分析', 2, 5400, 432, NOW() - INTERVAL 22 DAY),

-- 行业洞察与安全研究
(19, '安全警告：研究人员发现 341 个恶意 ClawHub 技能包',
 'The Hacker News 报道：安全研究人员在 ClawHub 技能市场发现341个恶意技能，可窃取用户数据和 API 密钥。文章详解攻击原理及防护措施，安装第三方技能前务必审查源码。',
 'https://thehackernews.com/2026/02/researchers-find-341-malicious-clawhub.html', 'The Hacker News', 6, 3, 'OpenClaw,安全,ClawHub,恶意软件,风险', 2, 8900, 712, NOW() - INTERVAL 35 DAY),

(20, 'IBM Think：ClawDBot —— 测试 AI Agent 垂直整合极限',
 'IBM 技术博客深度分析 ClawDBot（OpenClaw 的一个实现变种）在垂直整合场景下的能力边界，探讨 AI Agent 在企业级工作流中的实际表现与局限性。',
 'https://www.ibm.com/think/news/clawdbot-ai-agent-testing-limits-vertical-integration', 'IBM Think', 6, 3, 'Agent,IBM,企业AI,垂直整合,ClawDBot', 2, 3200, 256, NOW() - INTERVAL 40 DAY),

(21, 'Cisco 安全博客：个人 AI Agent 是安全噩梦？',
 'Cisco 安全团队从企业安全视角分析 OpenClaw 等个人 AI Agent 的安全隐患：本地文件访问、API 密钥暴露、网络权限滥用等风险，并提供企业网络环境下的防护建议。',
 'https://blogs.cisco.com/ai/personal-ai-agents-like-openclaw-are-a-security-nightmare', 'Cisco Blogs', 6, 3, 'OpenClaw,安全,企业,AI Agent,风险评估', 2, 4100, 318, NOW() - INTERVAL 45 DAY),

(22, 'The Verge：OpenClaw —— 让每个人都能拥有私人 AI 管家',
 'The Verge 科技媒体对 OpenClaw 的深度报道，探讨开源私人 AI 助手如何挑战 ChatGPT 等商业产品，分析其用户群体特征及隐私优势，并访谈核心开发者。',
 'https://www.theverge.com/news/872091/openclaw-moltbot-clawdbot-ai-agent-news', 'The Verge', 6, 1, 'OpenClaw,AI助手,隐私,开源,媒体报道', 2, 5600, 445, NOW() - INTERVAL 28 DAY),

-- Agent 框架资源
(23, 'OpenClaw 官方 GitHub 仓库（264k Stars）',
 'OpenClaw 开源 AI Agent 框架官方代码仓库，包含完整源码、安装脚本、技能开发文档和 API 文档。全球最受欢迎的个人 AI 助手框架之一，社区活跃，持续迭代。',
 'https://github.com/openclaw/openclaw', 'OpenClaw 开源社区', 2, 2, 'OpenClaw,GitHub,开源,Agent框架', 2, 9200, 876, NOW() - INTERVAL 60 DAY),

(24, 'ClawHub 技能市场 —— 5490+ 社区技能一站获取',
 'OpenClaw 官方技能市场，收录5490+社区开发技能，覆盖网页前端、编程、DevOps、搜索研究、营销、AI大模型、智能家居等31个分类，安装前请务必检查源码安全性。',
 'https://clawhub.com', 'ClawHub', 2, 1, 'OpenClaw,技能市场,ClawHub,插件', 2, 6800, 534, NOW() - INTERVAL 55 DAY),

(25, '飞书机器人接入 OpenClaw 完整教程',
 '将 OpenClaw AI 助手接入飞书（Lark）的详细配置教程，适合国内企业用户在飞书生态内使用私人 AI 助手，含飞书应用创建、Webhook 配置、消息路由全流程。',
 'https://my.feishu.cn/wiki/YkWgwqSchi9xW3kEuZscAm0lnFf', '飞书知识库', 4, 2, 'OpenClaw,飞书,Lark,企业,机器人', 2, 2800, 231, NOW() - INTERVAL 16 DAY);

-- 示例 Agent 技能库数据
INSERT IGNORE INTO t_mcp_skill (id, name, description, type, category, source_url, vendor, tags, status, collect_count, use_count, sort) VALUES
(1, 'Filesystem MCP',
 '让 AI 助手可以读写你电脑上的文件和文件夹。安全可控——你可以指定 AI 只能访问哪些目录，非常适合整理文档和代码项目。',
 1, '文件管理', 'https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem',
 'Anthropic', 'MCP,文件,本地', 2, 234, 1800, 100),
(2, 'Browser Use MCP',
 '赋予 AI 助手真实浏览器操作能力：自动填表、截图、抓取网页数据，就像让 AI 帮你用电脑一样。',
 1, '浏览器', 'https://github.com/browser-use/browser-use',
 'browser-use', 'MCP,浏览器,自动化', 2, 456, 3200, 95),
(3, 'GitHub MCP',
 '通过 AI 助手直接管理你的 GitHub 仓库：创建 Issue、提交 PR、查看代码改动，开发者效率神器。',
 1, '代码开发', 'https://github.com/modelcontextprotocol/servers/tree/main/src/github',
 'Anthropic', 'MCP,GitHub,代码', 2, 312, 2600, 90),
(4, 'Slack MCP',
 '连接你的 Slack 工作区，让 AI 帮你搜索历史消息、发送通知、汇总频道内容，再也不用手动刷消息了。',
 1, '通讯协作', 'https://github.com/modelcontextprotocol/servers/tree/main/src/slack',
 'Anthropic', 'MCP,Slack,协作', 2, 189, 1400, 85),
(5, '网页自动研究 Agent',
 '给定研究主题，自动搜索多个网页、提取关键信息、去重整合，最终生成一份结构化的研究报告。适合竞品调研、行业分析等场景。',
 2, '研究分析', 'https://github.com/assafelovic/gpt-researcher',
 'Assaf Elovic', 'Agent,研究,自动化', 2, 567, 4100, 88);

-- ─── openclaw101.dev 精选技能扩充数据（v3.1）─────────────────────────────────
-- 来源：https://openclaw101.dev/zh/day/5  OpenClaw 官方推荐精选技能
INSERT IGNORE INTO t_mcp_skill (id, name, description, type, category, source_url, install_guide, vendor, tags, status, collect_count, use_count, sort) VALUES

(6, 'remind-me',
 '将对话中提到的任何事项自动转化为准时提醒。只需告诉 AI 助手"明天下午3点提醒我开会"，它会在正确时间主动推送通知，再也不怕忘事。',
 2, '效率工具', 'https://clawhub.com/skills/remind-me',
 '## 安装方式\n\n```bash\nnpx clawhub@latest install remind-me\n```\n\n## 使用示例\n\n- "提醒我明天早上9点吃药"\n- "每周一早上发我本周任务清单"\n- "30分钟后提醒我去开会"\n\n## 说明\n\n基于 OpenClaw 心跳机制，支持一次性和周期性提醒，Telegram 推送。',
 'OpenClaw 社区', 'OpenClaw,提醒,定时,效率', 2, 389, 4200, 98),

(7, 'todo-tracker',
 '智能待办清单管理技能。在对话中随口说出的任务会被自动收集，AI 助手会追踪完成进度并定期汇报，让你的任务一件都不漏。',
 2, '效率工具', 'https://clawhub.com/skills/todo-tracker',
 '## 安装方式\n\n```bash\nnpx clawhub@latest install todo-tracker\n```\n\n## 使用示例\n\n- "帮我记下：写季度报告"\n- "今天有哪些待办？"\n- "把写报告标记为完成"\n\n## 说明\n\n支持优先级、截止日期、分类标签，可与 remind-me 技能联动。',
 'OpenClaw 社区', 'OpenClaw,待办,任务管理,效率', 2, 312, 3800, 96),

(8, 'imap-email',
 '连接你的邮箱（支持 Gmail / Outlook / 任意 IMAP 邮箱），让 AI 助手自动监控新邮件、提炼摘要、识别重要信息，并协助起草回复，彻底解放邮件处理时间。',
 2, '通讯协作', 'https://clawhub.com/skills/imap-email',
 '## 安装方式\n\n```bash\nnpx clawhub@latest install imap-email\n```\n\n## 配置\n\n在 `.env` 中设置：\n```\nIMAP_HOST=imap.gmail.com\nIMAP_USER=your@email.com\nIMAP_PASS=your-app-password\n```\n\n## 使用示例\n\n- "总结今天的未读邮件"\n- "帮我回复上面那封邮件"\n- "有没有关于项目的重要邮件？"',
 'OpenClaw 社区', 'OpenClaw,邮件,Gmail,IMAP,自动化', 2, 456, 5100, 94),

(9, 'web-search',
 '为 AI 助手接入实时联网搜索能力（基于 Brave Search / Tavily），让助手能获取最新资讯、查询实时数据，回答不再局限于训练数据截止日期。',
 2, '搜索研究', 'https://clawhub.com/skills/web-search',
 '## 安装方式\n\n```bash\nnpx clawhub@latest install web-search\n```\n\n## 配置\n\n```\nBRAVE_SEARCH_API_KEY=your-api-key\n```\n获取免费 API Key：https://brave.com/search/api/\n\n## 使用示例\n\n- "搜索今天 A 股市场情况"\n- "最新的 Claude 4 有哪些功能？"\n- "帮我找三篇关于 RAG 的最新论文"',
 'OpenClaw 社区', 'OpenClaw,搜索,Brave,联网,实时', 2, 534, 6200, 92),

(10, 'browser',
 '基于 Playwright 的浏览器自动化技能，赋予 AI 助手真实打开网页、点击、填表、截图的能力。爬取竞品价格、自动填写表单、网页内容抓取，一句话搞定。',
 2, '浏览器', 'https://clawhub.com/skills/browser',
 '## 安装方式\n\n```bash\nnpx clawhub@latest install browser\n# 首次使用需安装浏览器驱动\nnpx playwright install chromium\n```\n\n## 使用示例\n\n- "打开京东搜索 AirPods Pro 价格"\n- "截图 example.com 的首页"\n- "帮我登录 XX 网站并下载最新报告"\n\n## 注意\n\n需要服务器有图形环境或使用 headless 模式。',
 'OpenClaw 社区', 'OpenClaw,浏览器,Playwright,自动化,爬虫', 2, 678, 7800, 90),

(11, 'weather-nws',
 '实时天气查询技能，支持全球城市天气、未来7天预报、极端天气预警。可配合心跳机制实现每日自动推送天气早报，出行前再也不用单独打开天气 APP。',
 2, '生活助手', 'https://clawhub.com/skills/weather-nws',
 '## 安装方式\n\n```bash\nnpx clawhub@latest install weather-nws\n```\n\n## 使用示例\n\n- "今天北京天气怎么样？"\n- "本周末上海会下雨吗？"\n- "设置每天早上7点推送天气"\n\n## 说明\n\n免费使用，无需 API Key，数据来源 NWS（美国国家气象局），全球城市均支持。',
 'OpenClaw 社区', 'OpenClaw,天气,天气预报,生活', 2, 234, 2900, 85),

(12, 'newsletter-digest',
 '订阅内容摘要技能，自动抓取你关注的 Newsletter、RSS 源、YouTube 频道最新内容，提炼核心要点推送给你，告别信息过载，每天10分钟掌握行业动态。',
 2, '内容聚合', 'https://clawhub.com/skills/newsletter-digest',
 '## 安装方式\n\n```bash\nnpx clawhub@latest install newsletter-digest\n```\n\n## 配置\n\n在技能配置文件中添加订阅源：\n```yaml\nsources:\n  - type: rss\n    url: https://example.com/feed\n  - type: youtube\n    channel: UCxxxxxxx\n```\n\n## 使用示例\n\n- "给我今天的科技资讯摘要"\n- "最近有没有 AI 相关的好文章？"',
 'OpenClaw 社区', 'OpenClaw,RSS,摘要,内容聚合,Newsletter', 2, 289, 3400, 82),

(13, 'pdf-parser',
 '文档解析技能（基于 markitdown），支持 PDF、Word、PPT、Excel 等多种格式，将文档转为结构化 Markdown，让 AI 助手能直接分析、总结、问答文档内容。',
 2, '文件管理', 'https://clawhub.com/skills/pdf-parser',
 '## 安装方式\n\n```bash\nnpx clawhub@latest install pdf-parser\n```\n\n## 使用示例\n\n- "帮我总结这份 PDF 合同的关键条款"\n- "把这个 PPT 转成文字版"\n- "这份报告里有没有提到竞品价格？"\n\n## 支持格式\n\nPDF、DOCX、PPTX、XLSX、HTML、CSV、图片（OCR）',
 'OpenClaw 社区', 'OpenClaw,PDF,文档,解析,markitdown', 2, 198, 2600, 80),

(14, 'obsidian',
 '连接你的 Obsidian 知识库，让 AI 助手读写笔记、跨文档检索、自动整理日记，打通 AI 与个人知识管理系统，让你的"第二大脑"真正智能起来。',
 2, '笔记知识', 'https://clawhub.com/skills/obsidian',
 '## 安装方式\n\n```bash\nnpx clawhub@latest install obsidian\n```\n\n## 配置\n\n```\nOBSIDIAN_VAULT_PATH=/path/to/your/vault\n```\n\n## 使用示例\n\n- "在我的 Obsidian 里搜索关于 MCP 的笔记"\n- "帮我创建今天的日记"\n- "把这段内容整理成笔记保存到知识库"',
 'OpenClaw 社区', 'OpenClaw,Obsidian,笔记,知识管理,PKM', 2, 312, 3100, 78),

(15, 'homeassistant',
 '接入 Home Assistant 智能家居平台，让 AI 助手控制灯光、空调、门锁等智能设备，查询家庭能耗，设置自动化场景，用自然语言管理你的全屋智能。',
 2, '智能家居', 'https://clawhub.com/skills/homeassistant',
 '## 安装方式\n\n```bash\nnpx clawhub@latest install homeassistant\n```\n\n## 配置\n\n```\nHA_URL=http://your-home-assistant:8123\nHA_TOKEN=your-long-lived-access-token\n```\n\n## 使用示例\n\n- "把客厅灯调到50%亮度"\n- "今天家里用了多少电？"\n- "我要出门了，帮我关闭所有灯和空调"',
 'OpenClaw 社区', 'OpenClaw,智能家居,HomeAssistant,IoT,自动化', 2, 267, 2800, 75),

(16, 'coding-agent',
 '专业编程 Agent 技能，具备读写代码文件、运行终端命令、调试报错的能力。描述需求后，AI 自动完成从需求分析到代码实现的全流程，支持多语言多框架。',
 2, '代码开发', 'https://clawhub.com/skills/coding-agent',
 '## 安装方式\n\n```bash\nnpx clawhub@latest install coding-agent\n```\n\n## 使用示例\n\n- "帮我写一个 Python 脚本，批量重命名文件夹里的图片"\n- "这段代码有什么 bug？帮我修复"\n- "用 Vue3 写一个带搜索功能的列表组件"\n\n## 说明\n\n需要授权文件系统访问权限，建议配合 Filesystem MCP 使用。',
 'OpenClaw 社区', 'OpenClaw,编程,代码,Agent,开发', 2, 445, 5600, 88),

(17, 'seo-audit',
 'SEO 分析与优化技能，自动检测网站 SEO 问题：标题、描述、关键词密度、链接结构、页面速度等，生成可执行的优化建议报告，适合网站运营和内容创作者。',
 2, '营销分析', 'https://clawhub.com/skills/seo-audit',
 '## 安装方式\n\n```bash\nnpx clawhub@latest install seo-audit\n```\n\n## 使用示例\n\n- "分析 example.com 的 SEO 问题"\n- "这篇文章的关键词布局合理吗？"\n- "帮我生成一份 SEO 优化报告"\n\n## 说明\n\n配合 browser 技能效果更佳，可实时抓取页面数据进行分析。',
 'OpenClaw 社区', 'OpenClaw,SEO,营销,网站优化,分析', 2, 189, 2200, 72),

(18, 'discord',
 '连接 Discord 服务器，让 AI 助手监控频道消息、自动回复、发送通知、汇总社区动态。适合社区运营者和需要 Discord 信息聚合的用户。',
 2, '通讯协作', 'https://clawhub.com/skills/discord',
 '## 安装方式\n\n```bash\nnpx clawhub@latest install discord\n```\n\n## 配置\n\n```\nDISCORD_TOKEN=your-bot-token\nDISCORD_GUILD_ID=your-server-id\n```\n\n## 使用示例\n\n- "总结今天 #general 频道的重要讨论"\n- "在 #announcements 发送一条公告"\n- "有没有人在讨论产品 bug？"',
 'OpenClaw 社区', 'OpenClaw,Discord,社区,通讯,机器人', 2, 156, 1800, 68),

(19, 'kimi-integration',
 '集成月之暗面 Kimi AI 大模型，为 OpenClaw 助手提供超长上下文（支持百万 token）处理能力，适合分析超长文档、代码库、研究报告等大体量内容场景。',
 2, 'AI大模型', 'https://clawhub.com/skills/kimi-integration',
 '## 安装方式\n\n```bash\nnpx clawhub@latest install kimi-integration\n```\n\n## 配置\n\n```\nKIMI_API_KEY=your-moonshot-api-key\n```\n获取 API Key：https://platform.moonshot.cn/\n\n## 使用示例\n\n- "用 Kimi 分析这个 500 页的 PDF 报告"\n- "把整个代码仓库传给 Kimi 帮我梳理架构"\n- "这篇超长论文的核心观点是什么？"',
 'OpenClaw 社区', 'OpenClaw,Kimi,月之暗面,大模型,长上下文', 2, 223, 2700, 70);
