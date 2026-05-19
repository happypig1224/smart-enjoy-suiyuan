package com.shxy.suiyuancommon.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ContentAuditUtil {

    private static final Set<String> SENSITIVE_WORDS = new HashSet<>(Arrays.asList(
            "赌博", "色情", "毒品", "枪支", "爆炸", "杀人", "自杀",
            "传销", "诈骗", "放贷", "高利贷", "代开", "发票",
            "刷单", "兼职", "招嫖", "约炮", "裸聊",
            "反动", "邪教", "暴恐", "煽动", "分裂"
    ));

    public static String containsSensitiveWord(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        String lowerContent = content.toLowerCase();
        for (String word : SENSITIVE_WORDS) {
            if (lowerContent.contains(word)) {
                return word;
            }
        }
        return null;
    }

    public static String filterSensitiveWords(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        String result = content;
        for (String word : SENSITIVE_WORDS) {
            if (result.contains(word)) {
                String replacement = "*".repeat(word.length());
                result = result.replace(word, replacement);
            }
        }
        return result;
    }
}
