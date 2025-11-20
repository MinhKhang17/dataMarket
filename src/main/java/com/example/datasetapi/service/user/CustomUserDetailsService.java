package com.example.datasetapi.service.user;

import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Collection<GrantedAuthority> authorities = mapAuthorities(user);

        // Sử dụng builder của Spring Security, có thể set accountLocked/expired/credentialsExpired/disabled nếu cần
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) // đảm bảo đã encode (BCrypt)
                .authorities(authorities)
                .disabled(!user.isActive()) // nếu user.isActive() false => disabled
                .build();
    }

    private Collection<GrantedAuthority> mapAuthorities(User user) {
        if (user.getRole() == null) {
            // fallback: user không có role
            return Collections.emptyList();
        }
        // nếu Role.getName() lưu "ADMIN" không có prefix ROLE_ -> thêm prefix
        String roleName = user.getRole().getName();
        String authority = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        return List.of(new SimpleGrantedAuthority(authority));
    }
}
