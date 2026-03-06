package com.aicode.module.news.mapper;

import com.aicode.module.news.entity.News;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NewsMapper extends BaseMapper<News> {

    @Update("UPDATE t_news SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(Long id);

    @Update("UPDATE t_news SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(Long id);

    @Update("UPDATE t_news SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{id}")
    void decrementLikeCount(Long id);
}
