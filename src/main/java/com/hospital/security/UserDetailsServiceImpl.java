package com.hospital.security;

import com.hospital.entity.User;
import com.hospital.repository.UserRepository;
import com.hospital.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .filter(foundUser -> Boolean.TRUE.equals(foundUser.getIsActive()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found or inactive: " + username));

        List<String> roles = userRoleRepository.findByUserIdWithRole(user.getId()).stream()
                .map(userRole -> userRole.getRole().getRoleName().name())
                .toList();

        return UserPrincipal.from(user, roles);
    }
}
