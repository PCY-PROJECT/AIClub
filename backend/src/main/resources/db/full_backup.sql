-- MySQL dump 10.13  Distrib 9.6.0, for macos15.7 (arm64)
--
-- Host: localhost    Database: ai_invitecode
-- ------------------------------------------------------
-- Server version	9.6.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '9d306d12-14b0-11f1-8577-b89ef1dd7ee1:1-93';

--
-- Table structure for table `t_fetch_source`
--

DROP TABLE IF EXISTS `t_fetch_source`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_fetch_source` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `url` varchar(1024) COLLATE utf8mb4_unicode_ci NOT NULL,
  `feed_type` tinyint NOT NULL DEFAULT '1',
  `category_hint` tinyint DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `last_fetched_at` datetime DEFAULT NULL,
  `fetch_count` int NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_fetch_source`
--

LOCK TABLES `t_fetch_source` WRITE;
/*!40000 ALTER TABLE `t_fetch_source` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_fetch_source` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_invite_code`
--

DROP TABLE IF EXISTS `t_invite_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_invite_code` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL COMMENT '所属产品 ID',
  `provider_id` bigint NOT NULL COMMENT '提供者用户 ID',
  `parent_code_id` bigint DEFAULT NULL COMMENT '接龙来源邀请码 ID（NULL=首发）',
  `code_encrypted` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'AES-256 加密后的邀请码内容',
  `code_preview` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预览文本（如：ab******）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1可用 2已使用',
  `viewer_id` bigint DEFAULT NULL COMMENT '查看者用户 ID',
  `view_time` datetime DEFAULT NULL COMMENT '查看时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `claimant_id` bigint DEFAULT NULL COMMENT '获取者用户ID',
  `claim_time` datetime DEFAULT NULL COMMENT '获取时间',
  `confirm_deadline` datetime DEFAULT NULL COMMENT '确认截止时间（获取后12小时）',
  `confirm_time` datetime DEFAULT NULL COMMENT '实际确认时间',
  `confirm_result` tinyint DEFAULT NULL COMMENT '确认结果 1=有效 2=无效',
  `reviewer_id` bigint DEFAULT NULL COMMENT '审核管理员ID',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_status` (`product_id`,`status`,`create_time`),
  KEY `idx_provider_id` (`provider_id`),
  KEY `idx_parent_code_id` (`parent_code_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邀请码接龙表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_invite_code`
--

LOCK TABLES `t_invite_code` WRITE;
/*!40000 ALTER TABLE `t_invite_code` DISABLE KEYS */;
INSERT INTO `t_invite_code` VALUES (1,1,1,NULL,'yP8wzYuoUsTbu67+xY1Yl+7Ta22t9Q49Brbk0tqir+g=','****',2,2,'2026-02-28 22:27:36','2026-02-28 22:25:40',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(2,1,2,1,'Pen/VA1ESZ4fq1DqunneBRpLBdhArv9W1yyJEe7f31g=','****',2,NULL,NULL,'2026-02-28 22:27:49',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(3,1,2,NULL,'nwGP221eTKd5hUdW/O9b3fH9dZU7021mRB5Ee7iqntU=','****',4,NULL,NULL,'2026-02-28 22:28:02',4,'2026-03-03 09:18:07','2026-03-03 21:18:07','2026-03-03 09:18:13',2,NULL,NULL);
/*!40000 ALTER TABLE `t_invite_code` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_mcp_skill`
--

DROP TABLE IF EXISTS `t_mcp_skill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_mcp_skill` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(2048) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` tinyint NOT NULL DEFAULT '1',
  `category` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source_url` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `install_guide` text COLLATE utf8mb4_unicode_ci,
  `vendor` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tags` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '2',
  `collect_count` int NOT NULL DEFAULT '0',
  `use_count` int NOT NULL DEFAULT '0',
  `sort` int NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type_status` (`type`,`status`,`sort`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_mcp_skill`
--

LOCK TABLES `t_mcp_skill` WRITE;
/*!40000 ALTER TABLE `t_mcp_skill` DISABLE KEYS */;
INSERT INTO `t_mcp_skill` VALUES (1,'Filesystem MCP','让 AI 助手可以读写你电脑上的文件和文件夹。安全可控——你可以指定 AI 只能访问哪些目录，非常适合整理文档和代码项目。',1,'文件管理','https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem',NULL,'Anthropic','MCP,文件,本地',3,234,1801,100,'2026-03-01 19:34:01','2026-03-01 19:36:28'),(2,'Browser Use MCP','赋予 AI 助手真实浏览器操作能力：自动填表、截图、抓取网页数据，就像让 AI 帮你用电脑一样。',1,'浏览器','https://github.com/browser-use/browser-use',NULL,'browser-use','MCP,浏览器,自动化',2,456,3202,95,'2026-03-01 19:34:01','2026-03-06 08:44:34'),(3,'GitHub MCP','通过 AI 助手直接管理你的 GitHub 仓库：创建 Issue、提交 PR、查看代码改动，开发者效率神器。',1,'代码开发','https://github.com/modelcontextprotocol/servers/tree/main/src/github',NULL,'Anthropic','MCP,GitHub,代码',2,312,2601,90,'2026-03-01 19:34:01','2026-03-03 08:02:55'),(4,'Slack MCP','连接你的 Slack 工作区，让 AI 帮你搜索历史消息、发送通知、汇总频道内容，再也不用手动刷消息了。',1,'通讯协作','https://github.com/modelcontextprotocol/servers/tree/main/src/slack',NULL,'Anthropic','MCP,Slack,协作',2,189,1400,85,'2026-03-01 19:34:01','2026-03-01 19:34:01'),(5,'网页自动研究 Agent','给定研究主题，自动搜索多个网页、提取关键信息、去重整合，最终生成一份结构化的研究报告。适合竞品调研、行业分析等场景。',2,'研究分析','https://github.com/assafelovic/gpt-researcher',NULL,'Assaf Elovic','Agent,研究,自动化',2,567,4101,88,'2026-03-01 19:34:01','2026-03-03 08:03:02'),(6,'remind-me','将对话中提到的任何事项自动转化为准时提醒。只需告诉 AI 助手\"明天下午3点提醒我开会\"，它会在正确时间主动推送通知，再也不怕忘事。',2,'效率工具','https://clawhub.com/skills/remind-me','## 安装方式\n\n```bash\nnpx clawhub@latest install remind-me\n```\n\n## 使用示例\n\n- \"提醒我明天早上9点吃药\"\n- \"每周一早上发我本周任务清单\"\n- \"30分钟后提醒我去开会\"\n\n## 说明\n\n基于 OpenClaw 心跳机制，支持一次性和周期性提醒，Telegram 推送。','OpenClaw 社区','OpenClaw,提醒,定时,效率',2,389,4200,98,'2026-03-06 08:37:54','2026-03-06 08:37:54'),(7,'todo-tracker','智能待办清单管理技能。在对话中随口说出的任务会被自动收集，AI 助手会追踪完成进度并定期汇报，让你的任务一件都不漏。',2,'效率工具','https://clawhub.com/skills/todo-tracker','## 安装方式\n\n```bash\nnpx clawhub@latest install todo-tracker\n```\n\n## 使用示例\n\n- \"帮我记下：写季度报告\"\n- \"今天有哪些待办？\"\n- \"把写报告标记为完成\"\n\n## 说明\n\n支持优先级、截止日期、分类标签，可与 remind-me 技能联动。','OpenClaw 社区','OpenClaw,待办,任务管理,效率',2,312,3800,96,'2026-03-06 08:37:54','2026-03-06 08:37:54'),(8,'imap-email','连接你的邮箱（支持 Gmail / Outlook / 任意 IMAP 邮箱），让 AI 助手自动监控新邮件、提炼摘要、识别重要信息，并协助起草回复，彻底解放邮件处理时间。',2,'通讯协作','https://clawhub.com/skills/imap-email','## 安装方式\n\n```bash\nnpx clawhub@latest install imap-email\n```\n\n## 配置\n\n在 `.env` 中设置：\n```\nIMAP_HOST=imap.gmail.com\nIMAP_USER=your@email.com\nIMAP_PASS=your-app-password\n```\n\n## 使用示例\n\n- \"总结今天的未读邮件\"\n- \"帮我回复上面那封邮件\"\n- \"有没有关于项目的重要邮件？\"','OpenClaw 社区','OpenClaw,邮件,Gmail,IMAP,自动化',2,456,5100,94,'2026-03-06 08:37:54','2026-03-06 08:37:54'),(9,'web-search','为 AI 助手接入实时联网搜索能力（基于 Brave Search / Tavily），让助手能获取最新资讯、查询实时数据，回答不再局限于训练数据截止日期。',2,'搜索研究','https://clawhub.com/skills/web-search','## 安装方式\n\n```bash\nnpx clawhub@latest install web-search\n```\n\n## 配置\n\n```\nBRAVE_SEARCH_API_KEY=your-api-key\n```\n获取免费 API Key：https://brave.com/search/api/\n\n## 使用示例\n\n- \"搜索今天 A 股市场情况\"\n- \"最新的 Claude 4 有哪些功能？\"\n- \"帮我找三篇关于 RAG 的最新论文\"','OpenClaw 社区','OpenClaw,搜索,Brave,联网,实时',2,534,6200,92,'2026-03-06 08:37:54','2026-03-06 08:37:54'),(10,'browser','基于 Playwright 的浏览器自动化技能，赋予 AI 助手真实打开网页、点击、填表、截图的能力。爬取竞品价格、自动填写表单、网页内容抓取，一句话搞定。',2,'浏览器','https://clawhub.com/skills/browser','## 安装方式\n\n```bash\nnpx clawhub@latest install browser\n# 首次使用需安装浏览器驱动\nnpx playwright install chromium\n```\n\n## 使用示例\n\n- \"打开京东搜索 AirPods Pro 价格\"\n- \"截图 example.com 的首页\"\n- \"帮我登录 XX 网站并下载最新报告\"\n\n## 注意\n\n需要服务器有图形环境或使用 headless 模式。','OpenClaw 社区','OpenClaw,浏览器,Playwright,自动化,爬虫',2,678,7800,90,'2026-03-06 08:37:54','2026-03-06 08:37:54'),(11,'weather-nws','实时天气查询技能，支持全球城市天气、未来7天预报、极端天气预警。可配合心跳机制实现每日自动推送天气早报，出行前再也不用单独打开天气 APP。',2,'生活助手','https://clawhub.com/skills/weather-nws','## 安装方式\n\n```bash\nnpx clawhub@latest install weather-nws\n```\n\n## 使用示例\n\n- \"今天北京天气怎么样？\"\n- \"本周末上海会下雨吗？\"\n- \"设置每天早上7点推送天气\"\n\n## 说明\n\n免费使用，无需 API Key，数据来源 NWS（美国国家气象局），全球城市均支持。','OpenClaw 社区','OpenClaw,天气,天气预报,生活',2,234,2900,85,'2026-03-06 08:37:54','2026-03-06 08:37:54'),(12,'newsletter-digest','订阅内容摘要技能，自动抓取你关注的 Newsletter、RSS 源、YouTube 频道最新内容，提炼核心要点推送给你，告别信息过载，每天10分钟掌握行业动态。',2,'内容聚合','https://clawhub.com/skills/newsletter-digest','## 安装方式\n\n```bash\nnpx clawhub@latest install newsletter-digest\n```\n\n## 配置\n\n在技能配置文件中添加订阅源：\n```yaml\nsources:\n  - type: rss\n    url: https://example.com/feed\n  - type: youtube\n    channel: UCxxxxxxx\n```\n\n## 使用示例\n\n- \"给我今天的科技资讯摘要\"\n- \"最近有没有 AI 相关的好文章？\"','OpenClaw 社区','OpenClaw,RSS,摘要,内容聚合,Newsletter',2,289,3400,82,'2026-03-06 08:37:54','2026-03-06 08:37:54'),(13,'pdf-parser','文档解析技能（基于 markitdown），支持 PDF、Word、PPT、Excel 等多种格式，将文档转为结构化 Markdown，让 AI 助手能直接分析、总结、问答文档内容。',2,'文件管理','https://clawhub.com/skills/pdf-parser','## 安装方式\n\n```bash\nnpx clawhub@latest install pdf-parser\n```\n\n## 使用示例\n\n- \"帮我总结这份 PDF 合同的关键条款\"\n- \"把这个 PPT 转成文字版\"\n- \"这份报告里有没有提到竞品价格？\"\n\n## 支持格式\n\nPDF、DOCX、PPTX、XLSX、HTML、CSV、图片（OCR）','OpenClaw 社区','OpenClaw,PDF,文档,解析,markitdown',2,198,2600,80,'2026-03-06 08:37:54','2026-03-06 08:37:54'),(14,'obsidian','连接你的 Obsidian 知识库，让 AI 助手读写笔记、跨文档检索、自动整理日记，打通 AI 与个人知识管理系统，让你的\"第二大脑\"真正智能起来。',2,'笔记知识','https://clawhub.com/skills/obsidian','## 安装方式\n\n```bash\nnpx clawhub@latest install obsidian\n```\n\n## 配置\n\n```\nOBSIDIAN_VAULT_PATH=/path/to/your/vault\n```\n\n## 使用示例\n\n- \"在我的 Obsidian 里搜索关于 MCP 的笔记\"\n- \"帮我创建今天的日记\"\n- \"把这段内容整理成笔记保存到知识库\"','OpenClaw 社区','OpenClaw,Obsidian,笔记,知识管理,PKM',2,312,3100,78,'2026-03-06 08:37:54','2026-03-06 08:37:54'),(15,'homeassistant','接入 Home Assistant 智能家居平台，让 AI 助手控制灯光、空调、门锁等智能设备，查询家庭能耗，设置自动化场景，用自然语言管理你的全屋智能。',2,'智能家居','https://clawhub.com/skills/homeassistant','## 安装方式\n\n```bash\nnpx clawhub@latest install homeassistant\n```\n\n## 配置\n\n```\nHA_URL=http://your-home-assistant:8123\nHA_TOKEN=your-long-lived-access-token\n```\n\n## 使用示例\n\n- \"把客厅灯调到50%亮度\"\n- \"今天家里用了多少电？\"\n- \"我要出门了，帮我关闭所有灯和空调\"','OpenClaw 社区','OpenClaw,智能家居,HomeAssistant,IoT,自动化',2,267,2800,75,'2026-03-06 08:37:54','2026-03-06 08:37:54'),(16,'coding-agent','专业编程 Agent 技能，具备读写代码文件、运行终端命令、调试报错的能力。描述需求后，AI 自动完成从需求分析到代码实现的全流程，支持多语言多框架。',2,'代码开发','https://clawhub.com/skills/coding-agent','## 安装方式\n\n```bash\nnpx clawhub@latest install coding-agent\n```\n\n## 使用示例\n\n- \"帮我写一个 Python 脚本，批量重命名文件夹里的图片\"\n- \"这段代码有什么 bug？帮我修复\"\n- \"用 Vue3 写一个带搜索功能的列表组件\"\n\n## 说明\n\n需要授权文件系统访问权限，建议配合 Filesystem MCP 使用。','OpenClaw 社区','OpenClaw,编程,代码,Agent,开发',2,445,5600,88,'2026-03-06 08:37:54','2026-03-06 08:37:54'),(17,'seo-audit','SEO 分析与优化技能，自动检测网站 SEO 问题：标题、描述、关键词密度、链接结构、页面速度等，生成可执行的优化建议报告，适合网站运营和内容创作者。',2,'营销分析','https://clawhub.com/skills/seo-audit','## 安装方式\n\n```bash\nnpx clawhub@latest install seo-audit\n```\n\n## 使用示例\n\n- \"分析 example.com 的 SEO 问题\"\n- \"这篇文章的关键词布局合理吗？\"\n- \"帮我生成一份 SEO 优化报告\"\n\n## 说明\n\n配合 browser 技能效果更佳，可实时抓取页面数据进行分析。','OpenClaw 社区','OpenClaw,SEO,营销,网站优化,分析',2,189,2200,72,'2026-03-06 08:37:54','2026-03-06 08:37:54'),(18,'discord','连接 Discord 服务器，让 AI 助手监控频道消息、自动回复、发送通知、汇总社区动态。适合社区运营者和需要 Discord 信息聚合的用户。',2,'通讯协作','https://clawhub.com/skills/discord','## 安装方式\n\n```bash\nnpx clawhub@latest install discord\n```\n\n## 配置\n\n```\nDISCORD_TOKEN=your-bot-token\nDISCORD_GUILD_ID=your-server-id\n```\n\n## 使用示例\n\n- \"总结今天 #general 频道的重要讨论\"\n- \"在 #announcements 发送一条公告\"\n- \"有没有人在讨论产品 bug？\"','OpenClaw 社区','OpenClaw,Discord,社区,通讯,机器人',2,156,1800,68,'2026-03-06 08:37:54','2026-03-06 08:37:54'),(19,'kimi-integration','集成月之暗面 Kimi AI 大模型，为 OpenClaw 助手提供超长上下文（支持百万 token）处理能力，适合分析超长文档、代码库、研究报告等大体量内容场景。',2,'AI大模型','https://clawhub.com/skills/kimi-integration','## 安装方式\n\n```bash\nnpx clawhub@latest install kimi-integration\n```\n\n## 配置\n\n```\nKIMI_API_KEY=your-moonshot-api-key\n```\n获取 API Key：https://platform.moonshot.cn/\n\n## 使用示例\n\n- \"用 Kimi 分析这个 500 页的 PDF 报告\"\n- \"把整个代码仓库传给 Kimi 帮我梳理架构\"\n- \"这篇超长论文的核心观点是什么？\"','OpenClaw 社区','OpenClaw,Kimi,月之暗面,大模型,长上下文',2,223,2700,70,'2026-03-06 08:37:54','2026-03-06 08:37:54');
/*!40000 ALTER TABLE `t_mcp_skill` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_news`
--

DROP TABLE IF EXISTS `t_news`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_news` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `summary` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cover` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tags` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category` tinyint NOT NULL DEFAULT '1',
  `author_id` bigint DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '2',
  `like_count` int NOT NULL DEFAULT '0',
  `view_count` int NOT NULL DEFAULT '0',
  `publish_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_category` (`status`,`category`,`publish_time`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资讯表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_news`
--

LOCK TABLES `t_news` WRITE;
/*!40000 ALTER TABLE `t_news` DISABLE KEYS */;
INSERT INTO `t_news` VALUES (1,'GPT-4o 迎来重大更新，多模态能力全面升级','OpenAI 发布 GPT-4o 最新版本，图像理解、代码生成和实时语音能力均有显著提升，API 调用成本降低 50%。',NULL,'https://openai.com/blog','GPT-4o,OpenAI,多模态',1,NULL,2,12,343,'2026-02-27 22:50:19','2026-02-28 22:50:19'),(2,'Claude 3.7 Sonnet 发布，推理能力达到新高度','Anthropic 正式推出 Claude 3.7 Sonnet，在数学推理、代码编写和复杂分析方面超越前代，支持 200K token 超长上下文。',NULL,'https://anthropic.com/blog','Claude,Anthropic,推理',1,NULL,2,8,211,'2026-02-26 22:50:19','2026-02-28 22:50:19'),(3,'国产 AI 崛起：DeepSeek-R2 开源发布','深度求索发布 DeepSeek-R2 开源大模型，在多项基准测试中达到国际顶尖水平，完全免费可商用，引发全球 AI 社区广泛关注。',NULL,'https://deepseek.com','DeepSeek,开源,国内AI',1,NULL,2,45,1203,'2026-02-25 22:50:19','2026-02-28 22:50:19'),(4,'Midjourney v7 开放内测，画质提升令人惊叹','Midjourney 最新版本 v7 开放部分用户内测，采用全新架构，生成图像细节、光影和人体结构均有大幅改善，支持更精准的风格控制。',NULL,'https://midjourney.com','Midjourney,绘图AI,v7',3,NULL,4,31,891,'2026-02-24 22:50:19','2026-02-28 22:50:19'),(5,'AI 编程工具对比：Cursor vs GitHub Copilot 2026年最新评测','本文对两款主流 AI 编程助手进行深度横评，从代码补全、错误修复、多文件编辑等维度全面对比，附实际使用场景下的速度测试数据。',NULL,'https://example.com/ai-coding-tools','编程AI,Cursor,Copilot,评测',4,NULL,2,56,2100,'2026-02-23 22:50:19','2026-02-28 22:50:19');
/*!40000 ALTER TABLE `t_news` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_news_like`
--

DROP TABLE IF EXISTS `t_news_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_news_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `news_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_news_user` (`news_id`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资讯点赞记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_news_like`
--

LOCK TABLES `t_news_like` WRITE;
/*!40000 ALTER TABLE `t_news_like` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_news_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_point_record`
--

DROP TABLE IF EXISTS `t_point_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_point_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `delta` int NOT NULL COMMENT '积分变化（正增负减）',
  `balance` int NOT NULL COMMENT '变动后余额快照',
  `biz_type` tinyint NOT NULL COMMENT '1注册 2签到 3查看邀请码 4邀请码被查看 5邀请用户 6任务',
  `biz_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联业务 ID',
  `remark` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分流水表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_point_record`
--

LOCK TABLES `t_point_record` WRITE;
/*!40000 ALTER TABLE `t_point_record` DISABLE KEYS */;
INSERT INTO `t_point_record` VALUES (1,1,20,520,6,'2','完成任务：首次提交邀请码','2026-02-28 22:25:40'),(2,2,-50,150,3,'1','查看邀请码','2026-02-28 22:27:36'),(3,1,30,550,4,'1','邀请码被查看','2026-02-28 22:27:36'),(4,2,20,170,6,'2','完成任务：首次提交邀请码','2026-02-28 22:27:49'),(5,4,100,100,1,NULL,'新用户注册奖励','2026-03-03 07:54:14'),(6,4,-50,50,10,'3','获取邀请码','2026-03-03 09:18:07'),(7,2,-60,110,6,'3','邀请码被判定无效','2026-03-03 09:18:13'),(8,4,50,100,11,'3','邀请码无效退款','2026-03-03 09:18:13'),(9,4,10,110,2,NULL,'每日签到','2026-03-03 09:27:10');
/*!40000 ALTER TABLE `t_point_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_product`
--

DROP TABLE IF EXISTS `t_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_product` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品名称',
  `logo` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Logo URL',
  `description` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '产品简介',
  `official_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '官网地址',
  `category` tinyint NOT NULL COMMENT '1对话 2绘图 3代码 4视频 5音乐 9其他',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1上线 2下线',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序权重（越大越靠前）',
  `code_count` int NOT NULL DEFAULT '0' COMMENT '当前可用邀请码数量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `product_group` tinyint NOT NULL DEFAULT '2' COMMENT '1=OpenClaw生态 2=AI应用 3=其他',
  PRIMARY KEY (`id`),
  KEY `idx_category_status` (`category`,`status`,`sort`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 产品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_product`
--

LOCK TABLES `t_product` WRITE;
/*!40000 ALTER TABLE `t_product` DISABLE KEYS */;
INSERT INTO `t_product` VALUES (1,'ChatGPT',NULL,'OpenAI 出品的对话式 AI 助手，支持写作、编程、问答等','https://chat.openai.com',1,2,100,1,'2026-02-28 22:20:59','2026-03-03 09:18:06',2),(2,'Claude',NULL,'Anthropic 出品的 AI 助手，擅长分析与长文本处理','https://claude.ai',1,2,95,0,'2026-02-28 22:20:59','2026-02-28 22:20:59',2),(3,'Gemini',NULL,'Google 出品的多模态 AI 助手','https://gemini.google.com',1,2,90,0,'2026-02-28 22:20:59','2026-02-28 22:20:59',2),(4,'Midjourney',NULL,'强大的 AI 绘图工具，生成高质量艺术图像','https://midjourney.com',2,2,88,0,'2026-02-28 22:20:59','2026-02-28 22:20:59',2),(5,'Stable Diffusion',NULL,'开源 AI 绘图模型，可本地部署使用','https://stability.ai',2,2,75,0,'2026-02-28 22:20:59','2026-02-28 22:20:59',2),(6,'GitHub Copilot',NULL,'GitHub 出品的 AI 代码助手，智能补全与生成代码','https://github.com/features/copilot',3,2,92,0,'2026-02-28 22:20:59','2026-02-28 22:20:59',2),(7,'Cursor',NULL,'AI 驱动的代码编辑器，深度集成 Claude 代码生成能力','https://cursor.sh',3,2,88,0,'2026-02-28 22:20:59','2026-02-28 22:20:59',2),(8,'Sora',NULL,'OpenAI 文本生成视频模型，高质量 AI 视频创作','https://openai.com/sora',4,2,80,0,'2026-02-28 22:20:59','2026-02-28 22:20:59',2),(9,'Suno',NULL,'AI 音乐创作工具，输入文本自动生成完整歌曲','https://suno.ai',5,2,75,0,'2026-02-28 22:20:59','2026-02-28 22:20:59',2),(10,'Perplexity',NULL,'AI 搜索引擎，实时联网回答问题并标注来源','https://perplexity.ai',1,2,85,0,'2026-02-28 22:20:59','2026-02-28 22:20:59',2),(11,'OpenClaw',NULL,'开源自托管 AI Agent 框架，支持 Telegram/Discord 多渠道交互，5490+ 社区技能插件，月均费用仅 $8-35，GitHub 264k Stars。','https://github.com/openclaw/openclaw',1,2,99,0,'2026-03-06 08:52:32','2026-03-06 08:52:32',1),(12,'ClawHub',NULL,'OpenClaw 官方技能市场，收录 5490+ 社区开发技能，覆盖编程、DevOps、营销、智能家居等 31 个分类。','https://clawhub.com',9,2,98,0,'2026-03-06 08:52:32','2026-03-06 08:52:32',1),(13,'ClawDBot',NULL,'OpenClaw 的企业级 AI Agent 实现，强化了垂直整合能力，适合在 Slack/企业微信等平台部署私有 AI 助手。','https://clawhub.com/clawdbot',1,2,97,0,'2026-03-06 08:52:32','2026-03-06 08:52:32',1),(14,'evomap','https://evomap.ai/','','https://evomap.ai/',9,1,0,0,'2026-03-06 09:42:06','2026-03-06 09:42:06',2),(15,'Elys','','','',9,1,0,0,'2026-03-06 09:42:43','2026-03-06 09:42:43',2);
/*!40000 ALTER TABLE `t_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_resource`
--

DROP TABLE IF EXISTS `t_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_resource` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `summary` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source_url` varchar(1024) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cover` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `author` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category` tinyint NOT NULL DEFAULT '1',
  `difficulty` tinyint NOT NULL DEFAULT '1',
  `tags` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '2',
  `view_count` int NOT NULL DEFAULT '0',
  `collect_count` int NOT NULL DEFAULT '0',
  `is_auto_fetched` tinyint NOT NULL DEFAULT '0',
  `submit_user_id` bigint DEFAULT NULL,
  `publish_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_category` (`status`,`category`,`publish_time`),
  KEY `idx_difficulty` (`difficulty`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_resource`
--

LOCK TABLES `t_resource` WRITE;
/*!40000 ALTER TABLE `t_resource` DISABLE KEYS */;
INSERT INTO `t_resource` VALUES (1,'大模型入门：从 Transformer 到 ChatGPT 的演进之路','本文用最通俗的语言解释大语言模型的核心原理，从 Attention 机制到 RLHF，适合完全没有 AI 背景的读者入门。','https://example.com/llm-intro',NULL,'机器之心',1,1,'大模型,入门,Transformer',2,1240,89,0,NULL,'2026-02-26 19:34:01','2026-03-01 19:34:01','2026-03-01 19:34:01'),(2,'MCP 协议完全指南：让 Claude 连接你的本地工具','Model Context Protocol (MCP) 是 Anthropic 提出的开放标准，让 AI 助手可以安全地访问本地文件、数据库和 API。本文手把手教你配置第一个 MCP Server。','https://example.com/mcp-guide',NULL,'Anthropic Blog',3,2,'MCP,Claude,工具连接',2,3400,256,0,NULL,'2026-02-24 19:34:01','2026-03-01 19:34:01','2026-03-01 19:34:01'),(3,'Agent 框架横评 2026：LangChain vs AutoGen vs OpenClaw','深度对比三大主流 Agent 框架的架构设计、易用性和生态成熟度，附真实项目接入成本评估。','https://example.com/agent-comparison',NULL,'AI产品评测',2,2,'Agent,LangChain,AutoGen',2,5600,412,0,NULL,'2026-02-22 19:34:01','2026-03-01 19:34:01','2026-03-01 19:34:01'),(4,'用 AI 工具每天节省 2 小时：10 个真实工作流程','整理了 10 个职场人士高频使用的 AI 工作流，包括会议纪要、周报生成、邮件回复、数据分析，每个附有实操截图。','https://example.com/ai-workflow',NULL,'效率工具研究所',4,1,'工作流,效率,提示词',2,8900,678,0,NULL,'2026-02-19 19:34:01','2026-03-01 19:34:01','2026-03-01 19:34:01'),(5,'OpenClaw 上手教程：用 WhatsApp 控制你的 Claude','OpenClaw 是最受欢迎的个人 AI 助手开源框架，通过 MCP 将 Claude 连接到本地系统，并支持 WhatsApp/Telegram 作为对话入口。','https://example.com/openclaw-quickstart',NULL,'OpenClaw 官方',3,2,'OpenClaw,Claude,MCP,Agent',2,2300,198,0,NULL,'2026-02-27 19:34:01','2026-03-01 19:34:01','2026-03-01 19:34:01'),(6,'Day 1：初识 OpenClaw —— 开源私人 AI 助手入门','OpenClaw 是开源自托管个人 AI 助手框架，支持 Telegram/Discord 多渠道、5490+ 社区技能插件、本地完全隐私，月均费用仅 $8-35，7天教程首篇带你全面了解。','https://openclaw101.dev/zh/day/1',NULL,'OpenClaw 101',2,1,'OpenClaw,Agent,入门,AI助手',2,3800,312,0,NULL,'2026-02-20 08:28:56','2026-03-06 08:28:56','2026-03-06 08:28:56'),(7,'Day 2：10 分钟搭建你的专属 AI 助手','一行命令自动安装 OpenClaw，支持云服务器/Mac Mini/本地电脑三种部署方式，配合 Claude API Key 和 Telegram Bot Token 即可完成首次对话，\"比泡方便面还简单\"。','https://openclaw101.dev/zh/day/2',NULL,'OpenClaw 101',4,1,'OpenClaw,部署,Telegram,Claude',2,5200,428,0,NULL,'2026-02-21 08:28:56','2026-03-06 08:28:56','2026-03-06 08:28:56'),(8,'Day 3：给助手一个灵魂 —— SOUL.md / USER.md / AGENTS.md 配置指南','通过编写 SOUL.md（性格准则）、USER.md（主人档案）、AGENTS.md（工作规范）三个灵魂文件，将通用 AI 变成真正了解你的个人助手，配置可持续迭代优化。','https://openclaw101.dev/zh/day/3',NULL,'OpenClaw 101',4,1,'OpenClaw,个性化,配置,AI助手',2,4100,356,0,NULL,'2026-02-22 08:28:56','2026-03-06 08:28:56','2026-03-06 08:28:56'),(9,'Day 4：接入你的数字生活 —— Gmail / 日历 / 搜索 / 浏览器集成','为 AI 助手安装 Gmail、Google Calendar、Brave Search、Playwright 浏览器四大核心技能，通过 OAuth 授权实现邮件读取、日程管理、联网搜索和网页操作，助手从聊天工具升级为实用助手。','https://openclaw101.dev/zh/day/4',NULL,'OpenClaw 101',4,2,'OpenClaw,Gmail,技能,自动化,浏览器',2,3600,298,0,NULL,'2026-02-23 08:28:56','2026-03-06 08:28:56','2026-03-06 08:28:56'),(10,'Day 5：解锁技能树 —— OpenClaw 技能系统完全指南','OpenClaw 技能相当于 AI 助手的 App Store，每个技能由 SKILL.md+配置文件+脚本组成。精选10个核心技能推荐，并介绍多技能组合实现\"1+1>2\"自动化工作流的最佳实践。','https://openclaw101.dev/zh/day/5',NULL,'OpenClaw 101',4,2,'OpenClaw,技能,工作流,自动化',2,3200,267,0,NULL,'2026-02-24 08:28:56','2026-03-06 08:28:56','2026-03-06 08:28:56'),(11,'Day 6：让助手主动工作 —— 心跳机制与 Cron 自动化','介绍 OpenClaw 心跳机制（每30分钟主动唤醒）和 Cron 定时任务，配合三层记忆系统（每日笔记/长期记忆/灵魂记忆），实现晨报推送、邮件监控、数据告警等主动式 AI 管家能力。','https://openclaw101.dev/zh/day/6',NULL,'OpenClaw 101',4,2,'OpenClaw,自动化,Cron,心跳,记忆',2,2900,241,0,NULL,'2026-02-25 08:28:56','2026-03-06 08:28:56','2026-03-06 08:28:56'),(12,'Day 7：进阶玩法与未来展望 —— 自定义技能开发与多节点协作','7天课程收官篇：自定义技能包开发方法、多设备节点跨平台协作（手机/电脑/树莓派）、完整安全清单，以及 AI 助手多模态交互与 Agent 协作网络的未来展望。','https://openclaw101.dev/zh/day/7',NULL,'OpenClaw 101',4,3,'OpenClaw,进阶,自定义,多节点,安全',2,2700,218,0,NULL,'2026-02-26 08:28:56','2026-03-06 08:28:56','2026-03-06 08:28:56'),(13,'阿里云一键部署 OpenClaw AI 助手完整教程','阿里云官方出品的 OpenClaw 轻量应用服务器快速部署指南，含环境配置、安全组规则、域名绑定全流程，适合国内用户低延迟自托管 AI 助手。','https://help.aliyun.com/zh/simple-application-server/use-cases/quickly-deploy-and-use-openclaw',NULL,'阿里云',4,1,'OpenClaw,阿里云,部署,云服务器',2,4500,389,0,NULL,'2026-02-14 08:28:56','2026-03-06 08:28:56','2026-03-06 08:28:56'),(14,'腾讯云部署 OpenClaw 实战教程','腾讯云开发者社区出品的 OpenClaw 部署教程，从云服务器选购到 OpenClaw 安装配置，再到 Telegram 机器人绑定，国内用户友好，含常见报错解决方案。','https://cloud.tencent.com/developer/article/2625073',NULL,'腾讯云开发者',4,1,'OpenClaw,腾讯云,部署,Telegram',2,3800,312,0,NULL,'2026-02-09 08:28:56','2026-03-06 08:28:56','2026-03-06 08:28:56'),(15,'DigitalOcean 部署 OpenClaw 官方教程（英文）','DigitalOcean 官方社区教程，使用 Droplet 一键部署 OpenClaw，适合海外服务器低延迟访问 Claude API，含 Docker 配置和防火墙设置。','https://www.digitalocean.com/community/tutorials/how-to-run-openclaw',NULL,'DigitalOcean',4,2,'OpenClaw,DigitalOcean,部署,Docker',2,2100,167,0,NULL,'2026-02-04 08:28:56','2026-03-06 08:28:56','2026-03-06 08:28:56'),(16,'菜鸟教程：OpenClaw/ClawDBot 新手完全指南','面向零基础用户的 OpenClaw 图文教程，覆盖安装、基础配置、常用命令、FAQ，语言通俗易懂，国内访问速度快，适合第一次接触 AI 助手的读者。','https://www.runoob.com/ai-agent/openclaw-clawdbot-tutorial.html',NULL,'菜鸟教程',4,1,'OpenClaw,入门,新手,中文教程',2,6200,541,0,NULL,'2026-02-16 08:28:56','2026-03-06 08:28:56','2026-03-06 08:28:56'),(17,'知乎：我用 OpenClaw 搭了个私人 AI 助手，体验分享','知乎作者真实使用体验分享，包括搭建过程踩坑记录、与 ChatGPT/Claude 直接使用的对比、适合人群分析，评论区有大量用户互动讨论，干货满满。','https://zhuanlan.zhihu.com/p/2000850539936765122',NULL,'知乎专栏',4,1,'OpenClaw,使用体验,AI助手,测评',2,7800,623,0,NULL,'2026-02-19 08:28:56','2026-03-06 08:28:56','2026-03-06 08:28:56'),(18,'36氪：OpenClaw 凭什么成为 GitHub 最热 AI 项目','36氪深度报道 OpenClaw 在 GitHub 获得 264k Star 的原因，分析开源私人 AI 助手赛道崛起背景、与商业 AI 产品的差异化竞争，以及国内用户使用现状。','https://36kr.com/p/3671941309260675',NULL,'36氪',6,1,'OpenClaw,开源,AI助手,行业分析',2,5400,432,0,NULL,'2026-02-12 08:28:56','2026-03-06 08:28:56','2026-03-06 08:28:56'),(19,'安全警告：研究人员发现 341 个恶意 ClawHub 技能包','The Hacker News 报道：安全研究人员在 ClawHub 技能市场发现341个恶意技能，可窃取用户数据和 API 密钥。文章详解攻击原理及防护措施，安装第三方技能前务必审查源码。','https://thehackernews.com/2026/02/researchers-find-341-malicious-clawhub.html',NULL,'The Hacker News',6,3,'OpenClaw,安全,ClawHub,恶意软件,风险',2,8900,712,0,NULL,'2026-01-30 08:28:56','2026-03-06 08:28:56','2026-03-06 08:28:56'),(20,'IBM Think：ClawDBot —— 测试 AI Agent 垂直整合极限','IBM 技术博客深度分析 ClawDBot（OpenClaw 的一个实现变种）在垂直整合场景下的能力边界，探讨 AI Agent 在企业级工作流中的实际表现与局限性。','https://www.ibm.com/think/news/clawdbot-ai-agent-testing-limits-vertical-integration',NULL,'IBM Think',6,3,'Agent,IBM,企业AI,垂直整合,ClawDBot',2,3200,256,0,NULL,'2026-01-25 08:28:56','2026-03-06 08:28:56','2026-03-06 08:28:56'),(21,'Cisco 安全博客：个人 AI Agent 是安全噩梦？','Cisco 安全团队从企业安全视角分析 OpenClaw 等个人 AI Agent 的安全隐患：本地文件访问、API 密钥暴露、网络权限滥用等风险，并提供企业网络环境下的防护建议。','https://blogs.cisco.com/ai/personal-ai-agents-like-openclaw-are-a-security-nightmare',NULL,'Cisco Blogs',6,3,'OpenClaw,安全,企业,AI Agent,风险评估',2,4100,318,0,NULL,'2026-01-20 08:28:56','2026-03-06 08:28:56','2026-03-06 08:28:56'),(22,'The Verge：OpenClaw —— 让每个人都能拥有私人 AI 管家','The Verge 科技媒体对 OpenClaw 的深度报道，探讨开源私人 AI 助手如何挑战 ChatGPT 等商业产品，分析其用户群体特征及隐私优势，并访谈核心开发者。','https://www.theverge.com/news/872091/openclaw-moltbot-clawdbot-ai-agent-news',NULL,'The Verge',6,1,'OpenClaw,AI助手,隐私,开源,媒体报道',2,5600,445,0,NULL,'2026-02-06 08:28:56','2026-03-06 08:28:56','2026-03-06 08:28:56'),(23,'OpenClaw 官方 GitHub 仓库（264k Stars）','OpenClaw 开源 AI Agent 框架官方代码仓库，包含完整源码、安装脚本、技能开发文档和 API 文档。全球最受欢迎的个人 AI 助手框架之一，社区活跃，持续迭代。','https://github.com/openclaw/openclaw',NULL,'OpenClaw 开源社区',2,2,'OpenClaw,GitHub,开源,Agent框架',2,9200,876,0,NULL,'2026-01-05 08:28:56','2026-03-06 08:28:56','2026-03-06 08:28:56'),(24,'ClawHub 技能市场 —— 5490+ 社区技能一站获取','OpenClaw 官方技能市场，收录5490+社区开发技能，覆盖网页前端、编程、DevOps、搜索研究、营销、AI大模型、智能家居等31个分类，安装前请务必检查源码安全性。','https://clawhub.com',NULL,'ClawHub',2,1,'OpenClaw,技能市场,ClawHub,插件',2,6800,534,0,NULL,'2026-01-10 08:28:56','2026-03-06 08:28:56','2026-03-06 08:28:56'),(25,'飞书机器人接入 OpenClaw 完整教程','将 OpenClaw AI 助手接入飞书（Lark）的详细配置教程，适合国内企业用户在飞书生态内使用私人 AI 助手，含飞书应用创建、Webhook 配置、消息路由全流程。','https://my.feishu.cn/wiki/YkWgwqSchi9xW3kEuZscAm0lnFf',NULL,'飞书知识库',4,2,'OpenClaw,飞书,Lark,企业,机器人',2,2800,231,0,NULL,'2026-02-18 08:28:56','2026-03-06 08:28:56','2026-03-06 08:28:56');
/*!40000 ALTER TABLE `t_resource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_resource_collect`
--

DROP TABLE IF EXISTS `t_resource_collect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_resource_collect` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resource_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_user` (`resource_id`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_resource_collect`
--

LOCK TABLES `t_resource_collect` WRITE;
/*!40000 ALTER TABLE `t_resource_collect` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_resource_collect` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_site`
--

DROP TABLE IF EXISTS `t_site`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_site` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '网站名称',
  `description` varchar(1024) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '网站描述（200字内）',
  `url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '网站链接',
  `logo` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Logo URL',
  `category` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类（工具/研究/社区/教育/资讯/其他）',
  `tags` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签，逗号分隔',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1待审核 2已发布 3已拒绝',
  `submit_user_id` bigint DEFAULT NULL COMMENT '投稿用户ID，NULL=编辑录入',
  `view_count` int NOT NULL DEFAULT '0',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序权重（越大越靠前）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`,`sort`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 网站导航表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_site`
--

LOCK TABLES `t_site` WRITE;
/*!40000 ALTER TABLE `t_site` DISABLE KEYS */;
INSERT INTO `t_site` VALUES (1,'ChatGPT','OpenAI 出品的 AI 对话助手，支持写作、编程、分析等场景，GPT-4o 已支持图像、语音多模态输入。','https://chat.openai.com',NULL,'工具','ChatGPT,OpenAI,对话AI',2,NULL,1,100,'2026-03-06 09:05:36','2026-03-06 09:05:36'),(2,'Claude','Anthropic 出品的 AI 助手，擅长长文本处理与深度分析，支持 200K 上下文，安全性强。','https://claude.ai',NULL,'工具','Claude,Anthropic,对话AI',2,NULL,0,99,'2026-03-06 09:05:36','2026-03-06 09:05:36'),(3,'Perplexity','AI 驱动的实时搜索引擎，回答带来源引用，适合研究和快速获取可靠信息。','https://perplexity.ai',NULL,'工具','Perplexity,搜索,AI',2,NULL,0,95,'2026-03-06 09:05:36','2026-03-06 09:05:36'),(4,'Cursor','AI 驱动的代码编辑器，内置 Claude 模型，支持自然语言编程和多文件上下文理解。','https://cursor.sh',NULL,'工具','Cursor,代码AI,编程',2,NULL,0,93,'2026-03-06 09:05:36','2026-03-06 09:05:36'),(5,'Midjourney','最受欢迎的 AI 绘图工具之一，擅长艺术风格图像生成，通过 Discord 使用。','https://midjourney.com',NULL,'工具','Midjourney,绘图AI,AI艺术',2,NULL,1,90,'2026-03-06 09:05:36','2026-03-06 09:05:36'),(6,'Hugging Face','AI 社区和模型托管平台，提供数万个开源模型、数据集和在线推理空间，是 AI 研究者的重要资源。','https://huggingface.co',NULL,'研究','HuggingFace,开源,模型',2,NULL,0,88,'2026-03-06 09:05:36','2026-03-06 09:05:36'),(7,'Papers With Code','汇聚最新 AI 论文及对应代码实现，按任务/数据集/方法分类，跟踪研究前沿必备。','https://paperswithcode.com',NULL,'研究','论文,AI研究,SOTA',2,NULL,0,85,'2026-03-06 09:05:36','2026-03-06 09:05:36'),(8,'OpenClaw 101','OpenClaw AI Agent 框架的中文学习导航站，收录 374+ 教程资源，7天系列入门课程。','https://openclaw101.dev/zh',NULL,'教育','OpenClaw,Agent,教程,中文',2,NULL,0,92,'2026-03-06 09:05:36','2026-03-06 09:05:36'),(9,'Anthropic Blog','Anthropic 官方技术博客，发布 Claude 模型更新、AI 安全研究和前沿技术解读。','https://www.anthropic.com/blog',NULL,'资讯','Anthropic,Claude,AI安全',2,NULL,0,80,'2026-03-06 09:05:36','2026-03-06 09:05:36'),(10,'AI产品榜','追踪全球 AI 产品流量与增长趋势，发现下一个爆款 AI 工具。','https://top.aibase.com',NULL,'资讯','AI产品,榜单,流量',2,NULL,0,75,'2026-03-06 09:05:36','2026-03-06 09:05:36');
/*!40000 ALTER TABLE `t_site` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_skill`
--

DROP TABLE IF EXISTS `t_skill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_skill` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能标题',
  `prompt` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提示词正文',
  `applicable` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '适用产品，逗号分隔',
  `category` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类：写作/编程/设计/营销/分析/翻译/教育/职场/生活',
  `tags` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '场景标签，逗号分隔',
  `author_id` bigint NOT NULL COMMENT '提交者用户ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1待审核 2已发布 3已拒绝',
  `like_count` int NOT NULL DEFAULT '0',
  `collect_count` int NOT NULL DEFAULT '0',
  `use_count` int NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_category` (`status`,`category`,`collect_count`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能/提示词表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_skill`
--

LOCK TABLES `t_skill` WRITE;
/*!40000 ALTER TABLE `t_skill` DISABLE KEYS */;
INSERT INTO `t_skill` VALUES (1,'爆款小红书文案生成器','你是一名擅长小红书内容运营的文案专家。请根据我提供的主题，写一篇爆款小红书笔记，要求：\n1. 标题使用数字+emoji吸引眼球，如「✨ 5个让你...」\n2. 开头3行必须钩住读者，引发共鸣或好奇心\n3. 正文分点展开，每点附上具体细节和个人感受\n4. 结尾引导互动，如「你们有没有同款经历？」\n5. 配上10个精准话题标签\n主题：[填入你的主题]','ChatGPT,Claude,Gemini','营销','小红书,文案,爆款',1,2,89,234,3202,'2026-03-01 08:25:39'),(2,'代码 Review 专家','你是一名资深软件工程师，请对下面的代码进行专业Review，从以下维度给出反馈：\n1. **正确性**：逻辑是否有 bug，边界条件是否处理\n2. **性能**：有无明显的性能瓶颈，时间/空间复杂度分析\n3. **可读性**：命名是否清晰，注释是否必要\n4. **安全性**：有无 SQL注入、XSS、越权等安全问题\n5. **最佳实践**：是否符合该语言/框架的惯例\n请给出修改后的代码示例，并解释改动原因。\n代码：\n```\n[粘贴代码]\n```','ChatGPT,Claude,Cursor','编程','代码审查,代码质量,最佳实践',1,2,56,178,2801,'2026-03-01 08:25:39'),(3,'英中互译专业润色','你是一名拥有15年经验的专业翻译，精通英中双语互译。请翻译以下内容，要求：\n1. 准确传达原文含义，不遗漏任何信息\n2. 符合目标语言的表达习惯，避免翻译腔\n3. 专业术语保持一致，首次出现时注明原文\n4. 如有歧义，用括号标注多种可能的理解\n5. 最后给出一个更地道的意译版本供参考\n请先翻译，再在下方给出润色版本并说明润色理由。\n原文：[填入要翻译的内容]','ChatGPT,Claude,DeepSeek','翻译','翻译,润色,双语',2,2,34,112,1900,'2026-03-01 08:25:39'),(4,'职场邮件万能模板','你是一名职场沟通专家。请根据以下信息，帮我撰写一封专业的职场邮件：\n- 邮件类型：[如：申请加薪/拒绝需求/跟进项目/感谢合作]\n- 收件人：[姓名及职位]\n- 核心诉求：[一句话描述你想传达什么]\n- 背景信息：[可选，补充相关背景]\n要求：\n1. 主题行简洁有力，突出核心信息\n2. 开头礼貌破冰，不超过2行\n3. 正文逻辑清晰，重点突出\n4. 结尾明确下一步行动或期望回复\n5. 语气专业但不失温度','ChatGPT,Claude,Gemini','职场','职场,邮件,沟通',1,2,45,156,2400,'2026-03-01 08:25:39'),(5,'数据分析报告一键生成','你是一名资深数据分析师，请根据我提供的数据，生成一份专业的分析报告，包含：\n1. **执行摘要**（3-5行，非技术人员也能读懂的结论）\n2. **数据概览**（关键指标及同比/环比变化）\n3. **深度分析**（找出数据中最重要的3个洞察，每条附数据支撑）\n4. **问题诊断**（识别异常值和潜在风险）\n5. **行动建议**（基于数据给出3条具体可执行的建议）\n6. **可视化建议**（推荐用什么图表展示哪些数据）\n数据：[粘贴数据或描述数据情况]','ChatGPT,Claude,Gemini','分析','数据分析,报告,商业分析',1,2,23,89,1500,'2026-03-01 08:25:39'),(6,'角色扮演：苏格拉底式辩论导师','你现在是苏格拉底，一位以提问见长的哲学导师。你不会直接给出答案，而是通过一系列引导性问题帮助我自己找到答案。\n对话规则：\n1. 每次回复只提1-2个问题，让我有时间深入思考\n2. 用我的回答来设计下一个更深层的问题\n3. 当我的思路出现矛盾时，温和地指出并引导我反思\n4. 在对话接近尾声时，帮我总结自己推导出的结论\n5. 你的语气智慧而温和，充满好奇心\n今天我想探讨的问题是：[填入你想深入思考的问题]','ChatGPT,Claude','教育','哲学,苏格拉底,深度思考',2,2,67,198,3101,'2026-03-01 08:25:39'),(7,'这里','这里',NULL,'写作',NULL,1,1,0,0,0,'2026-03-01 11:33:46'),(8,'sad sad','asdasd',NULL,'写作',NULL,3,1,0,0,0,'2026-03-01 21:52:53');
/*!40000 ALTER TABLE `t_skill` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_skill_collect`
--

DROP TABLE IF EXISTS `t_skill_collect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_skill_collect` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `skill_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_skill_user` (`skill_id`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能收藏记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_skill_collect`
--

LOCK TABLES `t_skill_collect` WRITE;
/*!40000 ALTER TABLE `t_skill_collect` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_skill_collect` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_skill_like`
--

DROP TABLE IF EXISTS `t_skill_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_skill_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `skill_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_skill_user` (`skill_id`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能点赞记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_skill_like`
--

LOCK TABLES `t_skill_like` WRITE;
/*!40000 ALTER TABLE `t_skill_like` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_skill_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_task`
--

DROP TABLE IF EXISTS `t_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
  `task_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务类型标识',
  `frequency` tinyint NOT NULL COMMENT '1每日 2每周 3一次性',
  `points_reward` int NOT NULL COMMENT '完成奖励积分',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1启用 2停用',
  `sort` int NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `task_type` (`task_type`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_task`
--

LOCK TABLES `t_task` WRITE;
/*!40000 ALTER TABLE `t_task` DISABLE KEYS */;
INSERT INTO `t_task` VALUES (1,'每日签到','DAILY_CHECKIN',1,10,1,1,'2026-02-28 22:20:59'),(2,'首次提交邀请码','FIRST_SUBMIT_CODE',3,20,1,2,'2026-02-28 22:20:59'),(3,'累计提交5个邀请码','SUBMIT_CODE_5_TIMES',3,50,1,3,'2026-02-28 22:20:59'),(4,'邀请好友注册','INVITE_USER',2,50,1,4,'2026-02-28 22:20:59'),(5,'完善个人昵称','SET_NICKNAME',3,10,1,10,'2026-03-01 19:34:01'),(6,'首次发表评论','FIRST_COMMENT',3,15,1,11,'2026-03-01 19:34:01'),(7,'首次分享内容','FIRST_SHARE',3,20,1,12,'2026-03-01 19:34:01'),(8,'首次提交提示词','FIRST_SUBMIT_PROMPT',3,20,1,13,'2026-03-01 19:34:01'),(9,'邀请第一位好友','INVITE_FIRST_USER',3,50,1,14,'2026-03-01 19:34:01'),(10,'连续签到7天','CHECKIN_7_STREAK',3,30,1,20,'2026-03-01 19:34:01'),(11,'累计邀请5位好友','INVITE_5_USERS',3,100,1,21,'2026-03-01 19:34:01');
/*!40000 ALTER TABLE `t_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_task_record`
--

DROP TABLE IF EXISTS `t_task_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_task_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `task_id` bigint NOT NULL,
  `task_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_task_date` (`user_id`,`task_type`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务完成记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_task_record`
--

LOCK TABLES `t_task_record` WRITE;
/*!40000 ALTER TABLE `t_task_record` DISABLE KEYS */;
INSERT INTO `t_task_record` VALUES (1,1,2,'FIRST_SUBMIT_CODE','2026-02-28 22:25:40'),(2,2,2,'FIRST_SUBMIT_CODE','2026-02-28 22:27:49');
/*!40000 ALTER TABLE `t_task_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_user`
--

DROP TABLE IF EXISTS `t_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮箱（登录账号）',
  `password` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'BCrypt 加密密码',
  `nickname` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像 URL',
  `points` int NOT NULL DEFAULT '0' COMMENT '当前积分余额',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1正常 2封禁',
  `invite_code` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户推广邀请码',
  `inviter_id` bigint DEFAULT NULL COMMENT '邀请人用户 ID',
  `reg_ip` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '注册 IP',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `role` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '1普通用户 2编辑员 9超级管理员',
  `user_no` int unsigned DEFAULT NULL COMMENT '注册序号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `invite_code` (`invite_code`),
  UNIQUE KEY `user_no` (`user_no`),
  KEY `idx_email` (`email`),
  KEY `idx_invite_code` (`invite_code`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_user`
--

LOCK TABLES `t_user` WRITE;
/*!40000 ALTER TABLE `t_user` DISABLE KEYS */;
INSERT INTO `t_user` VALUES (1,'admin@test.com','$2b$10$Icy/LBZdzX4AVGKUVeFAMOmyF6TFb9tEoledU77OXWSB8JNWnezLK','admin',NULL,550,1,'ADMINCODE',NULL,'127.0.0.1','2026-02-26 22:24:15','2026-02-28 22:27:36',1,NULL),(2,'test@test.com','$2b$10$50Jo3FUxvbM/GCEVRNdr8OburXED7DIwVvCr.E7jTuLhDUyxVUx1.','tester',NULL,110,1,'TESTCODE',NULL,'127.0.0.1','2026-02-26 22:26:18','2026-03-03 09:18:13',1,NULL),(3,'oker@test.com','$2b$10$teJ6v/a0PJpCJA29QGsCf.jBAsL1Y52sN0zpvv0nH7.9jf8kfBh36','管理员',NULL,100,1,NULL,NULL,NULL,'2026-03-01 20:11:08','2026-03-06 09:18:59',9,NULL),(4,'2649384286@qq.com','$2a$10$kLynbRgcw1SrDy5XbCGv/Ot5kLENKarYp57WZRtyiudgV7muo/AP6','2649384286',NULL,110,1,'ZY8PA0V1',NULL,'0:0:0:0:0:0:0:1','2026-03-03 07:54:14','2026-03-03 09:27:09',1,NULL);
/*!40000 ALTER TABLE `t_user` ENABLE KEYS */;
UNLOCK TABLES;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-06  9:50:52
