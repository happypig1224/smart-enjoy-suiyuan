package com.shxy.suiyuancommon.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * JWT工具类
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/4 22:35
 */
public class JwtUtil {
    /**
     * 生成jwt
     * 使用Hs256算法, 私匙使用固定秘钥
     *
     * @param secretKey jwt秘钥
     * @param ttlMillis jwt过期时间(毫秒)
     * @param claims    存储信息
     */

    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        long expireMillis = nowMillis + ttlMillis;
        Date expireTime = new Date(expireMillis);
        return Jwts.builder()
                .signWith(signatureAlgorithm, secretKey.getBytes(StandardCharsets.UTF_8))
                .setClaims(claims)
                .setId(UUID.randomUUID().toString().replace("-", ""))
                .setIssuedAt(now)
                .setExpiration(expireTime)
                .compact();

    }

    /**
     * Token解密
     * @param secretKey jwt秘钥
     * @param token     加密后的token
     */
    public static Claims parseJWT(String secretKey, String token) {
        return Jwts.parser()
                // 设置密钥
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                // 设置需要解析的token
                .build()
                .parseSignedClaims( token)
                .getBody();
    }

}
