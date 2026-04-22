package com.shxy.suiyuancommon.utils;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 敏感词过滤工具类
 * 
 * @author Tech Lead
 */
@Component
public class SensitiveWordFilter {

    // 敏感词集合 - 实际应用中应从数据库或配置文件加载
    private static final Set<String> SENSITIVE_WORDS = new HashSet<>(Arrays.asList(
        "政治敏感词1", "政治敏感词2", "暴力词汇", "色情词汇", "辱骂词汇", "广告链接"
    ));

    // 正则表达式模式用于匹配敏感词
    private volatile Pattern pattern;

    /**
     * 检查内容是否包含敏感词
     * 
     * @param content 待检查的内容
     * @return 如果包含敏感词返回true，否则返回false
     */
    public boolean containsSensitiveWords(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        // 延迟初始化正则表达式模式
        if (pattern == null) {
            synchronized (this) {
                if (pattern == null) {
                    String regex = String.join("|", SENSITIVE_WORDS);
                    pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                }
            }
        }

        return pattern.matcher(content).find();
    }

    /**
     * 过滤敏感词，将敏感词替换为***
     * 
     * @param content 原始内容
     * @return 过滤后的内容
     */
    public String filterSensitiveWords(String content) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }

        String filteredContent = content;
        for (String word : SENSITIVE_WORDS) {
            filteredContent = filteredContent.replaceAll("(?i)" + Pattern.quote(word), "***");
        }

        return filteredContent;
    }

    /**
     * 添加敏感词到过滤列表
     * 注意：实际应用中应该通过配置中心或数据库管理
     * 
     * @param word 要添加的敏感词
     */
    public void addSensitiveWord(String word) {
        if (word != null && !word.trim().isEmpty()) {
            SENSITIVE_WORDS.add(word.trim());
            // 重置正则表达式模式以便下次重新编译
            pattern = null;
        }
    }
}