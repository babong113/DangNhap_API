package com.bteam.giasu.service;

import com.bteam.giasu.dto.request.LoginRequest;
import com.bteam.giasu.dto.request.RegisterRequest;
import com.bteam.giasu.dto.response.AuthRespone;
import com.bteam.giasu.entity.User;
import com.bteam.giasu.exception.InvalidDataException;
import com.bteam.giasu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthRespone register(RegisterRequest request)
    {

        // chuan hoa email
        String email=request.getEmail().trim().toLowerCase();

        if(userRepository.existsByEmail(request.getEmail()))
        {
            throw new InvalidDataException("email đã được sử dụng");
        }

        if(userRepository.existsByPhoneNumber(request.getPhoneNumber()))
        {
            throw new InvalidDataException(" Số điện thoại đã được sử dụng");
        }

        // 4. Chuyển String role thành enum UserRole
        User.UserRole role;

        try {

            role = User.UserRole.valueOf(
                    request.getRole()
                            .trim()
                            .toUpperCase()
            );

        } catch (InvalidDataException e) {

            throw new InvalidDataException(
                    "Vai trò không hợp lệ"
            );
        }

        // 5. Không cho client tự tạo ADMIN
        if (role == User.UserRole.ADMIN) {

            throw new InvalidDataException(
                    "Không thể đăng ký tài khoản ADMIN"
            );
        }

        // 6. Tạo User
        User user = new User();

        user.setEmail(email);

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        user.setPhoneNumber(
                request.getPhoneNumber().trim()
        );

        user.setFullName(
                request.getFullName().trim()
        );

        user.setRole(role);

        user.setStatus(
                User.UserStatus.ACTIVE
        );

        // 7. Save database
        User savedUser =
                userRepository.save(user);

        // 8. Sinh JWT
        String token =
                jwtService.generateToken(savedUser);

        // 9. Response
        return new AuthRespone(
                token,
                savedUser.getId().toString(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole().name()
        );
    }


    public AuthRespone login(LoginRequest request) {

        // 1. Chuẩn hóa email
        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        // 2. Tìm user
        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(
                                () -> new InvalidDataException(
                                        "Email hoặc mật khẩu không đúng"
                                )
                        );

        // 3. Kiểm tra password
        boolean passwordCorrect =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPasswordHash()
                );

        if (!passwordCorrect) {

            throw new InvalidDataException(
                    "Email hoặc mật khẩu không đúng"
            );
        }

        // 4. Kiểm tra trạng thái account
        if (user.getStatus()
                != User.UserStatus.ACTIVE) {

            throw new InvalidDataException(
                    "Tài khoản hiện không hoạt động"
            );
        }

        // 5. Cập nhật lần đăng nhập cuối
        user.setLastLogin(
                ZonedDateTime.now()
        );

        userRepository.save(user);

        // 6. Generate JWT
        String token =
                jwtService.generateToken(user);

        // 7. Trả response
        return new AuthRespone(
                token,
                user.getId().toString(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name()
        );
    }
}


