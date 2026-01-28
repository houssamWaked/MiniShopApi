package com.HoussamAlwaked.minimarket.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    private static final String HEADER_NAME = "X-API-KEY";
    private static final String ENV_NAME = "API_KEY";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        if (path == null || !path.startsWith("/api/")) {
            return true;
        }

        String expected = System.getenv(ENV_NAME);
        if (expected == null || expected.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "API key is not configured.");
        }

        String provided = request.getHeader(HEADER_NAME);
        if (provided == null || provided.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "API key is required.");
        }

        if (!expected.equals(provided)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API key.");
        }

        return true;
    }
}
