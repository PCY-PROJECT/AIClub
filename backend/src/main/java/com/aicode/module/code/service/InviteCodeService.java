package com.aicode.module.code.service;

import com.aicode.module.code.dto.ContributeCodeRequest;
import com.aicode.module.code.vo.ClaimedCodeVO;
import com.aicode.module.code.vo.CodeItemVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface InviteCodeService {

    /** 贡献邀请码（无冷静期，提交后 status=5 待审核） */
    void contributeCode(Long userId, ContributeCodeRequest req);

    /**
     * 花50积分从池中随机获取一条邀请码（事务+行锁）
     * 返回明文邀请码
     */
    ClaimedCodeVO claimCode(Long userId, Long productId);

    /** 确认邀请码有效性（"valid" | "invalid"） */
    void confirmCode(Long userId, Long codeId, String result);

    /** 我贡献的邀请码列表 */
    Page<CodeItemVO> myContributed(Long userId, int pageNum, int pageSize);

    /** 我获取的邀请码列表 */
    Page<ClaimedCodeVO> myClaimed(Long userId, int pageNum, int pageSize);

    /**
     * 处理超时未确认的邀请码（定时任务调用）
     * 扫描 status=2 且 confirm_deadline < now → 自动执行 valid 逻辑
     */
    void processExpiredConfirmations();
}
