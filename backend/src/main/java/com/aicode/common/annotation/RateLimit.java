package com.aicode.common.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解（内存滑动窗口）
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /** 限流 key 前缀 */
    String key();

    /** 时间窗口内最大请求次数 */
    int limit();

    /** 时间窗口大小（秒） */
    int window();

    /** 限流维度：IP / USER */
    String keySource() default "IP";

    /** 超限时返回的错误信息 */
    String message() default "操作太频繁，请稍后重试";
}
