package com.aicode.module.user.mapper;

import com.aicode.module.user.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /** 悲观锁：查看邀请码事务中锁定用户行 */
    @Select("SELECT * FROM t_user WHERE id = #{id} FOR UPDATE")
    User selectForUpdate(@Param("id") Long id);

    /**
     * 安全变更积分：SQL 层防止积分为负
     * delta 为正表示增加，为负表示扣减
     * @return 受影响行数，0 表示积分不足
     */
    @Update("UPDATE t_user SET points = points + #{delta} WHERE id = #{userId} AND points + #{delta} >= 0")
    int addPoints(@Param("userId") Long userId, @Param("delta") int delta);
}
