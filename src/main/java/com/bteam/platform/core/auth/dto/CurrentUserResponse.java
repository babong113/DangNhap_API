package com.bteam.platform.core.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentUserResponse {
    private String userId;
    private String email;
    private String fullName;
    private Set<String> roles;
    private Set<String> permissions;
}
