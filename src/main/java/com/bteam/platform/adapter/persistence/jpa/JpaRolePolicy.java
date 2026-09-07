package com.bteam.platform.adapter.persistence.jpa;

import com.bteam.platform.adapter.persistence.jpa.entity.PermissionEntity;
import com.bteam.platform.adapter.persistence.jpa.entity.RoleEntity;
import com.bteam.platform.adapter.persistence.jpa.repository.RoleRepository;
import com.bteam.platform.core.auth.config.AuthProperties;
import com.bteam.platform.core.auth.port.RolePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JpaRolePolicy implements RolePolicy {
    private final AuthProperties authProperties;
    private final RoleRepository roleRepository;

    @Override
    public boolean isRegistrationRoleAllowed(String role) {
        String normalizedRole = role.toUpperCase();
        return authProperties.allowedRoles()
                .stream()
                .map(String::toUpperCase)
                .anyMatch(normalizedRole::equals);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> normalizeRole(String role) {
        String normalizedRole = role.trim().toUpperCase();
        return roleRepository.existsByName(normalizedRole)
                ? Optional.of(normalizedRole)
                : Optional.empty();
    }

    @Override
    public String defaultRole() {
        return authProperties.defaultRole().trim().toUpperCase();
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> permissionsForRoles(Set<String> roles) {
        return roleRepository.findByNameIn(roles)
                .stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(PermissionEntity::getName)
                .collect(Collectors.toSet());
    }
}
