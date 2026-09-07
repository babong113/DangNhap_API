package com.bteam.platform.core.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    private Long id;
    private String email;
    private String passwordHash;
    private String phoneNumber;
    private String fullName;
    private String avatarUrl;
    private AccountStatus status;
    private ZonedDateTime lastLogin;
    private String resetPasswordToken;
    private ZonedDateTime resetPasswordTokenExpiry;
    @Builder.Default
    private Set<String> roles = new HashSet<>();
    @Builder.Default
    private Set<String> permissions = new HashSet<>();
}
