package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shxy.suiyuancommon.constant.RateLimitConstant;
import com.shxy.suiyuancommon.constant.UserStatusConstant;
import com.shxy.suiyuancommon.enums.UserRoleEnum;
import com.shxy.suiyuancommon.exception.*;
import com.shxy.suiyuancommon.properties.JwtProperties;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuancommon.utils.JwtUtil;
import com.shxy.suiyuancommon.utils.RateLimitUtil;
import com.shxy.suiyuancommon.utils.RedisCacheUtil;
import com.shxy.suiyuancommon.utils.SmsVerifyCodeUtil;
import com.shxy.suiyuancommon.utils.TencentCOSAvatarUtil;
import com.shxy.suiyuanentity.dto.UserDTO;
import com.shxy.suiyuanentity.entity.User;
import com.shxy.suiyuanentity.vo.UserVO;
import com.shxy.suiyuanserver.mapper.UserMapper;
import com.shxy.suiyuanserver.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.shxy.suiyuancommon.constant.RedisConstant.USER_INFO_KEY_PREFIX;
import static com.shxy.suiyuancommon.constant.RedisConstant.USER_TOKEN_KEY_PREFIX;

/**
 * @author Wu, Hui Ming
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2026-04-04 21:30:08
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TencentCOSAvatarUtil tencentCOSAvatarUtil;

    @Autowired
    private SmsVerifyCodeUtil smsVerifyCodeUtil;

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Override
    public Result<Map<String, Object>> login(UserDTO userLoginRequestDTO) {
        String phone = userLoginRequestDTO.getPhone();

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone));
        if (user == null) {
            log.warn("登录失败: 手机号不存在, phone={}", phone);
            throw new AccountNotFoundException();
        }
        if (!PASSWORD_ENCODER.matches(userLoginRequestDTO.getUserPassword(), user.getUserPassword())) {
            log.warn("登录失败: 密码错误, userId={}", user.getId());
            throw new PasswordErrorException();
        }
        if (user.getStatus() == 0) {
            log.warn("登录失败: 账户已被锁定, userId={}", user.getId());
            throw new AccountLockedException();
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", userVO);

        String tokenKey = USER_TOKEN_KEY_PREFIX + user.getId();
        redisTemplate.opsForValue().set(tokenKey, token, jwtProperties.getUserTtl(), TimeUnit.MILLISECONDS);

        log.info("用户登录成功: userId={}, phone={}", user.getId(), phone);
        return Result.success(data);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> register(UserDTO userRegisterDTO) {
        Result<String> checked = smsVerifyCodeUtil.checkSmsVerifyCode(userRegisterDTO.getPhone(), userRegisterDTO.getVerifyCode());
        if (checked.getCode() != 200) {
            return Result.fail("验证码错误!");
        }

        // 检查手机号是否已注册
        LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(User::getPhone, userRegisterDTO.getPhone());
        if (userMapper.exists(phoneWrapper)) {
            throw new PhoneExistsException();
        }

        // 生成唯一的用户名：user_ + 随机6位字符
        String userName;
        do {
            userName = "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        } while (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getUserName, userName)));

        String userPassword = PASSWORD_ENCODER.encode(userRegisterDTO.getUserPassword());

        User user = User.builder()
                .userName(userName)
                .userPassword(userPassword)
                .phone(userRegisterDTO.getPhone())
                .createTime(new Date())
                .updateTime(new Date())
                .role(UserRoleEnum.USER.getCode())
                .status(UserStatusConstant.NORMAL)
                .build();
        this.save(user);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        String tokenKey = USER_TOKEN_KEY_PREFIX + user.getId();
        redisTemplate.opsForValue().set(tokenKey, token, jwtProperties.getUserTtl(), TimeUnit.MILLISECONDS);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("userName", userName);
        return Result.success(data);
    }

    @Override
    public Result<Map<String, Object>> logout() {
        redisTemplate.delete(USER_TOKEN_KEY_PREFIX + BaseContext.getCurrentUserId());
        return Result.success();
    }

    @Override
    public Result<String> sendCaptcha(String phone) {
        String rateLimitKey = RateLimitConstant.SMS_RATE_LIMIT_KEY + phone;
        RateLimitUtil.checkRateLimit(stringRedisTemplate, rateLimitKey,
            RateLimitConstant.SMS_TIME_WINDOW, RateLimitConstant.SMS_MAX_REQUESTS);

        smsVerifyCodeUtil.sendSmsVerifyCode(phone);
        log.info("短信验证码发送成功: phone={}", phone);
        return Result.success("验证码发送成功");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<String> uploadAvatar(MultipartFile file) {
        Long userId = BaseContext.getCurrentUserId();

        String rateLimitKey = RateLimitConstant.UPLOAD_RATE_LIMIT_KEY + userId;
        RateLimitUtil.checkRateLimit(stringRedisTemplate, rateLimitKey,
            RateLimitConstant.UPLOAD_TIME_WINDOW, RateLimitConstant.UPLOAD_MAX_REQUESTS);

        try {
            String avatarUrl = tencentCOSAvatarUtil.uploadAvatar(file);
            userMapper.updateAvatar(userId, avatarUrl);
            redisTemplate.delete(USER_INFO_KEY_PREFIX + userId);
            log.info("用户上传头像成功: userId={}", userId);
            return Result.success("上传成功", avatarUrl);
        } catch (Exception e) {
            log.error("用户上传头像失败: userId={}", userId, e);
            return Result.fail("上传头像失败: " + e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<String> resetPassword(UserDTO userResetPasswordDTO) {
        Long userId = BaseContext.getCurrentUserId();
        String phone = userResetPasswordDTO.getPhone();

        Result<String> checked = smsVerifyCodeUtil.checkSmsVerifyCode(phone, userResetPasswordDTO.getVerifyCode());
        if (checked.getCode() != 200) {
            log.warn("用户{}密码重置失败: 验证码错误", userId);
            return Result.fail("验证码错误!");
        }

        String newPassword = PASSWORD_ENCODER.encode(userResetPasswordDTO.getNewPassword());
        int result = userMapper.updatePassword(userId, newPassword);
        if (result == 0) {
            log.warn("用户{}密码重置失败: 数据库更新失败", userId);
            return Result.fail("修改密码失败!");
        }

        redisTemplate.delete(USER_TOKEN_KEY_PREFIX + userId);
        log.info("用户{}密码重置成功", userId);
        return Result.success("修改密码成功!");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<String> updateUserInfo(UserDTO userDTO) {
        Long userId = BaseContext.getCurrentUserId();

        User existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            return Result.fail("用户不存在!");
        }

        if (userDTO.getUserName() != null && !userDTO.getUserName().equals(existingUser.getUserName())) {
            String newUserName = userDTO.getUserName().trim();
            if (newUserName.length() < 3 || newUserName.length() > 50) {
                return Result.fail("用户名长度必须在3-50个字符之间!");
            }
            LambdaQueryWrapper<User> nameWrapper = new LambdaQueryWrapper<>();
            nameWrapper.eq(User::getUserName, newUserName);
            if (userMapper.exists(nameWrapper)) {
                throw new UsernameExistsException();
            }
            existingUser.setUserName(newUserName);
            existingUser.setUpdateTime(new Date());
            userMapper.updateById(existingUser);
            redisTemplate.delete(USER_INFO_KEY_PREFIX + userId);
        }

        return Result.success("修改用户信息成功!");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<String> updatePhone(UserDTO userDTO) {
        Long userId = BaseContext.getCurrentUserId();

        if (userDTO.getPhone() == null || userDTO.getPhone().isEmpty()) {
            return Result.fail("新手机号不能为空!");
        }
        if (userDTO.getVerifyCode() == null || userDTO.getVerifyCode().isEmpty()) {
            return Result.fail("请输入验证码!");
        }

        String newPhone = userDTO.getPhone().trim();

        Result<String> checked = smsVerifyCodeUtil.checkSmsVerifyCode(newPhone, userDTO.getVerifyCode());
        if (checked.getCode() != 200) {
            log.warn("用户{}修改手机号失败: 验证码错误", userId);
            return Result.fail("验证码错误!");
        }

        LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(User::getPhone, newPhone);
        if (userMapper.exists(phoneWrapper)) {
            throw new PhoneExistsException();
        }

        int result = userMapper.updatePhone(userId, newPhone);
        if (result == 0) {
            log.warn("用户{}修改手机号失败: 数据库更新失败", userId);
            return Result.fail("修改手机号失败!");
        }

        redisTemplate.delete(USER_INFO_KEY_PREFIX + userId);
        log.info("用户{}修改手机号成功: newPhone={}", userId, newPhone);
        return Result.success("修改手机号成功!");
    }

    @Override
    public Result<UserVO> getUserInfo() {
        Long userId = BaseContext.getCurrentUserId();
        String cacheKey = USER_INFO_KEY_PREFIX + userId;

        UserVO userVO = redisCacheUtil.queryWithMutex(
                cacheKey,
                UserVO.class,
                key -> {
                    User user = userMapper.getUserInfo(userId);
                    if (user == null) {
                        return null;
                    }
                    UserVO vo = new UserVO();
                    BeanUtils.copyProperties(user, vo);
                    return vo;
                },
                jwtProperties.getUserTtl() / 1000,
                TimeUnit.SECONDS
        );

        if (userVO == null) {
            return Result.fail("用户不存在");
        }
        return Result.success(userVO);
    }
}
