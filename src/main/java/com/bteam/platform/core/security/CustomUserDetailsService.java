package com.bteam.platform.core.security;

import com.bteam.platform.core.auth.model.Account;
import com.bteam.platform.core.auth.model.AccountStatus;
import com.bteam.platform.core.auth.port.AccountStore;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final AccountStore accountStore;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountStore.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("Khong tim thay nguoi dung voi email: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(account.getEmail())
                .password(account.getPasswordHash())
                .authorities(Stream.concat(
                                account.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)),
                                account.getPermissions().stream().map(SimpleGrantedAuthority::new)
                        )
                        .toList())
                .disabled(account.getStatus() != AccountStatus.ACTIVE)
                .build();
    }
}
