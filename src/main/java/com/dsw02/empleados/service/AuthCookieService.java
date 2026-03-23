package com.dsw02.empleados.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthCookieService {

    private final boolean secureCookies;

    public AuthCookieService(@Value("${app.auth.secure-cookies:false}") boolean secureCookies) {
        this.secureCookies = secureCookies;
    }

    public void writeAuthCookies(HttpServletResponse response, String accessToken, String refreshToken, String csrfToken,
                                 long accessTokenSeconds, long refreshTokenSeconds) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("ACCESS_TOKEN", accessToken, accessTokenSeconds, true, "Lax"));
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("REFRESH_TOKEN", refreshToken, refreshTokenSeconds, true, "Strict"));
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("XSRF-TOKEN", csrfToken, refreshTokenSeconds, false, "Strict"));
    }

    public void clearAuthCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("ACCESS_TOKEN", "", 0, true, "Lax"));
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("REFRESH_TOKEN", "", 0, true, "Strict"));
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("XSRF-TOKEN", "", 0, false, "Strict"));
    }

    private String buildCookie(String name, String value, long maxAgeSeconds, boolean httpOnly, String sameSite) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
            .path("/")
            .httpOnly(httpOnly)
            .secure(secureCookies)
            .sameSite(sameSite)
            .maxAge(Duration.ofSeconds(Math.max(maxAgeSeconds, 0)))
            .build();
        return cookie.toString();
    }
}
