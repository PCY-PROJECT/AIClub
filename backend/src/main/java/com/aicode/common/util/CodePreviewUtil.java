package com.aicode.common.util;

/**
 * 邀请码模糊预览生成工具
 * ≤4位：全部遮挡；5-8位：显示前2位；>8位：显示前3位
 */
public class CodePreviewUtil {

    private CodePreviewUtil() {}

    public static String generate(String code) {
        if (code == null || code.isEmpty()) return "****";
        int len = code.length();
        if (len <= 4) return "*".repeat(len);
        int showLen = len <= 8 ? 2 : 3;
        return code.substring(0, showLen) + "*".repeat(len - showLen);
    }
}
