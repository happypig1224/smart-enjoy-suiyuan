package com.shxy.suiyuanserver.controller.user;

import com.shxy.suiyuancommon.annotation.RequireLogin;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.dto.ForgotPasswordDTO;
import com.shxy.suiyuanentity.dto.LoginDTO;
import com.shxy.suiyuanentity.dto.RegisterDTO;
import com.shxy.suiyuanentity.dto.ResetPasswordDTO;
import com.shxy.suiyuanentity.dto.UserDTO;
import com.shxy.suiyuanentity.vo.AuthorStatsVO;
import com.shxy.suiyuanentity.vo.NotificationStatsVO;
import com.shxy.suiyuanentity.vo.UserFollowVO;
import com.shxy.suiyuanentity.vo.UserStatsVO;
import com.shxy.suiyuanentity.vo.UserVO;
import com.shxy.suiyuanentity.vo.UserProfileVO;
import com.shxy.suiyuanserver.service.UserFollowService;
import com.shxy.suiyuanserver.service.UserNotificationService;
import com.shxy.suiyuanserver.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RequestMapping("/user/user")
@RestController
@Tag(name = "用户模块接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserFollowService userFollowService;

    @Autowired
    private UserNotificationService userNotificationService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户使用手机号+密码进行登录")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户通过手机号+验证码注册")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return userService.register(registerDTO);
    }

    @PostMapping("/captcha/send")
    @Operation(summary = "发送验证码", description = "向指定手机号发送短信验证码")
    public Result<String> sendCaptcha(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        if (phone == null || phone.isEmpty()) {
            return Result.fail("手机号不能为空");
        }
        return userService.sendCaptcha(phone);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出当前登录状态")
    public Result<Map<String, Object>> logOut() {
        return userService.logout();
    }

    @PostMapping("/avatar/upload")
    @RequireLogin
    @Operation(summary = "上传头像", description = "用户上传或更新个人头像")
    public Result<String> uploadAvatar(@RequestParam(value = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择要上传的头像文件");
        }
        return userService.uploadAvatar(file);
    }

    @PutMapping("/password/reset")
    @RequireLogin
    @Operation(summary = "重置密码", description = "通过手机验证码重置用户密码（需登录）")
    public Result<String> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO) {
        return userService.resetPassword(resetPasswordDTO);
    }

    @PutMapping("/password/forgot")
    @Operation(summary = "忘记密码", description = "通过手机验证码重置密码（无需登录）")
    public Result<String> forgotPassword(@Valid @RequestBody ForgotPasswordDTO forgotPasswordDTO) {
        return userService.forgotPassword(forgotPasswordDTO);
    }

    @PutMapping("/user/info")
    @RequireLogin
    @Operation(summary = "更新用户信息", description = "更新用户名")
    public Result<String> updateUserInfo(@Valid @RequestBody UserDTO userDTO) {
        return userService.updateUserInfo(userDTO);
    }

    @PutMapping("/phone/update")
    @RequireLogin
    @Operation(summary = "修改手机号", description = "通过验证码验证后修改手机号")
    public Result<String> updatePhone(@Valid @RequestBody UserDTO userDTO) {
        return userService.updatePhone(userDTO);
    }

    @GetMapping("/profile")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    public Result<UserVO> getUserInfo() {
        return userService.getUserInfo();
    }

    @PostMapping("/follow")
    @RequireLogin
    @Operation(summary = "关注用户", description = "关注指定ID的用户")
    public Result<String> followUser(@RequestParam("followeeId") Long followeeId) {
        return userFollowService.followUser(followeeId);
    }

    @PostMapping("/unfollow")
    @RequireLogin
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

    @GetMapping("/stats")
    @Operation(summary = "获取用户统计信息", description = "获取当前用户的关注数和收藏数")
    public Result<UserStatsVO> getUserStats() {
        return userService.getUserStats();
    }

    @GetMapping("/notifications")
    @Operation(summary = "获取通知列表", description = "获取当前用户的通知列表(分页))")
    public Result<PageResult> getNotifications(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        return userNotificationService.getNotifications(page, size);
    }

    @PostMapping("/notifications/read")
    @Operation(summary = "标记通知为已读", description = "将指定通知标记为已读")
    public Result<String> markAsRead(@RequestParam("notificationId") Long notificationId) {
        return userNotificationService.markAsRead(notificationId);
    }

    @PostMapping("/notifications/read-all")
    @Operation(summary = "标记所有通知为已读", description = "将当前用户的所有未读通知标记为已读")
    public Result<String> markAllAsRead() {
        return userNotificationService.markAllAsRead();
    }

    @DeleteMapping("/notifications/{notificationId}")
    @Operation(summary = "删除通知", description = "删除指定通知")
    public Result<String> deleteNotification(@PathVariable("notificationId") Long notificationId) {
        return userNotificationService.deleteNotification(notificationId);
    }

    @DeleteMapping("/notifications")
    @Operation(summary = "清空所有通知", description = "清空当前用户的所有通知")
    public Result<String> clearAllNotifications() {
        return userNotificationService.clearAllNotifications();
    }

    @GetMapping("/notifications/stats")
    @Operation(summary = "获取通知统计", description = "获取当前用户的通知统计信息（总数、未读数）")
    public Result<NotificationStatsVO> getNotificationStats() {
        return userNotificationService.getNotificationStats();
    }

    @GetMapping("/public/profile/{userId}")
    @Operation(summary = "获取用户公开信息", description = "获取指定用户的公开信息（不含手机号等敏感信息）")
    public Result<UserProfileVO> getUserPublicProfile(@PathVariable("userId") Long userId) {
        return userService.getUserProfileById(userId);
    }

    @GetMapping("/author/stats/{userId}")
    @Operation(summary = "获取作者统计信息", description = "获取指定作者的文章数和粉丝数")
    public Result<AuthorStatsVO> getAuthorStats(@PathVariable("userId") Long userId) {
        return userService.getAuthorStats(userId);
    }
}
