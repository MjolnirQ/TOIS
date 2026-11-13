package ru.nsu.tois.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.tois.entity.user.Permission;
import ru.nsu.tois.entity.user.Role;
import ru.nsu.tois.entity.user.UserAccount;
import ru.nsu.tois.repository.user.PermissionRepository;
import ru.nsu.tois.repository.user.UserAccountRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private static final String SUPERADMIN_ROLE = "SUPERADMIN_ROLE";

    private final UserAccountRepository userAccountRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        UserAccount user = userAccountRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.builder()
                .username(user.getLogin())
                .password(user.getPasswordHash())
                .disabled(!Boolean.TRUE.equals(user.getIsActive()))
                .authorities(
                        buildAuthorities(user)
                ).build();
    }

    private List<SimpleGrantedAuthority> buildAuthorities(UserAccount user) {
        Role role = user.getRole();
        if (role == null) {
            return List.of();
        }

        if (SUPERADMIN_ROLE.equals(role.getName())) {
            return permissionRepository.findAll().stream()
                    .map(Permission::getCode)
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        }

        if (role.getPermissions() == null) {
            return List.of();
        }

        return role.getPermissions().stream()
                .map(Permission::getCode)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
