package com.bteam.platform.adapter.persistence.jpa.repository;

import com.bteam.platform.adapter.persistence.jpa.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            update RefreshTokenEntity token
            set token.revokedAt = :revokedAt
            where token.user.id = :userId
              and token.revokedAt is null
              and token.expiresAt > :revokedAt
            """)
    void revokeAllActiveByUserId(@Param("userId") Long userId, @Param("revokedAt") ZonedDateTime revokedAt);
}
