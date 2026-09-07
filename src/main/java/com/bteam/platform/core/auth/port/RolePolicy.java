package com.bteam.platform.core.auth.port;

import java.util.Optional;
import java.util.Set;

public interface RolePolicy {
    boolean isRegistrationRoleAllowed(String role);
    Optional<String> normalizeRole(String role);
    String defaultRole();
    Set<String> permissionsForRoles(Set<String> roles);
}
