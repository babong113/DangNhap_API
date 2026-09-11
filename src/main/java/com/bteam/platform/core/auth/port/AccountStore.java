package com.bteam.platform.core.auth.port;

import com.bteam.platform.core.auth.model.Account;

import java.util.Optional;

public interface AccountStore {
    Optional<Account> findById(Long id);
    Optional<Account> findByEmail(String email);
    Optional<Account> findByResetPasswordToken(String token);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    Account save(Account account);
}
