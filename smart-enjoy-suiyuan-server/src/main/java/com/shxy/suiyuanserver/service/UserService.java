package com.shxy.suiyuanserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.dto.ForgotPasswordDTO;
import com.shxy.suiyuanentity.dto.LoginDTO;
import com.shxy.suiyuanentity.dto.RegisterDTO;
import com.shxy.suiyuanentity.dto.ResetPasswordDTO;
import com.shxy.suiyuanentity.dto.UserDTO;
import com.shxy.suiyuanentity.entity.User;
import com.shxy.suiyuanentity.vo.UserProfileVO;
import com.shxy.suiyuanentity.vo.UserStatsVO;
import com.shxy.suiyuanentity.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface UserService extends IService<User> {

    Result<Map<String, Object>> login(LoginDTO loginDTO);

    Result<Map<String, Object>> register(RegisterDTO registerDTO);

    Result<Map<String, Object>> logout();

    Result<String> sendCaptcha(String phone);

    Result<String> uploadAvatar(MultipartFile file);

    Result<String> resetPassword(ResetPasswordDTO resetPasswordDTO);

    Result<String> forgotPassword(ForgotPasswordDTO forgotPasswordDTO);

    Result<String> updateUserInfo(UserDTO userDTO);

    Result<String> updatePhone(UserDTO userDTO);

    Result<UserVO> getUserInfo();

    Result<UserStatsVO> getUserStats();

    Result<UserProfileVO> getUserProfileById(Long userId);
}
