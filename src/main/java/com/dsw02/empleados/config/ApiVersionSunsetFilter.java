package com.dsw02.empleados.config;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dsw02.empleados.service.ApiVersionSupportPolicyService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiVersionSunsetFilter extends OncePerRequestFilter {

    private final ApiVersionSupportPolicyService versionPolicyService;

    public ApiVersionSunsetFilter(ApiVersionSupportPolicyService versionPolicyService) {
        this.versionPolicyService = versionPolicyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path != null && path.startsWith("/api/v1/") && versionPolicyService.isVersionSunset("empleados")) {
            response.setStatus(HttpServletResponse.SC_GONE);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":\"GONE\",\"message\":\"API version v1 is sunset and no longer available\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
