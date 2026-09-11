package com.bteam.platform.core.auth.port;

import com.bteam.platform.core.auth.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenStore {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    void revokeAllActiveByUserId(Long userId);
}
