package com.allinone.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
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

        String csrfHeader = request.getHeader("X-CSRF-TOKEN");
        String csrfCookie = getCookie(request, "XSRF-TOKEN");

        if (csrfHeader == null || !csrfHeader.equals(csrfCookie)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return path.startsWith("/api/auth/")
                || path.equals("/api/users/create")
                || "PostmanRuntime".equals(
                request.getHeader("User-Agent")
        );
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
