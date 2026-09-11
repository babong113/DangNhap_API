package com.bteam.platform.adapter.persistence.jpa;

import com.bteam.platform.adapter.persistence.jpa.entity.RefreshTokenEntity;
import com.bteam.platform.adapter.persistence.jpa.entity.UserEntity;
import com.bteam.platform.adapter.persistence.jpa.repository.RefreshTokenRepository;
import com.bteam.platform.adapter.persistence.jpa.repository.UserRepository;
import com.bteam.platform.core.auth.model.RefreshToken;
import com.bteam.platform.core.auth.port.RefreshTokenStore;
import com.bteam.platform.core.common.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaRefreshTokenStore implements RefreshTokenStore {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenEntity entity = refreshToken.getId() == null
                ? new RefreshTokenEntity()
                : refreshTokenRepository.findById(refreshToken.getId()).orElseGet(RefreshTokenEntity::new);

        UserEntity user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new InvalidDataException("Nguoi dung khong ton tai"));

        entity.setId(refreshToken.getId());
        entity.setUser(user);
        entity.setTokenHash(refreshToken.getTokenHash());
        entity.setExpiresAt(refreshToken.getExpiresAt());
        entity.setRevokedAt(refreshToken.getRevokedAt());
        entity.setReplacedByTokenId(refreshToken.getReplacedByTokenId());
        entity.setCreatedByIp(refreshToken.getCreatedByIp());
        entity.setUserAgent(refreshToken.getUserAgent());

        return toRefreshToken(refreshTokenRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return refreshTokenRepository.findByTokenHash(tokenHash).map(this::toRefreshToken);
    }

    @Override
    @Transactional
    public void revokeAllActiveByUserId(Long userId) {
        refreshTokenRepository.revokeAllActiveByUserId(userId, ZonedDateTime.now());
    }

    private RefreshToken toRefreshToken(RefreshTokenEntity entity) {
        return RefreshToken.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .tokenHash(entity.getTokenHash())
                .expiresAt(entity.getExpiresAt())
                .revokedAt(entity.getRevokedAt())
                .replacedByTokenId(entity.getReplacedByTokenId())
                .createdAt(entity.getCreatedAt())
                .createdByIp(entity.getCreatedByIp())
                .userAgent(entity.getUserAgent())
                .build();
    }
}
