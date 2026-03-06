package com.aicode.module.news.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LikeResultVO {
    private boolean liked;
    private int likeCount;
}
