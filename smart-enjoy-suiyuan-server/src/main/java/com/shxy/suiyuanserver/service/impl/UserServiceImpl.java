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
import com.shxy.suiyuanentity.dto.ForgotPasswordDTO;
import com.shxy.suiyuanentity.dto.LoginDTO;
import com.shxy.suiyuanentity.dto.RegisterDTO;
import com.shxy.suiyuanentity.dto.ResetPasswordDTO;
import com.shxy.suiyuanentity.dto.UserDTO;
import com.shxy.suiyuanentity.entity.User;
import com.shxy.suiyuanentity.vo.AuthorStatsVO;
import com.shxy.suiyuanentity.vo.UserStatsVO;
import com.shxy.suiyuanentity.vo.UserVO;
import com.shxy.suiyuanentity.vo.UserProfileVO;
import com.shxy.suiyuanserver.mapper.PostFavoriteMapper;
import com.shxy.suiyuanserver.mapper.ResourceFavoriteMapper;
import com.shxy.suiyuanserver.mapper.SecondhandFavoriteMapper;
import com.shxy.suiyuanserver.mapper.UserFollowMapper;
import com.shxy.suiyuanserver.mapper.UserMapper;
import com.shxy.suiyuanserver.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.dao.DuplicateKeyException;
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

import static com.shxy.suiyuancommon.constant.RedisConstant.TOKEN_BLACKLIST_KEY_PREFIX;
import static com.shxy.suiyuancommon.constant.RedisConstant.USER_INFO_KEY_PREFIX;
import static com.shxy.suiyuancommon.constant.RedisConstant.USER_TOKEN_KEY_PREFIX;

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

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private PostFavoriteMapper postFavoriteMapper;

    @Autowired
    private ResourceFavoriteMapper resourceFavoriteMapper;

    @Autowired
    private SecondhandFavoriteMapper secondhandFavoriteMapper;

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Override
    public Result<Map<String, Object>> login(LoginDTO loginDTO) {
        String phone = loginDTO.getPhone();

        String loginRateLimitKey = RateLimitConstant.LOGIN_RATE_LIMIT_KEY + phone;
        RateLimitUtil.checkRateLimit(stringRedisTemplate, loginRateLimitKey,
                RateLimitConstant.LOGIN_TIME_WINDOW, RateLimitConstant.LOGIN_MAX_REQUESTS);

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone));
        if (user == null) {
            log.warn("登录失败: 手机号或密码错误, phone={}", phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
            throw new PasswordErrorException();
        }
        if (!PASSWORD_ENCODER.matches(loginDTO.getUserPassword(), user.getUserPassword())) {
            log.warn("登录失败: 手机号或密码错误, userId={}", user.getId());
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
        if (userVO.getPhone() != null && userVO.getPhone().length() >= 7) {
            userVO.setPhoneMasked(userVO.getPhone().substring(0, 3) + "****" + userVO.getPhone().substring(userVO.getPhone().length() - 4));
        }
        userVO.setPhone(null);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", userVO);

        String tokenKey = USER_TOKEN_KEY_PREFIX + user.getId();
        redisTemplate.opsForValue().set(tokenKey, token, jwtProperties.getUserTtl(), TimeUnit.MILLISECONDS);

        log.info("用户登录成功: userId={}", user.getId());
        return Result.success(data);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> register(RegisterDTO registerDTO) {
        Result<String> checked = smsVerifyCodeUtil.checkSmsVerifyCode(registerDTO.getPhone(), registerDTO.getVerifyCode());
        if (checked.getCode() != 200) {
            return Result.fail("验证码错误!");
        }

        LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(User::getPhone, registerDTO.getPhone());
        if (userMapper.exists(phoneWrapper)) {
            throw new PhoneExistsException();
        }

        String userName = registerDTO.getUserName();
        if (userName == null || userName.trim().isEmpty()) {
            do {
                userName = "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
            } while (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getUserName, userName)));
        } else {
            userName = userName.trim();
            if (userName.length() < 3 || userName.length() > 50) {
                return Result.fail("用户名长度必须在3-50个字符之间!");
            }
            if (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getUserName, userName))) {
                throw new UsernameExistsException();
            }
        }

        String userPassword = PASSWORD_ENCODER.encode(registerDTO.getUserPassword());

        User user = User.builder()
                .userName(userName)
                .userPassword(userPassword)
                .phone(registerDTO.getPhone())
                .createTime(new Date())
                .updateTime(new Date())
                .role(UserRoleEnum.USER.getCode())
                .status(UserStatusConstant.NORMAL)
                .build();
        try {
            this.save(user);
        } catch (DuplicateKeyException e) {
            throw new UsernameExistsException();
        }

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
        Long userId = BaseContext.getCurrentUserId();
        String tokenKey = USER_TOKEN_KEY_PREFIX + userId;
        String token = (String) redisTemplate.opsForValue().get(tokenKey);
        if (token != null) {
            try {
                io.jsonwebtoken.Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
                String jti = claims.getId();
                if (jti != null) {
                    long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
                    if (ttl > 0) {
                        stringRedisTemplate.opsForValue().set(TOKEN_BLACKLIST_KEY_PREFIX + jti, "1", ttl, TimeUnit.MILLISECONDS);
                    }
                }
            } catch (Exception e) {
                log.warn("登出时解析token失败: {}", e.getMessage());
            }
        }
        redisTemplate.delete(tokenKey);
        return Result.success();
    }

    @Override
    public Result<String> sendCaptcha(String phone) {
        String rateLimitKey = RateLimitConstant.SMS_RATE_LIMIT_KEY + phone;
        RateLimitUtil.checkRateLimit(stringRedisTemplate, rateLimitKey,
            RateLimitConstant.SMS_TIME_WINDOW, RateLimitConstant.SMS_MAX_REQUESTS);

        smsVerifyCodeUtil.sendSmsVerifyCode(phone);
        log.info("短信验证码发送成功: phone={}", phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
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
    public Result<String> resetPassword(ResetPasswordDTO resetPasswordDTO) {
        Long userId = BaseContext.getCurrentUserId();
        String phone = resetPasswordDTO.getPhone();

        User currentUser = userMapper.selectById(userId);
        if (currentUser == null) {
            return Result.fail("用户不存在");
        }
        if (!currentUser.getPhone().equals(phone)) {
            return Result.fail("手机号与当前登录用户不匹配");
        }

        Result<String> checked = smsVerifyCodeUtil.checkSmsVerifyCode(phone, resetPasswordDTO.getVerifyCode());
        if (checked.getCode() != 200) {
            log.warn("用户{}密码重置失败: 验证码错误", userId);
            return Result.fail("验证码错误!");
        }

        String newPassword = PASSWORD_ENCODER.encode(resetPasswordDTO.getNewPassword());
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
    public Result<String> forgotPassword(ForgotPasswordDTO forgotPasswordDTO) {
        String phone = forgotPasswordDTO.getPhone();

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone));
        if (user == null) {
            return Result.fail("该手机号未注册");
        }

        Result<String> checked = smsVerifyCodeUtil.checkSmsVerifyCode(phone, forgotPasswordDTO.getVerifyCode());
        if (checked.getCode() != 200) {
            log.warn("忘记密码重置失败: 验证码错误, phone={}", phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
            return Result.fail("验证码错误!");
        }

        String newPassword = PASSWORD_ENCODER.encode(forgotPasswordDTO.getNewPassword());
        int result = userMapper.updatePassword(user.getId(), newPassword);
        if (result == 0) {
            return Result.fail("修改密码失败!");
        }

        redisTemplate.delete(USER_TOKEN_KEY_PREFIX + user.getId());
        log.info("用户{}忘记密码重置成功", user.getId());
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
        log.info("用户{}修改手机号成功", userId);
        return Result.success("修改手机号成功!");
    }

    @Transactional(readOnly = true)
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
                    if (vo.getPhone() != null && vo.getPhone().length() >= 7) {
                        vo.setPhoneMasked(vo.getPhone().substring(0, 3) + "****" + vo.getPhone().substring(vo.getPhone().length() - 4));
                    }
                    vo.setPhone(null);
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

    @Transactional(readOnly = true)
    @Override
    public Result<UserStatsVO> getUserStats() {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return Result.fail("用户未登录");
        }

        Integer followingCount = userFollowMapper.countFollowing(userId);
        if (followingCount == null) {
            followingCount = 0;
        }

        Integer postFavoriteCount = postFavoriteMapper.countByUserId(userId);
        if (postFavoriteCount == null) {
            postFavoriteCount = 0;
        }

        Integer resourceFavoriteCount = resourceFavoriteMapper.countByUserId(userId);
        if (resourceFavoriteCount == null) {
            resourceFavoriteCount = 0;
        }

        Integer secondhandFavoriteCount = secondhandFavoriteMapper.countByUserId(userId);
        if (secondhandFavoriteCount == null) {
            secondhandFavoriteCount = 0;
        }

        int totalFavoriteCount = Math.max(0, postFavoriteCount + resourceFavoriteCount + secondhandFavoriteCount);

        UserStatsVO stats = UserStatsVO.builder()
                .followingCount(Math.max(0, followingCount))
                .favoriteCount(totalFavoriteCount)
                .build();

        return Result.success(stats);
    }

    @Transactional(readOnly = true)
    @Override
    public Result<UserProfileVO> getUserProfileById(Long userId) {
        if (userId == null || userId <= 0) {
            return Result.fail("用户ID无效");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        Integer followingCount = userFollowMapper.countFollowing(userId);
        if (followingCount == null) {
            followingCount = 0;
        }

        Integer followersCount = userFollowMapper.countFollowers(userId);
        if (followersCount == null) {
            followersCount = 0;
        }

        UserProfileVO profile = UserProfileVO.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .avatar(user.getAvatar())
                .createTime(user.getCreateTime())
                .followingCount(followingCount)
                .followersCount(followersCount)
                .build();

        return Result.success(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public Result<AuthorStatsVO> getAuthorStats(Long userId) {
        if (userId == null || userId <= 0) {
            return Result.fail("用户ID无效");
        }

        AuthorStatsVO stats = userMapper.selectAuthorStats(userId);
        if (stats == null) {
            stats = AuthorStatsVO.builder()
                    .postCount(0)
                    .followerCount(0)
                    .build();
        }

        return Result.success(stats);
    }
}
