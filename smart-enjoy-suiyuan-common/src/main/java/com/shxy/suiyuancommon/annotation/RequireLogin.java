package com.shxy.suiyuancommon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要登录才能访问的接口注解
 * <p>
 * 标注此注解的方法会由 LoginUserInterceptor 拦截并验证用户登录状态。
 * 未登录用户将被拦截并返回 401 未登录响应。
 * </p>
 * @author Wu, Hui Ming
 * @version 1.0
 * @School SuiHua  University
 * @since 2026/4/25 22:05
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireLogin {
}
