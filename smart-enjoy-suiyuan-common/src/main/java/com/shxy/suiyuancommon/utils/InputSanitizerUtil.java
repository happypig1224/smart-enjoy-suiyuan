package com.shxy.suiyuancommon.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * 输入净化工具类
 * 用于防止XSS攻击和SQL注入等安全问题
 * 
 * @author Tech Lead
 */
@Slf4j
public class InputSanitizerUtil {
    
    /**
     * 净化HTML内容，防止XSS攻击
     * 
     * @param input 待净化的输入
     * @return 净化后的安全内容
     */
    public static String sanitizeHtml(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        try {
            // 移除潜在的危险标签
            String sanitized = input
                .replaceAll("(?i)<script[^>]*>[\\s\\S]*?</script>", "") // 移除script标签
                .replaceAll("(?i)<iframe[^>]*>[\\s\\S]*?</iframe>", "") // 移除iframe标签
                .replaceAll("(?i)<object[^>]*>[\\s\\S]*?</object>", "") // 移除object标签
                .replaceAll("(?i)<embed[^>]*>[\\s\\S]*?</embed>", "") // 移除embed标签
                .replaceAll("(?i)<form[^>]*>[\\s\\S]*?</form>", "") // 移除form标签
                .replaceAll("(?i)<link[^>]*>", "") // 移除link标签
                .replaceAll("(?i)<meta[^>]*>", ""); // 移除meta标签
            
            // 移除潜在的危险属性
            sanitized = sanitized.replaceAll("(?i)(onload|onerror|onclick|onmouseover|onmouseout|onfocus|onblur|onchange|onsubmit|onkeydown|onkeypress|onkeyup)\\s*=\\s*[\"'][^\"']*[\"']", "");
            
            // 移除javascript伪协议
            sanitized = sanitized.replaceAll("(?i)javascript:", "");
            sanitized = sanitized.replaceAll("(?i)vbscript:", "");
            sanitized = sanitized.replaceAll("(?i)data:", "");
            
            // 转义HTML特殊字符
            sanitized = sanitized
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
            
            return sanitized;
        } catch (Exception e) {
            log.error("HTML净化过程中发生错误", e);
            // 发生异常时返回原始输入的转义版本，确保安全性
            return escapeHtml(input);
        }
    }
    
    /**
     * 转义HTML特殊字符
     * 
     * @param input 待转义的输入
     * @return 转义后的内容
     */
    public static String escapeHtml(String input) {
        if (input == null) {
            return null;
        }
        
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");
    }
    
    /**
     * 验证输入是否包含危险字符
     * 
     * @param input 待验证的输入
     * @return 如果包含危险字符返回true，否则返回false
     */
    public static boolean containsDangerousChars(String input) {
        if (input == null) {
            return false;
        }
        
        String lowerInput = input.toLowerCase();
        return lowerInput.contains("<script") || 
               lowerInput.contains("javascript:") || 
               lowerInput.contains("vbscript:") || 
               lowerInput.contains("onload") || 
               lowerInput.contains("onerror") || 
               lowerInput.contains("alert(") || 
               lowerInput.contains("eval(") || 
               lowerInput.contains("expression(") ||
               lowerInput.contains("document.cookie") ||
               lowerInput.contains("window.location") ||
               lowerInput.contains("document.write") ||
               lowerInput.contains("innerhtml") ||
               lowerInput.contains("outerhtml");
    }
}