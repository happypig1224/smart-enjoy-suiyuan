package com.shxy.smartlearningacademycommon.utils;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import com.shxy.smartlearningacademycommon.exception.FileUploadException;
import com.shxy.smartlearningacademycommon.properties.TencentCOSProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * 腾讯云COS头像上传工具类
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/9 10:40
 */
@Slf4j
@Component
public class TencentCOSAvatarUtil {

    @Autowired
    private TencentCOSProperties tencentCOSProperties;

    /**
     * 上传头像到腾讯云COS
     *
     * @param file 头像文件
     * @return 头像访问URL
     */
    public String uploadAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("头像文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isImageFile(originalFilename)) {
            throw new FileUploadException("只支持图片格式的文件（jpg、jpeg、png、gif、bmp）");
        }

        long fileSize = file.getSize();
        if (fileSize > 5 * 1024 * 1024) {
            throw new FileUploadException("头像文件大小不能超过5MB");
        }

        COSClient cosClient = null;
        try {
            cosClient = initCOSClient();
            
            String fileName = generateFileName(originalFilename);
            String key = tencentCOSProperties.getAvatarDir() + "/" + fileName;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileSize);
            metadata.setContentType(getContentType(originalFilename));

            InputStream inputStream = file.getInputStream();
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    tencentCOSProperties.getBucketName(),
                    key,
                    inputStream,
                    metadata
            );

            PutObjectResult result = cosClient.putObject(putObjectRequest);
            
            log.info("头像上传成功，ETag: {}, Key: {}", result.getETag(), key);

            return getCosUrl(key);

        } catch (CosServiceException e) {
            log.error("COS服务异常，状态码: {}, 错误码: {}, 错误信息: {}", 
                    e.getStatusCode(), e.getErrorCode(), e.getErrorMessage());
            throw new FileUploadException("头像上传失败：" + e.getErrorMessage());
        } catch (CosClientException e) {
            log.error("COS客户端异常", e);
            throw new FileUploadException("头像上传失败：" + e.getMessage());
        } catch (IOException e) {
            log.error("文件读取异常", e);
            throw new FileUploadException("头像文件读取失败");
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }

    /**
     * 删除COS中的头像
     *
     * @param avatarUrl 头像URL
     */
    public void deleteAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return;
        }

        COSClient cosClient = null;
        try {
            cosClient = initCOSClient();
            
            String key = extractKeyFromUrl(avatarUrl);
            if (key != null && !key.isEmpty()) {
                cosClient.deleteObject(tencentCOSProperties.getBucketName(), key);
                log.info("头像删除成功，Key: {}", key);
            }
        } catch (CosServiceException e) {
            log.error("删除头像时COS服务异常", e);
        } catch (CosClientException e) {
            log.error("删除头像时COS客户端异常", e);
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }

    /**
     * 初始化COS客户端
     *
     * @return COSClient实例
     */
    private COSClient initCOSClient() {
        COSCredentials cred = new BasicCOSCredentials(
                tencentCOSProperties.getSecretId(),
                tencentCOSProperties.getSecretKey()
        );

        Region region = new Region(tencentCOSProperties.getRegion());
        ClientConfig clientConfig = new ClientConfig(region);

        return new COSClient(cred, clientConfig);
    }

    /**
     * 生成唯一文件名
     *
     * @param originalFilename 原始文件名
     * @return 新文件名
     */
    private String generateFileName(String originalFilename) {
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID().toString().replace("-", "") + suffix;
    }

    /**
     * 判断是否为图片文件
     *
     * @param filename 文件名
     * @return 是否为图片
     */
    private boolean isImageFile(String filename) {
        String lowerCase = filename.toLowerCase();
        return lowerCase.endsWith(".jpg") ||
                lowerCase.endsWith(".jpeg") ||
                lowerCase.endsWith(".png") ||
                lowerCase.endsWith(".gif") ||
                lowerCase.endsWith(".bmp");
    }

    /**
     * 获取Content-Type
     *
     * @param filename 文件名
     * @return Content-Type
     */
    private String getContentType(String filename) {
        String lowerCase = filename.toLowerCase();
        if (lowerCase.endsWith(".jpg") || lowerCase.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerCase.endsWith(".png")) {
            return "image/png";
        } else if (lowerCase.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerCase.endsWith(".bmp")) {
            return "image/bmp";
        }
        return "application/octet-stream";
    }

    /**
     * 获取COS访问URL
     *
     * @param key 对象键
     * @return 完整URL
     */
    private String getCosUrl(String key) {
        return String.format("https://%s.cos.%s.myqcloud.com/%s",
                tencentCOSProperties.getBucketName(),
                tencentCOSProperties.getRegion(),
                key);
    }

    /**
     * 从URL中提取Key
     *
     * @param url COS URL
     * @return 对象键
     */
    private String extractKeyFromUrl(String url) {
        try {
            String prefix = String.format("https://%s.cos.%s.myqcloud.com/",
                    tencentCOSProperties.getBucketName(),
                    tencentCOSProperties.getRegion());
            if (url.startsWith(prefix)) {
                return url.substring(prefix.length());
            }
        } catch (Exception e) {
            log.error("解析URL失败", e);
        }
        return null;
    }
}
