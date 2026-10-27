package com.exp9.cicd.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 1. Adds X-App-Track / X-App-Version headers to every response so the canary
 *    verification script can tell which release answered.
 * 2. When app.simulate-failure=true (set only on the canary by the CD workflow's
 *    "simulate failure" option) every /api/** call fails with HTTP 500 - this is how
 *    we demonstrate an automatic ROLLBACK.
 */
@Component
public class ReleaseTrackFilter extends OncePerRequestFilter {

    private final String track;
    private final String version;
    private final boolean simulateFailure;

    public ReleaseTrackFilter(@Value("${app.track}") String track,
                              @Value("${app.version}") String version,
                              @Value("${app.simulate-failure:false}") boolean simulateFailure) {
        this.track = track;
        this.version = version;
        this.simulateFailure = simulateFailure;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        response.setHeader("X-App-Track", track);
        response.setHeader("X-App-Version", version);
        if (simulateFailure && request.getRequestURI().startsWith("/api/")) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Simulated canary failure");
            return;
        }
        chain.doFilter(request, response);
    }
}
