package com.bteam.platform.adapter.persistence.jpa;

import com.bteam.platform.adapter.persistence.jpa.entity.PermissionEntity;
import com.bteam.platform.adapter.persistence.jpa.entity.RoleEntity;
import com.bteam.platform.adapter.persistence.jpa.entity.UserEntity;
import com.bteam.platform.adapter.persistence.jpa.repository.RoleRepository;
import com.bteam.platform.adapter.persistence.jpa.repository.UserRepository;
import com.bteam.platform.core.auth.model.Account;
import com.bteam.platform.core.auth.port.AccountStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JpaAccountStore implements AccountStore {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findByEmail(String email) {
        return userRepository.findByEmail(email).map(this::toAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findByResetPasswordToken(String token) {
        return userRepository.findByResetPasswordToken(token).map(this::toAccount);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    @Transactional
    public Account save(Account account) {
        UserEntity entity = account.getId() == null
                ? new UserEntity()
                : userRepository.findById(account.getId()).orElseGet(UserEntity::new);

        entity.setId(account.getId());
        entity.setEmail(account.getEmail());
        entity.setPasswordHash(account.getPasswordHash());
        entity.setPhoneNumber(account.getPhoneNumber());
        entity.setFullName(account.getFullName());
        entity.setAvatarUrl(account.getAvatarUrl());
        entity.setStatus(account.getStatus());
        entity.setLastLogin(account.getLastLogin());
        entity.setResetPasswordToken(account.getResetPasswordToken());
        entity.setResetPasswordTokenExpiry(account.getResetPasswordTokenExpiry());

        Set<RoleEntity> roles = new HashSet<>(roleRepository.findByNameIn(account.getRoles()));
        entity.setRoles(roles);

        return toAccount(userRepository.save(entity));
    }

    private Account toAccount(UserEntity entity) {
        Set<String> roles = entity.getRoles()
                .stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());
        Set<String> permissions = entity.getRoles()
                .stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(PermissionEntity::getName)
                .collect(Collectors.toSet());

        return Account.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .phoneNumber(entity.getPhoneNumber())
                .fullName(entity.getFullName())
                .avatarUrl(entity.getAvatarUrl())
                .status(entity.getStatus())
                .lastLogin(entity.getLastLogin())
                .resetPasswordToken(entity.getResetPasswordToken())
                .resetPasswordTokenExpiry(entity.getResetPasswordTokenExpiry())
                .roles(roles)
                .permissions(permissions)
                .build();
    }
}
