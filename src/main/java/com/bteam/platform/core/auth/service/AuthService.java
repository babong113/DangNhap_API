package com.bteam.platform.core.auth.service;

import com.bteam.platform.core.auth.dto.AuthResponse;
import com.bteam.platform.core.auth.dto.CurrentUserResponse;
import com.bteam.platform.core.auth.dto.ForgotPasswordRequest;
import com.bteam.platform.core.auth.dto.LoginRequest;
import com.bteam.platform.core.auth.dto.LogoutRequest;
import com.bteam.platform.core.auth.dto.RefreshTokenRequest;
import com.bteam.platform.core.auth.dto.RegisterRequest;
import com.bteam.platform.core.auth.dto.ResetPasswordRequest;
import com.bteam.platform.core.auth.model.Account;
import com.bteam.platform.core.auth.model.AccountStatus;
import com.bteam.platform.core.auth.model.RefreshToken;
import com.bteam.platform.core.auth.port.AccountStore;
import com.bteam.platform.core.auth.port.MailSender;
import com.bteam.platform.core.auth.port.RefreshTokenStore;
import com.bteam.platform.core.auth.port.RolePolicy;
import com.bteam.platform.core.common.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AccountStore accountStore;
    private final MailSender mailSender;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RolePolicy rolePolicy;
    private final RefreshTokenStore refreshTokenStore;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration = 2_592_000_000L;

    public AuthResponse register(RegisterRequest request, String ipAddress, String userAgent) {
        String email = request.getEmail().trim().toLowerCase();
        String phoneNumber = request.getPhoneNumber().trim();

        if (accountStore.existsByEmail(email)) {
            throw new InvalidDataException("Email da duoc su dung");
        }

        if (accountStore.existsByPhoneNumber(phoneNumber)) {
            throw new InvalidDataException("So dien thoai da duoc su dung");
        }

        String requestedRole = request.getRole();
        String role = (requestedRole == null || requestedRole.isBlank())
                ? rolePolicy.normalizeRole(rolePolicy.defaultRole())
                .orElseThrow(() -> new InvalidDataException("Vai tro mac dinh khong ton tai"))
                : rolePolicy.normalizeRole(requestedRole)
                .orElseThrow(() -> new InvalidDataException("Vai tro khong hop le"));

        if (!rolePolicy.isRegistrationRoleAllowed(role)) {
            throw new InvalidDataException("Vai tro khong duoc phep dang ky");
        }

        Account account = Account.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(phoneNumber)
                .fullName(request.getFullName().trim())
                .status(AccountStatus.ACTIVE)
                .roles(Set.of(role))
                .build();

        Account savedAccount = accountStore.save(account);
        return toAuthResponse(savedAccount, createRefreshToken(savedAccount, ipAddress, userAgent));
    }

    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        String email = request.getEmail().trim().toLowerCase();

        Account account = accountStore.findByEmail(email).orElseThrow(
                () -> new InvalidDataException("Email hoac mat khau khong dung"));

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new InvalidDataException("Email hoac mat khau khong dung");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidDataException("Tai khoan hien khong hoat dong");
        }

        account.setLastLogin(ZonedDateTime.now());
        Account savedAccount = accountStore.save(account);

        return toAuthResponse(savedAccount, createRefreshToken(savedAccount, ipAddress, userAgent));
    }

    public AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress, String userAgent) {
        RefreshToken currentToken = refreshTokenStore.findByTokenHash(hashToken(request.getRefreshToken()))
                .orElseThrow(() -> new InvalidDataException("Refresh token khong hop le"));

        if (currentToken.getRevokedAt() != null) {
            refreshTokenStore.revokeAllActiveByUserId(currentToken.getUserId());
            throw new InvalidDataException("Refresh token da bi thu hoi va co dau hieu duoc dung lai");
        }

        if (!currentToken.isActive()) {
            throw new InvalidDataException("Refresh token da het han hoac da bi thu hoi");
        }

        Account account = accountStore.findById(currentToken.getUserId())
                .orElseThrow(() -> new InvalidDataException("Nguoi dung khong ton tai"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidDataException("Tai khoan hien khong hoat dong");
        }

        GeneratedRefreshToken generatedRefreshToken = createRefreshToken(account, ipAddress, userAgent);
        currentToken.setRevokedAt(ZonedDateTime.now());
        currentToken.setReplacedByTokenId(generatedRefreshToken.refreshToken().getId());
        refreshTokenStore.save(currentToken);

        return toAuthResponse(account, generatedRefreshToken);
    }

    public void logout(LogoutRequest request) {
        refreshTokenStore.findByTokenHash(hashToken(request.getRefreshToken()))
                .ifPresent(refreshToken -> {
                    refreshToken.setRevokedAt(ZonedDateTime.now());
                    refreshTokenStore.save(refreshToken);
                });
    }

    public void logoutAll(String email) {
        Account account = accountStore.findByEmail(email)
                .orElseThrow(() -> new InvalidDataException("Nguoi dung khong ton tai"));
        refreshTokenStore.revokeAllActiveByUserId(account.getId());
    }

    public CurrentUserResponse currentUser(String email) {
        Account account = accountStore.findByEmail(email)
                .orElseThrow(() -> new InvalidDataException("Nguoi dung khong ton tai"));

        return new CurrentUserResponse(
                account.getId().toString(),
                account.getEmail(),
                account.getFullName(),
                account.getRoles(),
                account.getPermissions()
        );
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        Account account = accountStore.findByEmail(email).orElseThrow(
                () -> new InvalidDataException("Khong tim thay tai khoan voi email nay"));

        account.setResetPasswordToken(UUID.randomUUID().toString());
        account.setResetPasswordTokenExpiry(ZonedDateTime.now().plusMinutes(30));
        Account savedAccount = accountStore.save(account);

        mailSender.sendResetPasswordEmail(savedAccount.getEmail(), savedAccount.getResetPasswordToken());
    }

    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getConfirmPassword().equals(request.getNewPassword())) {
            throw new InvalidDataException("Xac nhan mat khau khong khop");
        }

        Account account = accountStore.findByResetPasswordToken(request.getToken())
                .orElseThrow(() -> new InvalidDataException("Token dat lai mat khau khong hop le"));

        if (account.getResetPasswordTokenExpiry() == null
                || account.getResetPasswordTokenExpiry().isBefore(ZonedDateTime.now())) {
            throw new InvalidDataException("Da het han dat lai mat khau");
        }

        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        account.setResetPasswordToken(null);
        account.setResetPasswordTokenExpiry(null);
        accountStore.save(account);
    }

    private AuthResponse toAuthResponse(Account account, GeneratedRefreshToken refreshToken) {
        Set<String> permissions = rolePolicy.permissionsForRoles(account.getRoles());
        account.setPermissions(permissions);
        String accessToken = jwtService.generateToken(account);

        return new AuthResponse(
                accessToken,
                refreshToken.rawToken(),
                "Bearer",
                jwtService.getAccessTokenExpiration() / 1000,
                account.getId().toString(),
                account.getEmail(),
                account.getFullName(),
                account.getRoles(),
                permissions
        );
    }

    private GeneratedRefreshToken createRefreshToken(Account account, String ipAddress, String userAgent) {
        if (refreshTokenExpiration <= 0) {
            throw new IllegalStateException("jwt.refresh-token-expiration phai lon hon 0");
        }

        String rawToken = generateOpaqueToken();
        RefreshToken savedToken = refreshTokenStore.save(RefreshToken.builder()
                .userId(account.getId())
                .tokenHash(hashToken(rawToken))
                .expiresAt(ZonedDateTime.now().plusNanos(refreshTokenExpiration * 1_000_000))
                .createdByIp(ipAddress)
                .userAgent(trimToLength(userAgent, 512))
                .build());

        return new GeneratedRefreshToken(rawToken, savedToken);
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[64];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 khong kha dung", e);
        }
    }

    private String trimToLength(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private record GeneratedRefreshToken(String rawToken, RefreshToken refreshToken) {
    }
}
