package com.shxy.smartlearningacademycommon.utils;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.CheckSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.CheckSmsVerifyCodeResponse;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.google.gson.Gson;
import com.shxy.smartlearningacademycommon.exception.SmsVerifyCodeSendFailException;
import com.shxy.smartlearningacademycommon.exception.SmsVerifyFailException;
import com.shxy.smartlearningacademycommon.result.Result;
import darabonba.core.client.ClientOverrideConfiguration;

import java.util.concurrent.CompletableFuture;

/**
 * 封装短信发送和验证工具
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/5 21:54
 */
public class SmsVerifyCodeUtil {
    /**
     * 发送短信验证码
 * @param phoneNumber 手机号
     * @return
     */
    public static Result<String> sendSmsVerifyCode(String phoneNumber) {
//        DefaultCredentialProvider provider = DefaultCredentialProvider.builder()
//                .build();
        StaticCredentialProvider provider = StaticCredentialProvider.create(
                Credential.builder()
                        .accessKeyId(System.getenv("ALIYUN_ACCESS_KEY_ID"))
                        .accessKeySecret(System.getenv("ALIYUN_ACCESS_KEY_SECRET"))
                        .build()
        );
        try (AsyncClient client = AsyncClient.builder()
                .region("cn-shenzhen") // Region ID
                .credentialsProvider(provider)
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                .setEndpointOverride("dypnsapi.aliyuncs.com")
                )
                .build()) {

            SendSmsVerifyCodeRequest sendSmsVerifyCodeRequest = SendSmsVerifyCodeRequest.builder()
                    .signName("速通互联验证码")
                    .templateCode("100001")
                    .phoneNumber(phoneNumber)
                    .templateParam("{\"code\":\"##code##\",\"min\":\"5\"}")
                    .build();

            CompletableFuture<SendSmsVerifyCodeResponse> response = client.sendSmsVerifyCode(sendSmsVerifyCodeRequest);
            SendSmsVerifyCodeResponse resp = response.get();
            System.out.println(new Gson().toJson(resp));
        } catch (Exception e) {
            throw new SmsVerifyCodeSendFailException(e.getMessage());
        }
        return Result.success("发送成功");
    }

    /**
     *
     * 验证短信验证码
     * @param phoneNumber 手机号
     * @param verifyCode 验证码
     * @return
     */
    public static Result<String> checkSmsVerifyCode(String phoneNumber, String verifyCode) {
        StaticCredentialProvider provider=StaticCredentialProvider.create(
                Credential.builder()
                        .accessKeyId(System.getenv("ALIYUN_ACCESS_KEY_ID"))
                        .accessKeySecret(System.getenv("ALIYUN_ACCESS_KEY_SECRET"))
                        .build()
        );

        try (AsyncClient client = AsyncClient.builder()
                .region("cn-shenzhen") // Region ID
                .credentialsProvider(provider)
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                .setEndpointOverride("dypnsapi.aliyuncs.com")
                )
                .build()) {

            CheckSmsVerifyCodeRequest checkSmsVerifyCodeRequest = CheckSmsVerifyCodeRequest.builder()
                    .phoneNumber(phoneNumber)
                    .verifyCode(verifyCode)
                    .build();

            CompletableFuture<CheckSmsVerifyCodeResponse> response = client.checkSmsVerifyCode(checkSmsVerifyCodeRequest);
            CheckSmsVerifyCodeResponse resp = null;
            try {
                resp = response.get();
            } catch (Exception e) {
                throw new SmsVerifyFailException(e.getMessage());
            }
            return Result.success("验证成功");
        }
    }
}
