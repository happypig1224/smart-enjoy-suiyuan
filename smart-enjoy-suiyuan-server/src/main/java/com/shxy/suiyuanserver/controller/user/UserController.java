package com.shxy.suiyuanserver.controller.user;

import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.dto.UserDTO;
import com.shxy.suiyuanentity.vo.UserFollowVO;
import com.shxy.suiyuanentity.vo.UserVO;
import com.shxy.suiyuanserver.service.UserFollowService;
import com.shxy.suiyuanserver.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 用户模块接口
 * @author Wu, Hui Ming
 * @version 2.0
 * @School Suihua University
 * @since 2026/4/4 20:56
 */
@RequestMapping("/user/user")
@RestController
@Tag(name = "用户模块接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserFollowService userFollowService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户使用手机号+密码进行登录")
    public Result<Map<String, Object>> login(@Valid @RequestBody UserDTO userLoginDTO) {
        return userService.login(userLoginDTO);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户通过手机号+验证码注册，用户名系统自动生成")
    public Result<Map<String, Object>> register(@Valid @RequestBody UserDTO userRegisterDTO) {
        return userService.register(userRegisterDTO);
    }

    @GetMapping("/captcha/send")
    @Operation(summary = "发送验证码", description = "向指定手机号发送短信验证码")
    public Result<String> sendCaptcha(@RequestParam("phone") String phone) {
        return userService.sendCaptcha(phone);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出当前登录状态")
    public Result<Map<String, Object>> logOut() {
        return userService.logout();
    }

    @PostMapping("/avatar/upload")
    @Operation(summary = "上传头像", description = "用户上传或更新个人头像")
    public Result<String> uploadAvatar(@RequestParam(value = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择要上传的头像文件");
        }
        return userService.uploadAvatar(file);
    }

    @PutMapping("/password/reset")
    @Operation(summary = "重置密码", description = "通过手机验证码重置用户密码")
    public Result<String> resetPassword(@Valid @RequestBody UserDTO userResetPasswordDTO) {
        return userService.resetPassword(userResetPasswordDTO);
    }

    @PutMapping("/user/info")
    @Operation(summary = "更新用户信息", description = "更新用户名")
    public Result<String> updateUserInfo(@Valid @RequestBody UserDTO userDTO) {
        return userService.updateUserInfo(userDTO);
    }

    @PutMapping("/phone/update")
    @Operation(summary = "修改手机号", description = "通过验证码验证后修改手机号")
    public Result<String> updatePhone(@Valid @RequestBody UserDTO userDTO) {
        return userService.updatePhone(userDTO);
    }

    @GetMapping("/user/info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    public Result<UserVO> getUserInfo() {
        return userService.getUserInfo();
    }

    @PostMapping("/follow")
    @Operation(summary = "关注用户", description = "关注指定ID的用户")
    public Result<String> followUser(@RequestParam("followeeId") Long followeeId) {
        return userFollowService.followUser(followeeId);
    }

    @PostMapping("/unfollow")
    @Operation(summary = "取消关注", description = "取消关注指定ID的用户")
    public Result<String> unfollowUser(@RequestParam("followeeId") Long followeeId) {
        return userFollowService.unfollowUser(followeeId);
    }

    @GetMapping("/follow/list")
    @Operation(summary = "获取关注列表", description = "获取当前用户关注的所有用户列表")
    public Result<List<UserFollowVO>> getFollowList() {
        return userFollowService.getFollowList();
    }
    @GetMapping("/follow/check")
    @Operation(summary = "检查是否已关注", description = "检查当前用户是否已关注指定用户")
    public Result<Boolean> isFollowing(@RequestParam("followeeId") Long followeeId) {
        return userFollowService.isFollowing(followeeId);
    }

    @GetMapping("/notifications")
    @Operation(summary = "获取通知列表", description = "获取当前用户的通知列表")
    public Result<List<String>> getNotifications() {
        return userFollowService.getNotifications();
    }

    @PostMapping("/notifications/clear")
    @Operation(summary = "清除未读通知数", description = "将当前用户的未读通知数清零")
    public Result<String> clearUnreadCount() {
        return userFollowService.clearUnreadCount();
    }
}
