package com.shxy.suiyuancommon.utils;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.CheckSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.CheckSmsVerifyCodeResponse;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.google.gson.Gson;
import com.shxy.suiyuancommon.exception.SmsVerifyCodeSendFailException;
import com.shxy.suiyuancommon.exception.SmsVerifyFailException;
import com.shxy.suiyuancommon.result.Result;
import darabonba.core.client.ClientOverrideConfiguration;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 封装短信发送和验证工具
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/5 21:54
 */
@Component
@Slf4j
public class SmsVerifyCodeUtil {
    private AsyncClient client;

    @Autowired
    public void setClient(AsyncClient client) {
        this.client = client;
    }

    /**
     * 发送短信验证码
     *
     * @param phoneNumber 手机号
     * @return
     */
    public Result<String> sendSmsVerifyCode(String phoneNumber) {
        try {
            SendSmsVerifyCodeRequest sendSmsVerifyCodeRequest = SendSmsVerifyCodeRequest.builder()
                    .signName("速通互联验证码")
                    .templateCode("100001")
                    .phoneNumber(phoneNumber)
                    .templateParam("{\"code\":\"##code##\",\"min\":\"5\"}")
                    .build();

            CompletableFuture<SendSmsVerifyCodeResponse> response = client.sendSmsVerifyCode(sendSmsVerifyCodeRequest);
            SendSmsVerifyCodeResponse resp = response.get();
            log.info("发送短信验证码响应: {}", resp);
        } catch (Exception e) {
            throw new SmsVerifyCodeSendFailException(e.getMessage());
        }
        return Result.success("发送成功");
    }

    /**
     * 验证短信验证码
     *
     * @param phoneNumber 手机号
     * @param verifyCode  验证码
     * @return
     */
    public Result<String> checkSmsVerifyCode(String phoneNumber, String verifyCode) {
        CheckSmsVerifyCodeRequest checkSmsVerifyCodeRequest = CheckSmsVerifyCodeRequest.builder()
                .phoneNumber(phoneNumber)
                .verifyCode(verifyCode)
                .build();
        CompletableFuture<CheckSmsVerifyCodeResponse> response = client.checkSmsVerifyCode(checkSmsVerifyCodeRequest);
        CheckSmsVerifyCodeResponse resp = null;
        try {
            resp = response.get();
        } catch (Exception e) {
            return Result.fail("验证码错误!");
        }
        return Result.success("验证成功");
    }
}
