package com.bulkSms.Utility;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Logger;

public class StaticCSPFilter  extends HttpFilter {
    private static final Logger LOGGER = Logger.getLogger(StaticCSPFilter.class.getName());

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
//        response.setHeader("Content-Security-Policy", "default-src 'self'; frame-ancestors 'none'");
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; style-src 'self' https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com; frame-ancestors 'none';");

        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-Content-Type-Options", "nosniff");

//        LOGGER.info("Added CSP Header: " + response.getHeader("Content-Security-Policy"));
        LOGGER.info("Added X-Frame-Options Header: " + response.getHeader("X-Frame-Options"));
        LOGGER.info("Added X-Content-Type-Options Header: " + response.getHeader("X-Content-Type-Options"));
        chain.doFilter(request, response);
    }
}
