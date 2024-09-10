package com.bulkSms.Authentication;

import com.bulkSms.Model.CommonResponse;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.io.PrintWriter;

@Configuration
@ControllerAdvice
public class AuthEntryPoint implements AuthenticationEntryPoint {
    private Logger logger = LoggerFactory.getLogger(AuthenticationEntryPoint.class);

    CommonResponse commonResponse = new CommonResponse();

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<CommonResponse> handleUserNotFoundException(UsernameNotFoundException ex) {
        logger.error(ex.getMessage());
        commonResponse.setMsg(ex.getMessage());
        return new ResponseEntity<>(commonResponse, HttpStatus.NOT_FOUND);
    }

    // You can add more exception handlers for different exceptions here
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CommonResponse> handleRuntimeException(RuntimeException ex) {
        logger.error(ex.getMessage());
        commonResponse.setMsg(ex.getMessage());
        return new ResponseEntity<>(commonResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse> handleGlobalException(Exception ex) {
        logger.error(ex.getMessage());
        commonResponse.setMsg(ex.getMessage());

        return new ResponseEntity<>(commonResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<CommonResponse> handleGlobalException(BadCredentialsException ex) {
        logger.error(ex.getMessage());
        commonResponse.setMsg(ex.getMessage());

        return new ResponseEntity<>(commonResponse, HttpStatus.NOT_FOUND);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        commonResponse.setMsg("Access denied.");
        Gson gson = new Gson();
        PrintWriter writer=response.getWriter();
        writer.print(gson.toJson(commonResponse));
        writer.flush();

    }
}

