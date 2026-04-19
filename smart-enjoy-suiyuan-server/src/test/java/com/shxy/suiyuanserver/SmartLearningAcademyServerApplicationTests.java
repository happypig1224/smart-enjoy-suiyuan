package com.shxy.suiyuanserver;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.SmsVerifyCodeUtil;
import com.shxy.suiyuanentity.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@AutoConfigureMockMvc
@SpringBootTest
@Slf4j
class smartenjoysuiyuanServerApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SmsVerifyCodeUtil smsVerifyCodeUtil;


    @Test
    void testSendCode() {
        String phoneNumber = "19997556477";
        Result<String> sendSmsVerifyCode = smsVerifyCodeUtil.sendSmsVerifyCode(phoneNumber);
        log.info("发送验证码结果:{}", sendSmsVerifyCode.toString());
    }
    @Test
    void testCheckCode() {
        String phoneNumber = "19997556314";
        Result<String> checkedSmsVerifyCode = smsVerifyCodeUtil.checkSmsVerifyCode(phoneNumber, "9063");
        log.info("验证码验证结果:{}", checkedSmsVerifyCode.toString());
    }



    /**
     * 测试登录时，用户名或密码正确
     */
    @Test
    void testLoginWithValidCredentials() throws Exception {
        UserDTO loginRequest = UserDTO.builder()
                .userName("zhangsan")
                .userPassword("123456")
                .build();

        MvcResult result = mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        log.info("登录响应: {}", responseContent);
    }

    /**
     * 测试登录时，密码错误
     */
    @Test
    void testLoginWithInvalidPassword() throws Exception {
        UserDTO loginRequest = UserDTO.builder()
                .userName("testuser")
                .userPassword("wrongpassword")
                .build();

        MvcResult result = mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        log.info("错误密码登录响应: {}", responseContent);
    }

    /**
     * 测试登录时，用户不存在
     * */
    @Test
    void testLoginWithNonExistentUser() throws Exception {
        UserDTO loginRequest = UserDTO.builder()
                .userName("nonexistentuser")
                .userPassword("123456")
                .build();

        MvcResult result = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        log.info("不存在用户登录响应: {}", responseContent);
    }

    /**
     * 测试登录时，用户名或密码为空
     */
    @Test
    void testLoginWithEmptyFields() throws Exception {
        UserDTO loginRequest = UserDTO.builder()
                .userName("")
                .userPassword("")
                .build();

        MvcResult result = mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        log.info("空字段登录响应: {}", responseContent);
    }



}
