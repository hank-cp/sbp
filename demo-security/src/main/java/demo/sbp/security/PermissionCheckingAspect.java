/*
 * Copyright (C) 2019-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demo.sbp.security;

import demo.sbp.security.annotation.RequirePermission;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

/**
 * Permission checking interceptor.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@Aspect
public class PermissionCheckingAspect {

    @Around("@annotation(permission)")
    public Object check(ProceedingJoinPoint joinPoint, RequirePermission permission) throws Throwable {
        String requiredRole = "ROLE_"+permission.value();
        if (!StringUtils.isEmpty(requiredRole)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails user = null;
            if (authentication != null && authentication.isAuthenticated()) {
                user = (UserDetails) authentication.getPrincipal();
            }
            if (user == null
                    || user.getAuthorities() == null
                    || user.getAuthorities().stream().noneMatch(auth -> requiredRole.equalsIgnoreCase(auth.getAuthority()))) {
                throw new InsufficientAuthenticationException("Permission Denied");
            }
        }
        return joinPoint.proceed();
    }
}

