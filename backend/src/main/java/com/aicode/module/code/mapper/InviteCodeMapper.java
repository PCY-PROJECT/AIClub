package com.aicode.module.code.mapper;

import com.aicode.module.code.entity.InviteCode;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InviteCodeMapper extends BaseMapper<InviteCode> {

    /** 悲观锁：锁定指定ID行 */
    @Select("SELECT * FROM t_invite_code WHERE id = #{id} FOR UPDATE")
    InviteCode selectForUpdate(@Param("id") Long id);

    /**
     * 悲观锁：从池中随机选取一条可用邀请码（status=1，非自己贡献）
     * 使用 LIMIT 1 + FOR UPDATE 保证并发安全
     */
    @Select("SELECT * FROM t_invite_code " +
            "WHERE product_id = #{productId} AND status = 1 AND provider_id != #{userId} " +
            "ORDER BY RAND() LIMIT 1 FOR UPDATE")
    InviteCode selectForClaimForUpdate(@Param("productId") Long productId, @Param("userId") Long userId);
}
