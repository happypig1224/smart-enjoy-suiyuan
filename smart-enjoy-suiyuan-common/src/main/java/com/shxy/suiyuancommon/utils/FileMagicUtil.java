package com.shxy.suiyuancommon.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileMagicUtil {

    private static final Map<String, byte[]> MAGIC_BYTES = new HashMap<>();

    static {
        MAGIC_BYTES.put("jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        MAGIC_BYTES.put("jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        MAGIC_BYTES.put("png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});
        MAGIC_BYTES.put("gif", new byte[]{0x47, 0x49, 0x46, 0x38});
        MAGIC_BYTES.put("bmp", new byte[]{0x42, 0x4D});
        MAGIC_BYTES.put("pdf", new byte[]{0x25, 0x50, 0x44, 0x46});
    }

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/bmp",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain",
            "text/markdown"
    );

    public static boolean isValidFile(InputStream inputStream, String originalFilename) {
        if (originalFilename == null) return false;
        String ext = originalFilename.toLowerCase();
        int dotIdx = ext.lastIndexOf('.');
        if (dotIdx < 0) return false;
        ext = ext.substring(dotIdx + 1);

        byte[] expectedMagic = MAGIC_BYTES.get(ext);
        if (expectedMagic == null) return true;

        BufferedInputStream bis = null;
        if (!(inputStream instanceof BufferedInputStream)) {
            bis = new BufferedInputStream(inputStream);
        } else {
            bis = (BufferedInputStream) inputStream;
        }

        try {
            bis.mark(expectedMagic.length + 1);
            byte[] actualMagic = new byte[expectedMagic.length];
            int bytesRead = bis.read(actualMagic);
            bis.reset();

            if (bytesRead < expectedMagic.length) return false;

            for (int i = 0; i < expectedMagic.length; i++) {
                if (actualMagic[i] != expectedMagic[i]) return false;
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isValidImage(InputStream inputStream, String originalFilename) {
        return isValidFile(inputStream, originalFilename);
    }

    public static List<String> getAllowedMimeTypes() {
        return ALLOWED_MIME_TYPES;
    }

    public static boolean isAllowedMimeType(String mimeType) {
        return ALLOWED_MIME_TYPES.contains(mimeType);
    }
}
