package com.shxy.suiyuanserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.dto.UserDTO;
import com.shxy.suiyuanentity.entity.User;
import com.shxy.suiyuanentity.vo.UserVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
* @author huang qi long
* @description 针对表【user】的数据库操作Service
* @createDate 2026-04-04 21:30:08
*/
public interface UserService extends IService<User> {

    Result<Map<String, Object>> login(UserDTO userLoginDTO);

    Result<Map<String, Object>> register(UserDTO userRegisterDTO);

    Result<Map<String, Object>> logout();

    Result<String> sendCaptcha(String phone);

    Result<String> uploadAvatar(MultipartFile file);

    Result<String> resetPassword(UserDTO userResetPasswordDTO);

    Result<String> updateUserInfo(UserDTO userDTO);

    Result<String> updatePhone(UserDTO userDTO);

    Result<UserVO> getUserInfo();

}
