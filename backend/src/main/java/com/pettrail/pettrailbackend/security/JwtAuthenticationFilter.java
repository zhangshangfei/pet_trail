package com.pettrail.pettrailbackend.security;

import com.pettrail.pettrailbackend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String token = getTokenFromRequest(request);
            log.debug("请求URL: {}, Token: {}", request.getRequestURI(), token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null");
            
            // 1. 验证 Token
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                String openid = jwtUtil.getOpenidFromToken(token);

                // 2. 构建认证对象
                // 关键点：我们将 userId 作为了 principal (主体)
                // 这样在业务代码里就可以直接强转为 Long 获取 ID
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,  // <--- 这里放 userId，而不是 User 对象或 String
                                null,    // credentials (密码位，通常留空)
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 3. 存入上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info("用户认证成功: userId={}, token={}", userId, token.substring(0, Math.min(20, token.length())) + "...");
            } else {
                log.debug("Token验证失败: token={}", token);
            }
        } catch (Exception e) {
            log.error("无法设置用户认证: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取Token
     * @param request HttpServletRequest
     * @return Token字符串
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
