package com.allinone.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j // Thêm log để debug dễ hơn
public class CsrfFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (isSafeMethod(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1. Sửa lại tên Header: Dùng X-XSRF-TOKEN (cho đúng chuẩn và khớp axios)
        // Hoặc X-CSRF-TOKEN tùy bạn, nhưng phải khớp tuyệt đối giữa Postman/FE và BE
        String csrfHeader = request.getHeader("X-XSRF-TOKEN");
        String csrfCookie = getCookie(request, "XSRF-TOKEN");

        log.info("CSRF Check - Header: {}, Cookie: {}", csrfHeader, csrfCookie);

        // 2. Kiểm tra Null và So sánh
        if (csrfHeader == null || csrfCookie == null || !csrfHeader.equals(csrfCookie)) {
            log.warn("CSRF Validation Failed for path: {}", request.getServletPath());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("CSRF Token Mismatch or Missing");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String userAgent = request.getHeader("User-Agent");

        // 3. Sửa lại check Postman: Postman gửi User-Agent chứa "PostmanRuntime/"
        boolean isPostman = userAgent != null && userAgent.contains("PostmanRuntime");

        return path.startsWith("/api/auth/")
                || path.equals("/api/users/create")
                || isPostman;
    }

    private boolean isSafeMethod(HttpServletRequest request) {
        return request.getMethod().equals("GET")
                || request.getMethod().equals("HEAD")
                || request.getMethod().equals("OPTIONS");
    }

    private String getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}