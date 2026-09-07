package com.bteam.platform.core.auth.service;

import com.bteam.platform.core.auth.dto.LoginRequest;
import com.bteam.platform.core.auth.dto.RegisterRequest;
import com.bteam.platform.core.auth.dto.AuthResponse;
import com.bteam.platform.core.auth.model.Account;
import com.bteam.platform.core.auth.model.AccountStatus;
import com.bteam.platform.core.auth.port.AccountStore;
import com.bteam.platform.core.auth.port.MailSender;
import com.bteam.platform.core.auth.port.RolePolicy;
import com.bteam.platform.core.common.exception.InvalidDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AccountStore accountStore;

    @Mock
    private MailSender mailSender;

    @Mock
    private JwtService jwtService;

    @Mock
    private RolePolicy rolePolicy;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void registerCreatesAccountWithDefaultRoleAndReturnsToken() {
        AuthService authService = new AuthService(
                accountStore, mailSender, jwtService, passwordEncoder, rolePolicy);
        RegisterRequest request = RegisterRequest.builder()
                .email(" Student@Example.com ")
                .password("123456")
                .phoneNumber("0912345678")
                .fullName("Nguyen Van A")
                .build();

        when(accountStore.existsByEmail("student@example.com")).thenReturn(false);
        when(accountStore.existsByPhoneNumber("0912345678")).thenReturn(false);
        when(rolePolicy.defaultRole()).thenReturn("STUDENT");
        when(rolePolicy.normalizeRole("STUDENT")).thenReturn(Optional.of("STUDENT"));
        when(rolePolicy.isRegistrationRoleAllowed("STUDENT")).thenReturn(true);
        when(rolePolicy.permissionsForRoles(Set.of("STUDENT"))).thenReturn(Set.of("account:read"));
        when(accountStore.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(1L);
            account.setPermissions(Set.of("account:read"));
            return account;
        });
        when(jwtService.generateToken(any(Account.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("student@example.com");
        assertThat(response.getRoles()).containsExactly("STUDENT");
        assertThat(response.getPermissions()).containsExactly("account:read");
    }

    @Test
    void registerRejectsInvalidRole() {
        AuthService authService = new AuthService(
                accountStore, mailSender, jwtService, passwordEncoder, rolePolicy);
        RegisterRequest request = RegisterRequest.builder()
                .email("student@example.com")
                .password("123456")
                .phoneNumber("0912345678")
                .fullName("Nguyen Van A")
                .role("UNKNOWN")
                .build();

        when(rolePolicy.normalizeRole("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("Vai tro khong hop le");
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        AuthService authService = new AuthService(
                accountStore, mailSender, jwtService, passwordEncoder, rolePolicy);
        Account account = Account.builder()
                .id(1L)
                .email("student@example.com")
                .fullName("Nguyen Van A")
                .roles(Set.of("STUDENT"))
                .status(AccountStatus.ACTIVE)
                .passwordHash(passwordEncoder.encode("123456"))
                .build();

        when(accountStore.findByEmail("student@example.com")).thenReturn(Optional.of(account));
        when(accountStore.save(account)).thenReturn(account);
        when(rolePolicy.permissionsForRoles(Set.of("STUDENT"))).thenReturn(Set.of("account:read"));
        when(jwtService.generateToken(account)).thenReturn("jwt-token");

        AuthResponse response = authService.login(
                LoginRequest.builder()
                        .email(" Student@Example.com ")
                        .password("123456")
                        .build()
        );

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("student@example.com");
    }
}
