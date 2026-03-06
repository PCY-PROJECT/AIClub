<template>
  <div class="admin-page page-container">

    <!-- 页头 -->
    <div class="admin-header">
      <div class="admin-header-left">
        <h1 class="admin-title">后台管理</h1>
        <span class="admin-badge">超级管理员</span>
      </div>
    </div>

    <!-- Tab 容器 -->
    <div class="admin-body">
      <el-tabs v-model="activeTab" class="admin-tabs">

        <!-- ══════════ 邀请码 ══════════ -->
        <el-tab-pane label="邀请码" name="codes">
          <div class="filter-bar">
            <div class="filter-item">
              <span class="filter-label">产品</span>
              <el-select v-model="codeFilter.productId" placeholder="全部" clearable size="small" style="width:140px">
                <el-option v-for="p in productOptions" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </div>
            <div class="filter-item">
              <span class="filter-label">状态</span>
              <el-select v-model="codeFilter.status" placeholder="全部" clearable size="small" style="width:120px">
                <el-option label="待审核" :value="5" />
                <el-option label="池中可用" :value="1" />
                <el-option label="已分配" :value="2" />
                <el-option label="已确认有效" :value="3" />
                <el-option label="已确认无效" :value="4" />
                <el-option label="审核拒绝" :value="6" />
              </el-select>
            </div>
            <el-button size="small" type="primary" @click="loadCodes(1)">查询</el-button>
            <el-button size="small" @click="resetFilter('codes')">重置</el-button>
          </div>
          <el-table :data="codeList" size="small" class="admin-table" v-loading="codeLoading">
            <el-table-column label="ID" width="70" prop="id" />
            <el-table-column label="产品" width="120" prop="productName" />
            <el-table-column label="提交人" width="90" prop="providerNickname" />
            <el-table-column label="预览" width="110" prop="codePreview">
              <template #default="{ row }">
                <span class="code-preview">{{ row.codePreview }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="codeStatusType(row.status)" size="small">
                  {{ codeStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="获取者" width="90" prop="claimantNickname">
              <template #default="{ row }">
                <span>{{ row.claimantNickname || '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="获取时间" width="140">
              <template #default="{ row }">
                <span class="time-text">{{ row.claimTime ? fmtTime(row.claimTime) : '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="确认截止" width="140">
              <template #default="{ row }">
                <span class="time-text">{{ row.confirmDeadline ? fmtTime(row.confirmDeadline) : '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="确认结果" width="90">
              <template #default="{ row }">
                <el-tag v-if="row.confirmResult === 1" type="success" size="small">有效</el-tag>
                <el-tag v-else-if="row.confirmResult === 2" type="danger" size="small">无效</el-tag>
                <span v-else class="table-empty">—</span>
              </template>
            </el-table-column>
            <el-table-column label="提交时间" width="140">
              <template #default="{ row }">
                <span class="time-text">{{ fmtTime(row.createTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="110">
              <template #default="{ row }">
                <template v-if="row.status === 5">
                  <el-button size="small" type="success" text @click="doApproveCode(row)">通过</el-button>
                  <el-button size="small" type="danger" text @click="doRejectCode(row)">拒绝</el-button>
                </template>
                <span v-else class="table-empty">—</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination
              v-if="codeTotal > 20"
              v-model:current-page="codePage"
              :page-size="20" :total="codeTotal"
              layout="prev, pager, next"
              @current-change="loadCodes"
            />
          </div>
        </el-tab-pane>

        <!-- ══════════ 知识库 ══════════ -->
        <el-tab-pane label="知识库" name="resources">
          <div class="filter-bar">
            <div class="filter-item">
              <span class="filter-label">分类</span>
              <el-select v-model="resFilter.category" placeholder="全部" clearable size="small" style="width:120px">
                <template v-for="(label, val) in ResourceCategoryMap" :key="val">
                  <el-option v-if="Number(val) > 0" :label="label" :value="Number(val)" />
                </template>
              </el-select>
            </div>
            <div class="filter-item">
              <span class="filter-label">状态</span>
              <el-select v-model="resFilter.status" placeholder="全部" clearable size="small" style="width:110px">
                <el-option label="待审核" :value="1" />
                <el-option label="已发布" :value="2" />
                <el-option label="已拒绝" :value="3" />
                <el-option label="已下架" :value="4" />
              </el-select>
            </div>
            <el-button size="small" type="primary" @click="loadResources(1)">查询</el-button>
            <el-button size="small" @click="resetFilter('resources')">重置</el-button>
            <div class="toolbar-spacer" />
          </div>
          <el-table :data="resList" size="small" class="admin-table" v-loading="resLoading">
            <el-table-column label="标题" min-width="200">
              <template #default="{ row }">
                <a :href="row.sourceUrl" target="_blank" class="table-link">{{ row.title }}</a>
              </template>
            </el-table-column>
            <el-table-column label="分类" width="90" prop="categoryName" />
            <el-table-column label="难度" width="70" prop="difficultyName" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">
                  {{ statusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <template v-if="row.status === 1">
                  <el-button size="small" type="success" text @click="approveRes(row.id)">通过</el-button>
                  <el-button size="small" type="danger" text @click="rejectRes(row.id)">拒绝</el-button>
                </template>
                <template v-else-if="row.status === 2">
                  <el-button size="small" type="warning" text @click="offlineRes(row.id)">下架</el-button>
                </template>
                <span v-else class="table-empty">—</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination v-if="resTotal > 20" v-model:current-page="resPage"
              :page-size="20" :total="resTotal" layout="prev, pager, next"
              @current-change="loadResources" />
          </div>
        </el-tab-pane>

        <!-- ══════════ 提示词 ══════════ -->
        <el-tab-pane label="提示词" name="prompts">
          <div class="filter-bar">
            <div class="filter-item">
              <span class="filter-label">分类</span>
              <el-select v-model="promptFilter.category" placeholder="全部" clearable size="small" style="width:120px">
                <el-option v-for="c in skillCategories" :key="c" :label="c" :value="c" />
              </el-select>
            </div>
            <div class="filter-item">
              <span class="filter-label">状态</span>
              <el-select v-model="promptFilter.status" placeholder="全部" clearable size="small" style="width:110px">
                <el-option label="待审核" :value="1" />
                <el-option label="已发布" :value="2" />
                <el-option label="已拒绝" :value="3" />
              </el-select>
            </div>
            <el-button size="small" type="primary" @click="loadPrompts(1)">查询</el-button>
            <el-button size="small" @click="resetFilter('prompts')">重置</el-button>
          </div>
          <el-table :data="promptList" size="small" class="admin-table" v-loading="promptLoading">
            <el-table-column label="标题" min-width="200" prop="title" />
            <el-table-column label="分类" width="90" prop="category" />
            <el-table-column label="作者" width="100" prop="authorNickname" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="130">
              <template #default="{ row }">
                <template v-if="row.status === 1">
                  <el-button size="small" type="success" text @click="approvePrompt(row.id)">通过</el-button>
                  <el-button size="small" type="danger" text @click="rejectPrompt(row.id)">拒绝</el-button>
                </template>
                <span v-else class="table-empty">—</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination v-if="promptTotal > 20" v-model:current-page="promptPage"
              :page-size="20" :total="promptTotal" layout="prev, pager, next"
              @current-change="loadPrompts" />
          </div>
        </el-tab-pane>

        <!-- ══════════ Agent技能库 ══════════ -->
        <el-tab-pane label="Agent技能库" name="mcp">
          <div class="filter-bar">
            <div class="filter-item">
              <span class="filter-label">类型</span>
              <el-select v-model="mcpFilter.type" placeholder="全部" clearable size="small" style="width:130px">
                <el-option label="MCP Server" :value="1" />
                <el-option label="Agent Skill" :value="2" />
              </el-select>
            </div>
            <div class="filter-item">
              <span class="filter-label">状态</span>
              <el-select v-model="mcpFilter.status" placeholder="全部" clearable size="small" style="width:110px">
                <el-option label="已上架" :value="2" />
                <el-option label="已下架" :value="3" />
              </el-select>
            </div>
            <el-button size="small" type="primary" @click="loadMcpList(1)">查询</el-button>
            <el-button size="small" @click="resetFilter('mcp')">重置</el-button>
            <div class="toolbar-spacer" />
            <el-button size="small" type="primary" @click="openMcpForm()">+ 新增</el-button>
          </div>
          <el-table :data="mcpList" size="small" class="admin-table" v-loading="mcpLoading">
            <el-table-column label="名称" min-width="150" prop="name" />
            <el-table-column label="类型" width="110" prop="typeName" />
            <el-table-column label="分类" width="100" prop="category" />
            <el-table-column label="开发者" width="110" prop="vendor" />
            <el-table-column label="排序" width="70" prop="sort" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 2 ? 'success' : 'info'" size="small">
                  {{ row.status === 2 ? '已上架' : '已下架' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button size="small" text @click="editMcp(row)">编辑</el-button>
                <el-button size="small" text :type="row.status === 2 ? 'warning' : 'success'"
                  @click="toggleMcpStatus(row)">{{ row.status === 2 ? '下架' : '上架' }}</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination v-if="mcpTotal > 20" v-model:current-page="mcpPage"
              :page-size="20" :total="mcpTotal" layout="prev, pager, next"
              @current-change="loadMcpList" />
          </div>
        </el-tab-pane>

        <!-- ══════════ 资讯 ══════════ -->
        <el-tab-pane label="资讯" name="news">
          <div class="filter-bar">
            <div class="filter-item">
              <span class="filter-label">分类</span>
              <el-select v-model="newsFilter.category" placeholder="全部" clearable size="small" style="width:120px">
                <template v-for="(label, val) in NewsCategoryMap" :key="val">
                  <el-option v-if="Number(val) > 0" :label="label" :value="Number(val)" />
                </template>
              </el-select>
            </div>
            <div class="filter-item">
              <span class="filter-label">状态</span>
              <el-select v-model="newsFilter.status" placeholder="全部" clearable size="small" style="width:110px">
                <el-option label="待审核" :value="1" />
                <el-option label="已发布" :value="2" />
                <el-option label="已拒绝" :value="3" />
                <el-option label="已下线" :value="4" />
              </el-select>
            </div>
            <el-button size="small" type="primary" @click="loadNewsList(1)">查询</el-button>
            <el-button size="small" @click="resetFilter('news')">重置</el-button>
          </div>
          <el-table :data="newsList" size="small" class="admin-table" v-loading="newsLoading">
            <el-table-column label="标题" min-width="220" prop="title" />
            <el-table-column label="分类" width="90" prop="categoryName" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="newsStatusType(row.status)" size="small">{{ newsStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <template v-if="row.status === 1">
                  <el-button size="small" type="success" text @click="approveNewsItem(row.id)">通过</el-button>
                  <el-button size="small" type="danger" text @click="rejectNewsItem(row.id)">拒绝</el-button>
                </template>
                <template v-else-if="row.status === 2">
                  <el-button size="small" type="warning" text @click="offlineNewsItem(row.id)">下线</el-button>
                </template>
                <span v-else class="table-empty">—</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination v-if="newsTotal > 20" v-model:current-page="newsPage"
              :page-size="20" :total="newsTotal" layout="prev, pager, next"
              @current-change="loadNewsList" />
          </div>
        </el-tab-pane>

        <!-- ══════════ 产品 ══════════ -->
        <el-tab-pane label="产品" name="products">
          <div class="filter-bar">
            <div class="filter-item">
              <span class="filter-label">状态</span>
              <el-select v-model="productFilter.status" placeholder="全部" clearable size="small" style="width:110px">
                <el-option label="上线" :value="1" />
                <el-option label="下线" :value="2" />
              </el-select>
            </div>
            <el-button size="small" type="primary" @click="loadProducts(1)">查询</el-button>
            <el-button size="small" @click="resetFilter('products')">重置</el-button>
            <div class="toolbar-spacer" />
            <el-button size="small" type="primary" @click="openProductForm()">+ 新增</el-button>
          </div>
          <el-table :data="productList" size="small" class="admin-table" v-loading="productLoading">
            <el-table-column label="ID" width="70" prop="id" />
            <el-table-column label="产品名称" min-width="130" prop="name" />
            <el-table-column label="分类" width="90">
              <template #default="{ row }">{{ CategoryMap[row.category] ?? '其他' }}</template>
            </el-table-column>
            <el-table-column label="官网" min-width="160">
              <template #default="{ row }">
                <a v-if="row.officialUrl" :href="row.officialUrl" target="_blank" class="table-link">{{ row.officialUrl }}</a>
                <span v-else class="table-empty">—</span>
              </template>
            </el-table-column>
            <el-table-column label="邀请码数" width="80" prop="codeCount" />
            <el-table-column label="排序" width="70" prop="sort" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '上线' : '下线' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button size="small" text @click="editProduct(row)">编辑</el-button>
                <el-button size="small" text :type="row.status === 1 ? 'warning' : 'success'"
                  @click="toggleProductStatus(row)">{{ row.status === 1 ? '下线' : '上线' }}</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination v-if="productTotal > 20" v-model:current-page="productPage"
              :page-size="20" :total="productTotal" layout="prev, pager, next"
              @current-change="loadProducts" />
          </div>
        </el-tab-pane>

        <!-- ══════════ 网站导航 ══════════ -->
        <el-tab-pane label="网站导航" name="sites">
          <div class="filter-bar">
            <div class="filter-item">
              <span class="filter-label">状态</span>
              <el-select v-model="siteFilter.status" placeholder="全部" clearable size="small" style="width:110px">
                <el-option label="待审核" :value="1" />
                <el-option label="已发布" :value="2" />
                <el-option label="已拒绝" :value="3" />
              </el-select>
            </div>
            <el-button size="small" type="primary" @click="loadSites(1)">查询</el-button>
            <el-button size="small" @click="resetFilter('sites')">重置</el-button>
          </div>
          <el-table :data="siteList" size="small" class="admin-table" v-loading="siteLoading">
            <el-table-column label="网站名称" min-width="120" prop="name" />
            <el-table-column label="链接" min-width="180">
              <template #default="{ row }">
                <a :href="row.url" target="_blank" class="table-link">{{ row.url }}</a>
              </template>
            </el-table-column>
            <el-table-column label="分类" width="90" prop="category" />
            <el-table-column label="访问量" width="80" prop="viewCount" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 2 ? 'success' : row.status === 1 ? 'warning' : 'danger'" size="small">
                  {{ row.status === 2 ? '已发布' : row.status === 1 ? '待审核' : '已拒绝' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="提交时间" width="140">
              <template #default="{ row }">
                <span class="time-text">{{ fmtTime(row.createTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <template v-if="row.status === 1">
                  <el-button size="small" type="success" text @click="approveSiteItem(row.id)">通过</el-button>
                  <el-button size="small" type="danger" text @click="rejectSiteItem(row.id)">拒绝</el-button>
                </template>
                <el-button size="small" type="danger" text @click="deleteSiteItem(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination v-if="siteTotal > 20" v-model:current-page="sitePage"
              :page-size="20" :total="siteTotal" layout="prev, pager, next"
              @current-change="loadSites" />
          </div>
        </el-tab-pane>

        <!-- ══════════ 用户 ══════════ -->
        <el-tab-pane label="用户" name="users">
          <div class="filter-bar">
            <div class="filter-item">
              <span class="filter-label">状态</span>
              <el-select v-model="userFilter.status" placeholder="全部" clearable size="small" style="width:110px">
                <el-option label="正常" :value="1" />
                <el-option label="封禁" :value="2" />
              </el-select>
            </div>
            <el-button size="small" type="primary" @click="loadUsers(1)">查询</el-button>
            <el-button size="small" @click="resetFilter('users')">重置</el-button>
          </div>
          <el-table :data="userList" size="small" class="admin-table" v-loading="userLoading">
            <el-table-column label="ID" width="70" prop="id" />
            <el-table-column label="邮箱" min-width="160" prop="email" />
            <el-table-column label="昵称" width="100" prop="nickname" />
            <el-table-column label="积分" width="80" prop="points" />
            <el-table-column label="No." width="70" prop="userNo" />
            <el-table-column label="角色" width="90">
              <template #default="{ row }">
                <el-tag :type="row.role === 9 ? 'danger' : row.role === 2 ? 'warning' : 'info'" size="small">
                  {{ row.role === 9 ? '管理员' : row.role === 2 ? '编辑员' : '用户' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '正常' : '封禁' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="注册时间" width="140">
              <template #default="{ row }">
                <span class="time-text">{{ fmtTime(row.createTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="130">
              <template #default="{ row }">
                <el-button size="small" text @click="adjustUser = row; adjustDelta = 0; adjustRemark = ''; adjustDialogVisible = true">调积分</el-button>
                <el-button size="small" text :type="row.status === 1 ? 'warning' : 'success'"
                  @click="toggleUserStatus(row)">{{ row.status === 1 ? '封禁' : '解封' }}</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination v-if="userTotal > 20" v-model:current-page="userPage"
              :page-size="20" :total="userTotal" layout="prev, pager, next"
              @current-change="loadUsers" />
          </div>
        </el-tab-pane>

      </el-tabs>
    </div>

    <!-- MCP 新增/编辑 Dialog -->
    <el-dialog v-model="showMcpForm" :title="mcpFormData.id ? '编辑 MCP 条目' : '新增 MCP 条目'" width="560px">
      <el-form :model="mcpFormData" label-position="top" size="small">
        <div class="form-row">
          <el-form-item label="名称 *" style="flex:1">
            <el-input v-model="mcpFormData.name" />
          </el-form-item>
          <el-form-item label="类型 *" style="width:140px">
            <el-select v-model="mcpFormData.type" style="width:100%">
              <el-option label="MCP Server" :value="1" />
              <el-option label="Agent Skill" :value="2" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="分类" style="flex:1">
            <el-input v-model="mcpFormData.category" placeholder="如：文件管理、浏览器" />
          </el-form-item>
          <el-form-item label="开发者/组织" style="flex:1">
            <el-input v-model="mcpFormData.vendor" />
          </el-form-item>
        </div>
        <el-form-item label="功能描述 * （面向普通用户）">
          <el-input v-model="mcpFormData.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="源码地址">
          <el-input v-model="mcpFormData.sourceUrl" placeholder="https://github.com/..." />
        </el-form-item>
        <el-form-item label="安装/使用指南 (Markdown)">
          <el-input v-model="mcpFormData.installGuide" type="textarea" :rows="4" />
        </el-form-item>
        <div class="form-row">
          <el-form-item label="标签（逗号分隔）" style="flex:1">
            <el-input v-model="mcpFormData.tags" />
          </el-form-item>
          <el-form-item label="排序权重" style="width:130px">
            <el-input-number v-model="mcpFormData.sort" :min="0" style="width:100%" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showMcpForm = false">取消</el-button>
        <el-button type="primary" :loading="mcpSaving" @click="saveMcp">保存</el-button>
      </template>
    </el-dialog>

    <!-- 产品新增/编辑 Dialog -->
    <el-dialog v-model="showProductForm" :title="productFormData.id ? '编辑产品' : '新增产品'" width="500px">
      <el-form :model="productFormData" label-position="top" size="small">
        <div class="form-row">
          <el-form-item label="产品名称 *" style="flex:1">
            <el-input v-model="productFormData.name" placeholder="如：Claude" />
          </el-form-item>
          <el-form-item label="分类 *" style="width:140px">
            <el-select v-model="productFormData.category" style="width:100%">
              <el-option v-for="(label, val) in CategoryMap" :key="val" :label="label" :value="Number(val)" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="官网地址">
          <el-input v-model="productFormData.officialUrl" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="Logo URL">
          <el-input v-model="productFormData.logo" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="产品描述">
          <el-input v-model="productFormData.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="排序权重">
          <el-input-number v-model="productFormData.sort" :min="0" style="width:120px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showProductForm = false">取消</el-button>
        <el-button type="primary" :loading="productSaving" @click="saveProduct">保存</el-button>
      </template>
    </el-dialog>

    <!-- 积分调整 Dialog -->
    <el-dialog v-model="adjustDialogVisible" title="积分调整" width="380px">
      <el-form label-position="top" size="small">
        <el-form-item :label="`用户：${adjustUser?.nickname || adjustUser?.email}`">
          <el-input-number v-model="adjustDelta" :min="-9999" :max="9999" style="width:100%" />
          <div class="form-hint">正数增加，负数扣减</div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="adjustRemark" placeholder="如：活动补偿积分" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="doAdjustPoints">确认</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminListResources, adminApproveResource, adminRejectResource, adminOfflineResource,
  adminListPrompts, adminApprovePrompt, adminRejectPrompt,
  adminListMcp, adminCreateMcp, adminUpdateMcp, adminUpdateMcpStatus,
  adminListNews, adminApproveNews, adminRejectNews, adminOfflineNews,
  adminListUsers, adminAdjustPoints, adminUpdateUserStatus,
  adminListCodes, adminApproveCode, adminRejectCode,
  adminListProducts, adminCreateProduct, adminUpdateProduct, adminUpdateProductStatus,
} from '@/api/admin'
import { adminListSites, adminApproveSite, adminRejectSite, adminDeleteSite } from '@/api/site'
import { getProducts } from '@/api/product'
import { ResourceCategoryMap, NewsCategoryMap, CategoryMap } from '@/types'
import type { ResourceItem, SkillItem, McpSkillItem, NewsItem } from '@/types'

const skillCategories = ['写作', '编程', '设计', '营销', '分析', '翻译', '教育', '职场', '生活']

// ─── 产品选项（用于邀请码筛选）──────────────────────────────
const productOptions = ref<{ id: number; name: string }[]>([])
async function loadProductOptions() {
  try {
    const list = await getProducts()
    productOptions.value = list.map((p: any) => ({ id: p.id, name: p.name }))
  } catch { /* ignore */ }
}

const activeTab = ref('codes')

// ─── 重置筛选 ───────────────────────────────────────────────
function resetFilter(tab: string) {
  if (tab === 'codes') { Object.assign(codeFilter, { productId: undefined, status: undefined }); loadCodes(1) }
  if (tab === 'resources') { Object.assign(resFilter, { category: undefined, status: undefined }); loadResources(1) }
  if (tab === 'prompts') { Object.assign(promptFilter, { category: undefined, status: undefined }); loadPrompts(1) }
  if (tab === 'mcp') { Object.assign(mcpFilter, { type: undefined, status: undefined }); loadMcpList(1) }
  if (tab === 'news') { Object.assign(newsFilter, { category: undefined, status: undefined }); loadNewsList(1) }
  if (tab === 'users') { Object.assign(userFilter, { status: undefined }); loadUsers(1) }
  if (tab === 'products') { Object.assign(productFilter, { status: undefined }); loadProducts(1) }
  if (tab === 'sites') { Object.assign(siteFilter, { status: undefined }); loadSites(1) }
}

// ─── 邀请码 ────────────────────────────────────────────────
const codeList = ref<any[]>([])
const codeLoading = ref(false)
const codeTotal = ref(0)
const codePage = ref(1)
const codeFilter = reactive({ productId: undefined as number | undefined, status: undefined as number | undefined })

async function loadCodes(page = codePage.value) {
  codePage.value = page
  codeLoading.value = true
  try {
    const r = await adminListCodes({ ...codeFilter, page, size: 20 })
    codeList.value = r.records; codeTotal.value = r.total
  } finally { codeLoading.value = false }
}
async function doApproveCode(row: any) {
  await ElMessageBox.confirm(`确认通过邀请码「${row.codePreview}」？通过后将进入池中可用状态。`, '审核通过', { type: 'info' })
  await adminApproveCode(row.id)
  ElMessage.success('已通过，邀请码已入池')
  loadCodes()
}
async function doRejectCode(row: any) {
  await ElMessageBox.confirm(`确认拒绝邀请码「${row.codePreview}」？`, '审核拒绝', { type: 'warning' })
  await adminRejectCode(row.id)
  ElMessage.success('已拒绝')
  loadCodes()
}

// ─── 知识库 ────────────────────────────────────────────────
const resList = ref<ResourceItem[]>([])
const resLoading = ref(false)
const resTotal = ref(0)
const resPage = ref(1)
const resFilter = reactive({ category: undefined as number | undefined, status: undefined as number | undefined })

async function loadResources(page = resPage.value) {
  resPage.value = page
  resLoading.value = true
  try {
    const r = await adminListResources({ ...resFilter, page, size: 20 })
    resList.value = r.records; resTotal.value = r.total
  } finally { resLoading.value = false }
}
async function approveRes(id: number) {
  await adminApproveResource(id); ElMessage.success('已通过'); loadResources()
}
async function rejectRes(id: number) {
  await adminRejectResource(id); ElMessage.success('已拒绝'); loadResources()
}
async function offlineRes(id: number) {
  await ElMessageBox.confirm('确认将该知识库文章下架？下架后前台不再展示。', '下架确认', { type: 'warning' })
  await adminOfflineResource(id)
  ElMessage.success('已下架')
  loadResources()
}

// ─── 提示词 ────────────────────────────────────────────────
const promptList = ref<SkillItem[]>([])
const promptLoading = ref(false)
const promptTotal = ref(0)
const promptPage = ref(1)
const promptFilter = reactive({ category: undefined as string | undefined, status: undefined as number | undefined })

async function loadPrompts(page = promptPage.value) {
  promptPage.value = page
  promptLoading.value = true
  try {
    const r = await adminListPrompts({ ...promptFilter, page, size: 20 })
    promptList.value = r.records; promptTotal.value = r.total
  } finally { promptLoading.value = false }
}
async function approvePrompt(id: number) {
  await adminApprovePrompt(id); ElMessage.success('已通过'); loadPrompts()
}
async function rejectPrompt(id: number) {
  await adminRejectPrompt(id); ElMessage.success('已拒绝'); loadPrompts()
}

// ─── Agent技能库 ───────────────────────────────────────────
const mcpList = ref<McpSkillItem[]>([])
const mcpLoading = ref(false)
const mcpTotal = ref(0)
const mcpPage = ref(1)
const mcpFilter = reactive({ type: undefined as number | undefined, status: undefined as number | undefined })
const showMcpForm = ref(false)
const mcpSaving = ref(false)
const mcpFormData = reactive({
  id: undefined as number | undefined,
  name: '', description: '', type: undefined as number | undefined,
  category: '', sourceUrl: '', installGuide: '', vendor: '', tags: '', sort: 0,
})

async function loadMcpList(page = mcpPage.value) {
  mcpPage.value = page
  mcpLoading.value = true
  try {
    const r = await adminListMcp({ ...mcpFilter, page, size: 20 })
    mcpList.value = r.records; mcpTotal.value = r.total
  } finally { mcpLoading.value = false }
}
function openMcpForm() {
  Object.assign(mcpFormData, { id: undefined, name: '', description: '', type: undefined,
    category: '', sourceUrl: '', installGuide: '', vendor: '', tags: '', sort: 0 })
  showMcpForm.value = true
}
function editMcp(item: McpSkillItem) {
  Object.assign(mcpFormData, { id: item.id, name: item.name, description: item.description,
    type: item.type, category: item.category ?? '', sourceUrl: item.sourceUrl ?? '',
    installGuide: item.installGuide ?? '', vendor: item.vendor ?? '',
    tags: item.tags ?? '', sort: item.sort })
  showMcpForm.value = true
}
async function saveMcp() {
  if (!mcpFormData.name || !mcpFormData.type) { ElMessage.warning('名称和类型必填'); return }
  mcpSaving.value = true
  try {
    mcpFormData.id ? await adminUpdateMcp(mcpFormData.id, mcpFormData) : await adminCreateMcp(mcpFormData)
    ElMessage.success('保存成功')
    showMcpForm.value = false
    loadMcpList()
  } finally { mcpSaving.value = false }
}
async function toggleMcpStatus(item: McpSkillItem) {
  const newStatus = item.status === 2 ? 3 : 2
  await adminUpdateMcpStatus(item.id, newStatus)
  ElMessage.success(newStatus === 2 ? '已上架' : '已下架')
  loadMcpList()
}

// ─── 产品管理 ──────────────────────────────────────────────
const productList = ref<any[]>([])
const productLoading = ref(false)
const productTotal = ref(0)
const productPage = ref(1)
const productFilter = reactive({ status: undefined as number | undefined })
const showProductForm = ref(false)
const productSaving = ref(false)
const productFormData = reactive({
  id: undefined as number | undefined,
  name: '', description: '', officialUrl: '', logo: '',
  category: undefined as number | undefined, sort: 0,
})

async function loadProducts(page = productPage.value) {
  productPage.value = page
  productLoading.value = true
  try {
    const r = await adminListProducts({ ...productFilter, page, size: 20 })
    productList.value = r.records; productTotal.value = r.total
  } finally { productLoading.value = false }
}
function openProductForm() {
  Object.assign(productFormData, { id: undefined, name: '', description: '', officialUrl: '', logo: '', category: undefined, sort: 0 })
  showProductForm.value = true
}
function editProduct(item: any) {
  Object.assign(productFormData, { id: item.id, name: item.name, description: item.description ?? '',
    officialUrl: item.officialUrl ?? '', logo: item.logo ?? '', category: item.category, sort: item.sort ?? 0 })
  showProductForm.value = true
}
async function saveProduct() {
  if (!productFormData.name) { ElMessage.warning('产品名称必填'); return }
  if (!productFormData.category) { ElMessage.warning('请选择分类'); return }
  productSaving.value = true
  try {
    productFormData.id
      ? await adminUpdateProduct(productFormData.id, productFormData)
      : await adminCreateProduct(productFormData)
    ElMessage.success('保存成功')
    showProductForm.value = false
    loadProducts()
    loadProductOptions()   // 同步邀请码筛选下拉
  } finally { productSaving.value = false }
}
async function toggleProductStatus(item: any) {
  const newStatus = item.status === 1 ? 2 : 1
  await adminUpdateProductStatus(item.id, newStatus)
  ElMessage.success(newStatus === 1 ? '已上线' : '已下线')
  loadProducts()
  loadProductOptions()
}

// ─── 网站导航 ──────────────────────────────────────────────
const siteList = ref<any[]>([])
const siteLoading = ref(false)
const siteTotal = ref(0)
const sitePage = ref(1)
const siteFilter = reactive({ status: undefined as number | undefined })

async function loadSites(page = sitePage.value) {
  sitePage.value = page
  siteLoading.value = true
  try {
    const r = await adminListSites({ ...siteFilter, page, size: 20 })
    siteList.value = r.records; siteTotal.value = r.total
  } finally { siteLoading.value = false }
}
async function approveSiteItem(id: number) {
  await adminApproveSite(id); ElMessage.success('已通过，网站已上线'); loadSites()
}
async function rejectSiteItem(id: number) {
  await adminRejectSite(id); ElMessage.success('已拒绝'); loadSites()
}
async function deleteSiteItem(id: number) {
  await ElMessageBox.confirm('确认删除该网站？此操作不可撤销。', '删除确认', { type: 'warning' })
  await adminDeleteSite(id); ElMessage.success('已删除'); loadSites()
}

// ─── 资讯 ─────────────────────────────────────────────────
const newsList = ref<NewsItem[]>([])
const newsLoading = ref(false)
const newsTotal = ref(0)
const newsPage = ref(1)
const newsFilter = reactive({ category: undefined as number | undefined, status: undefined as number | undefined })

async function loadNewsList(page = newsPage.value) {
  newsPage.value = page
  newsLoading.value = true
  try {
    const r = await adminListNews({ ...newsFilter, page, size: 20 })
    newsList.value = r.records; newsTotal.value = r.total
  } finally { newsLoading.value = false }
}
async function approveNewsItem(id: number) {
  await adminApproveNews(id); ElMessage.success('已通过'); loadNewsList()
}
async function rejectNewsItem(id: number) {
  await adminRejectNews(id); ElMessage.success('已拒绝'); loadNewsList()
}
async function offlineNewsItem(id: number) {
  await ElMessageBox.confirm('确认将该资讯下线？下线后前台不再展示。', '下线确认', { type: 'warning' })
  await adminOfflineNews(id)
  ElMessage.success('已下线')
  loadNewsList()
}

// ─── 用户 ─────────────────────────────────────────────────
const userList = ref<any[]>([])
const userLoading = ref(false)
const userTotal = ref(0)
const userPage = ref(1)
const userFilter = reactive({ status: undefined as number | undefined })
const adjustUser = ref<any>(null)
const adjustDialogVisible = ref(false)
const adjustDelta = ref(0)
const adjustRemark = ref('')

async function loadUsers(page = userPage.value) {
  userPage.value = page
  userLoading.value = true
  try {
    const r = await adminListUsers({ page, size: 20 })
    userList.value = r.records; userTotal.value = r.total
  } finally { userLoading.value = false }
}
async function toggleUserStatus(user: any) {
  const newStatus = user.status === 1 ? 2 : 1
  await adminUpdateUserStatus(user.id, newStatus)
  ElMessage.success(newStatus === 1 ? '已解封' : '已封禁')
  loadUsers()
}
async function doAdjustPoints() {
  if (!adjustUser.value || !adjustDelta.value) { ElMessage.warning('请填写调整数量'); return }
  await adminAdjustPoints(adjustUser.value.id, adjustDelta.value, adjustRemark.value || undefined)
  ElMessage.success('积分已调整')
  adjustDialogVisible.value = false
  loadUsers()
}

// ─── Tab 切换懒加载 ──────────────────────────────────────
watch(activeTab, (tab) => {
  if (tab === 'codes' && !codeList.value.length) loadCodes(1)
  if (tab === 'resources' && !resList.value.length) loadResources(1)
  if (tab === 'prompts' && !promptList.value.length) loadPrompts(1)
  if (tab === 'mcp' && !mcpList.value.length) loadMcpList(1)
  if (tab === 'news' && !newsList.value.length) loadNewsList(1)
  if (tab === 'users' && !userList.value.length) loadUsers(1)
  if (tab === 'products' && !productList.value.length) loadProducts(1)
  if (tab === 'sites' && !siteList.value.length) loadSites(1)
})

// ─── 工具方法 ─────────────────────────────────────────────
function statusType(status: number) {
  if (status === 2) return 'success'
  if (status === 1) return 'warning'
  if (status === 4) return 'info'
  return 'danger'
}
function statusLabel(status: number) {
  const map: Record<number, string> = { 1: '待审核', 2: '已发布', 3: '已拒绝', 4: '已下架' }
  return map[status] ?? '—'
}

// 资讯专用（含 status=4 已下线）
function newsStatusType(status: number) {
  if (status === 2) return 'success'
  if (status === 1) return 'warning'
  if (status === 4) return 'info'
  return 'danger'
}
function newsStatusLabel(status: number) {
  const map: Record<number, string> = { 1: '待审核', 2: '已发布', 3: '已拒绝', 4: '已下线' }
  return map[status] ?? '—'
}

// 邀请码专用状态
function codeStatusType(status: number) {
  const map: Record<number, string> = { 1: 'success', 2: 'warning', 3: 'success', 4: 'danger', 5: 'info', 6: 'danger' }
  return (map[status] as any) ?? 'info'
}
function codeStatusLabel(status: number) {
  const map: Record<number, string> = { 1: '池中可用', 2: '已分配', 3: '已确认有效', 4: '已确认无效', 5: '待审核', 6: '审核拒绝' }
  return map[status] ?? '—'
}

function fmtTime(t: string) {
  if (!t) return '—'
  return t.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  loadProductOptions()
  loadCodes(1)
})
</script>

<style scoped>
.admin-page {
  padding: 28px 0 60px;
}

/* ─── 页头 ─── */
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.admin-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.admin-title {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
}
.admin-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: var(--radius-sm);
  background: rgba(255, 101, 104, 0.15);
  color: #ff6568;
}

/* ─── 主体容器 ─── */
.admin-body {
  background: var(--color-bg-elevated);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 0 20px 20px;
}

/* ─── Tabs 样式覆盖 ─── */
.admin-tabs {
  --el-tabs-header-height: 44px;
}
:deep(.el-tabs__header) {
  margin: 0 0 16px;
  border-bottom: 1px solid var(--color-border);
}
:deep(.el-tabs__nav-wrap::after) {
  display: none;
}
:deep(.el-tabs__item) {
  color: var(--color-text-muted);
  font-size: 13px;
  padding: 0 16px;
}
:deep(.el-tabs__item.is-active) {
  color: var(--color-primary);
}
:deep(.el-tabs__active-bar) {
  background-color: var(--color-primary);
}

/* ─── 筛选栏 ─── */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  padding: 12px 14px;
  background: var(--color-bg-overlay);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  flex-wrap: wrap;
}
.filter-item {
  display: flex;
  align-items: center;
  gap: 6px;
}
.filter-label {
  font-size: 12px;
  color: var(--color-text-muted);
  white-space: nowrap;
  flex-shrink: 0;
}
.toolbar-spacer {
  flex: 1;
}

/* ─── 表格 ─── */
.admin-table {
  border-radius: var(--radius-md);
  overflow: hidden;
}
:deep(.el-table__header-wrapper) {
  border-radius: var(--radius-md) var(--radius-md) 0 0;
}
:deep(.el-table th.el-table__cell) {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.02em;
}
:deep(.el-table td.el-table__cell) {
  font-size: 13px;
  padding: 9px 0;
}
:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: rgba(255,255,255,0.02);
}

.table-link {
  color: var(--color-primary);
  text-decoration: none;
  font-size: 13px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.table-link:hover { text-decoration: underline; }
.table-empty {
  color: var(--color-text-disabled);
  font-size: 12px;
}
.code-preview {
  font-family: monospace;
  font-size: 13px;
  color: var(--color-primary);
  letter-spacing: 0.05em;
}
.time-text {
  font-size: 12px;
  color: var(--color-text-muted);
}

/* ─── 分页 ─── */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

/* ─── Dialog 表单 ─── */
.form-row {
  display: flex;
  gap: 12px;
}
.form-hint {
  font-size: 11px;
  color: var(--color-text-muted);
  margin-top: 4px;
}
</style>
