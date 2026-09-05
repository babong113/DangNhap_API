package com.bteam.giasu.controller;

import com.bteam.giasu.dto.request.ForgotPasswordRequest;
import com.bteam.giasu.dto.request.LoginRequest;
import com.bteam.giasu.dto.request.RegisterRequest;
import com.bteam.giasu.dto.request.ResetPasswordRequest;
import com.bteam.giasu.dto.response.AuthRespone;
import com.bteam.giasu.dto.response.ForgetRespone;
import com.bteam.giasu.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid
            @RequestBody RegisterRequest request
    )
    {
        AuthRespone respone=authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respone);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid
            @RequestBody LoginRequest request
            )
    {
        AuthRespone respone=authService.login(request);
        return ResponseEntity.ok(respone);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid
            @RequestBody ForgotPasswordRequest request
            )
    {
        authService.forgetPassword(request);

        return ResponseEntity.ok(new ForgetRespone(true,"Email đặt lại mật khẩu đã được gửi"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Valid
            @RequestBody ResetPasswordRequest request
            )
    {
        authService.resetPassword(request);
        return ResponseEntity.ok(new ForgetRespone(true,"Đặt lại mật khẩu thành công"));
    }
}
