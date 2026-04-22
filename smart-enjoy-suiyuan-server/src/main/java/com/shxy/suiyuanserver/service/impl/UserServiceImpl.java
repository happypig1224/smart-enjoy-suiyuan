package com.shxy.suiyuanserver.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import com.shxy.suiyuanserver.service.UserService;
import com.shxy.suiyuanserver.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
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
 * @author huang qi long
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
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TencentCOSAvatarUtil tencentCOSAvatarUtil;

    @Autowired
    private SmsVerifyCodeUtil smsVerifyCodeUtil;


    private static PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();


    /**
     * 登录
     *
     * @param userLoginRequestDTO
     * @return
     */
    public Result<Map<String, Object>> login(UserDTO userLoginRequestDTO) {
        String username = userLoginRequestDTO.getUserName();
        log.info("用户尝试登录: username={}", username);

        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUserName, username);
        //User user = this.getOne(lambdaQueryWrapper);
        User user = userMapper.selectOne(lambdaQueryWrapper);
        if (user == null) {
            log.warn("登录失败: 用户名不存在, username={}", username);
            throw new AccountNotFoundException();
        }
        if (!passwordEncoder.matches(userLoginRequestDTO.getUserPassword(), user.getUserPassword())) {
            log.warn("登录失败: 密码错误, userId={}", user.getId());
            throw new PasswordErrorException();
        }
        if (user.getStatus() == 0) {
            log.warn("登录失败: 账户已被锁定, userId={}", user.getId());
            throw new AccountLockedException();
        }
        
        log.info("用户登录验证通过: userId={}, username={}", user.getId(), username);
        
        //生成对应的token令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", userVO);

        //登录成功token存入redis
        String tokenKey = USER_TOKEN_KEY_PREFIX + user.getId();
        redisTemplate.opsForValue().set(tokenKey, token, jwtProperties.getUserTtl(), TimeUnit.MILLISECONDS);
        
        log.info("用户登录成功: userId={}, username={}", user.getId(), username);
        return Result.success(data);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> register(UserDTO userRegisterDTO) {
        log.info("用户注册请求: userName={}, phone={}",
                userRegisterDTO.getUserName(),
                userRegisterDTO.getPhone());
        Result<String> checked = smsVerifyCodeUtil.checkSmsVerifyCode(userRegisterDTO.getPhone(), userRegisterDTO.getCode());
        if (checked.getCode() != 200) {
            return Result.fail("验证码错误!");
        }
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper
                .eq(User::getUserName, userRegisterDTO.getUserName())
                .or()
                .eq(User::getPhone, userRegisterDTO.getPhone());
        long count = userMapper.selectCount(userQueryWrapper);
        if (count > 0) {
            LambdaQueryWrapper<User> nameWrapper = new LambdaQueryWrapper<>();
            nameWrapper.eq(User::getUserName, userRegisterDTO.getUserName());
            if (userMapper.exists(nameWrapper)) {
                throw new AccountExistsException();
            }
            throw new PhoneExistsException();
        }
        //String userPassword = DigestUtils.md5DigestAsHex(userRegisterDTO.getUserPassword().getBytes());
        String userPassword = passwordEncoder.encode(userRegisterDTO.getUserPassword());

        String nickName = "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);

        User user = User.builder()
                .userName(userRegisterDTO.getUserName())
                .userPassword(userPassword)
                .userGender(userRegisterDTO.getUserGender())
                .phone(userRegisterDTO.getPhone())
                .nickName(nickName)
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
        return Result.success(data);
    }

    public Result<Map<String, Object>> logout() {
        redisTemplate.delete(USER_TOKEN_KEY_PREFIX + BaseContext.getCurrentUserId());
        return Result.success();
    }

    public Result<String> sendCaptcha(String phone) {
        // 短信限流: 60秒内最多发送1次
        String rateLimitKey = RateLimitConstant.SMS_RATE_LIMIT_KEY + phone;
        RateLimitUtil.checkRateLimit(redisTemplate, rateLimitKey,
            RateLimitConstant.SMS_TIME_WINDOW, RateLimitConstant.SMS_MAX_REQUESTS);
        
        log.info("发送短信验证码: phone={}", phone);
        smsVerifyCodeUtil.sendSmsVerifyCode(phone);
        return Result.success("验证码发送成功");
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> uploadAvatar(MultipartFile file) {
        Long userId = BaseContext.getCurrentUserId();
        
        // 文件上传限流: 60秒内最多上传5次
        String rateLimitKey = RateLimitConstant.UPLOAD_RATE_LIMIT_KEY + userId;
        RateLimitUtil.checkRateLimit(redisTemplate, rateLimitKey,
            RateLimitConstant.UPLOAD_TIME_WINDOW, RateLimitConstant.UPLOAD_MAX_REQUESTS);
        
        log.info("用户{}开始上传头像, 文件名: {}, 文件大小: {} bytes", userId, file.getOriginalFilename(), file.getSize());
        try {
            String avatarUrl = tencentCOSAvatarUtil.uploadAvatar(file);
            userMapper.updateAvatar(userId, avatarUrl);
            // 缓存失效
            redisTemplate.delete(USER_INFO_KEY_PREFIX + userId);
            log.info("用户{}上传头像成功, 头像地址: {}", userId, avatarUrl);
            return Result.success(avatarUrl);
        } catch (Exception e) {
            log.error("用户{}上传头像失败", userId, e);
            return Result.fail("上传头像失败: " + e.getMessage());
        }
    }

    public Result<String> resetPassword(UserDTO userResetPasswordDTO) {
        Long userId = BaseContext.getCurrentUserId();
        String phone = userResetPasswordDTO.getPhone();
        log.info("用户{}开始重置密码, 手机号: {}, IP: {}", userId, phone, getClientIp());

        Result<String> checked = smsVerifyCodeUtil.checkSmsVerifyCode(phone, userResetPasswordDTO.getCode());
        if (checked.getCode() != 200) {
            log.warn("用户{}密码重置失败: 验证码错误, 手机号: {}", userId, phone);
            return Result.fail("验证码错误!");
        }

        String newPassword = passwordEncoder.encode(userResetPasswordDTO.getNewPassword());
        int result = userMapper.updatePassword(userId, newPassword);
        if (result == 0) {
            log.warn("用户{}密码重置失败: 数据库更新失败, 手机号: {}", userId, phone);
            return Result.fail("修改密码失败!");
        }

        // 清除用户token,强制重新登录
        redisTemplate.delete(USER_TOKEN_KEY_PREFIX + userId);
        log.info("用户{}密码重置成功, 已清除会话, 手机号: {}", userId, phone);
        return Result.success("修改密码成功!");
    }

    public Result<String> updateUserInfo(UserDTO userDTO) {
        Long userId = BaseContext.getCurrentUserId();
        log.info("用户{}开始修改个人信息", userId);

        User existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            log.warn("用户{}修改信息失败: 用户不存在", userId);
            return Result.fail("用户不存在!");
        }

        String oldNickName = existingUser.getNickName();
        Integer oldGender = existingUser.getUserGender();
        Integer oldAge = existingUser.getUserAge();
        String oldGrade = existingUser.getUserGrade();

        String nickName = userDTO.getNickName() != null ? userDTO.getNickName() : existingUser.getNickName();
        Integer userGender = userDTO.getUserGender() != null ? userDTO.getUserGender() : existingUser.getUserGender();
        Integer userAge = userDTO.getUserAge() != null ? userDTO.getUserAge() : existingUser.getUserAge();
        String userGrade = userDTO.getUserGrade() != null ? userDTO.getUserGrade() : existingUser.getUserGrade();

        User user = User.builder()
                .id(userId)
                .nickName(nickName)
                .userGender(userGender)
                .userAge(userAge)
                .userGrade(userGrade)
                .updateTime(new Date())
                .build();
        int result = userMapper.updateUserInfo(user);
        if (result == 0) {
            log.warn("用户{}修改信息失败: 数据库更新失败", userId);
            return Result.fail("修改用户信息失败!");
        }

        // 记录变更内容
        StringBuilder changes = new StringBuilder();
        if (!Objects.equals(oldNickName, nickName)) {
            changes.append(String.format("昵称[%s->%s] ", oldNickName, nickName));
        }
        if (!Objects.equals(oldGender, userGender)) {
            changes.append(String.format("性别[%s->%s] ", oldGender, userGender));
        }
        if (!Objects.equals(oldAge, userAge)) {
            changes.append(String.format("年龄[%s->%s] ", oldAge, userAge));
        }
        if (!Objects.equals(oldGrade, userGrade)) {
            changes.append(String.format("年级[%s->%s] ", oldGrade, userGrade));
        }

        log.info("用户{}修改信息成功: {}", userId, changes.toString().trim());

        // 清除用户信息缓存
        redisTemplate.delete(USER_INFO_KEY_PREFIX + userId);

        return Result.success("修改用户信息成功!");
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        try {
            jakarta.servlet.http.HttpServletRequest request =
                    ((org.springframework.web.context.request.ServletRequestAttributes)
                            org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes())
                            .getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return ip;
        } catch (Exception e) {
            return "unknown";
        }
    }

    public Result<UserVO> getUserInfo() {
        Long userId = BaseContext.getCurrentUserId();
        String cacheKey = USER_INFO_KEY_PREFIX + userId;

        log.info("用户{}开始获取信息", userId);

        // 使用工具类解决缓存穿透+击穿+雪崩
        UserVO userVO = redisCacheUtil.queryWithMutex(
                cacheKey,
                UserVO.class,
                key -> {
                    User user = userMapper.getUserInfo(userId);
                    if (user == null) {
                        log.warn("用户{}信息不存在", userId);
                        return null;
                    }
                    UserVO vo = new UserVO();
                    BeanUtils.copyProperties(user, vo);
                    return vo;
                },
                jwtProperties.getUserTtl(),
                TimeUnit.MILLISECONDS
        );

        if (userVO == null) {
            log.warn("用户{}获取信息失败，用户不存在", userId);
            return Result.fail("用户不存在");
        }
        log.info("用户{}获取信息成功", userId);
        return Result.success(userVO);
    }
}
