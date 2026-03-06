package com.aicode.module.resource.mapper;

import com.aicode.module.resource.entity.Resource;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {

    @Update("UPDATE t_resource SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(Long id);

    @Update("UPDATE t_resource SET collect_count = collect_count + 1 WHERE id = #{id}")
    void incrementCollectCount(Long id);

    @Update("UPDATE t_resource SET collect_count = GREATEST(collect_count - 1, 0) WHERE id = #{id}")
    void decrementCollectCount(Long id);
}
