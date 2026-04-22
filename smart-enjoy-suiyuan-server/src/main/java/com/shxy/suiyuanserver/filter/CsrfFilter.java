package com.shxy.suiyuanserver.filter;

import com.shxy.suiyuancommon.constant.JwtClaimConstant;
import com.shxy.suiyuancommon.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * CSRF防护过滤器
 * 
 * @author Tech Lead
 */
@Slf4j
@Component
public class CsrfFilter extends HttpFilter {
    
    @Value("${smart.enjoy.suiyuan.jwt.user-token-name:Authorization}")
    private String tokenHeaderName;
    
    @Value("${smart.enjoy.suiyuan.jwt.user-secret-key}")
    private String jwtSecretKey;
    
    // 需要CSRF保护的HTTP方法
    private static final List<String> CSRF_PROTECTED_METHODS = Arrays.asList("POST", "PUT", "DELETE", "PATCH");
    
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        String method = request.getMethod().toUpperCase();
        
        // 只对特定的HTTP方法进行CSRF检查
        if (CSRF_PROTECTED_METHODS.contains(method)) {
            // 检查请求是否包含有效的JWT token
            String token = extractToken(request);
            if (token == null || !validateToken(token)) {
                log.warn("CSRF防护：请求缺少有效的认证token，method: {}, uri: {}", method, request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"code\":403,\"message\":\"缺少有效的认证信息\"}");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
    
    /**
     * 从请求头中提取JWT token
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(tokenHeaderName);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    /**
     * 验证JWT token的有效性
     */
    private boolean validateToken(String token) {
        try {
            Claims claims = JwtUtil.parseJWT(jwtSecretKey, token);
            // 可以进一步验证claims中的内容
            return claims != null && claims.get(JwtClaimConstant.USER_ID) != null;
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }
}