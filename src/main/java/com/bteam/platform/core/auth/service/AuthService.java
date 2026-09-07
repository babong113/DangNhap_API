package com.bteam.platform.core.auth.service;

import com.bteam.platform.core.auth.dto.AuthResponse;
import com.bteam.platform.core.auth.dto.ForgotPasswordRequest;
import com.bteam.platform.core.auth.dto.LoginRequest;
import com.bteam.platform.core.auth.dto.RegisterRequest;
import com.bteam.platform.core.auth.dto.ResetPasswordRequest;
import com.bteam.platform.core.auth.model.Account;
import com.bteam.platform.core.auth.model.AccountStatus;
import com.bteam.platform.core.auth.port.AccountStore;
import com.bteam.platform.core.auth.port.MailSender;
import com.bteam.platform.core.auth.port.RolePolicy;
import com.bteam.platform.core.common.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AccountStore accountStore;
    private final MailSender mailSender;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RolePolicy rolePolicy;

    public AuthResponse register(RegisterRequest request) {
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
        return toAuthResponse(savedAccount);
    }

    public AuthResponse login(LoginRequest request) {
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

        return toAuthResponse(savedAccount);
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

    private AuthResponse toAuthResponse(Account account) {
        Set<String> permissions = rolePolicy.permissionsForRoles(account.getRoles());
        account.setPermissions(permissions);

        return new AuthResponse(
                jwtService.generateToken(account),
                account.getId().toString(),
                account.getEmail(),
                account.getFullName(),
                account.getRoles(),
                permissions
        );
    }
}
