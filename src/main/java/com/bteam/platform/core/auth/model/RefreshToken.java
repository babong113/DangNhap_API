package com.bteam.platform.core.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {
    private Long id;
    private Long userId;
    private String tokenHash;
    private ZonedDateTime expiresAt;
    private ZonedDateTime revokedAt;
    private Long replacedByTokenId;
    private ZonedDateTime createdAt;
    private String createdByIp;
    private String userAgent;

    public boolean isActive() {
        return revokedAt == null && expiresAt != null && expiresAt.isAfter(ZonedDateTime.now());
    }
}
