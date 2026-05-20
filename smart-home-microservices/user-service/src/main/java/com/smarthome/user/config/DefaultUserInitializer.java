package com.smarthome.user.config;

import com.smarthome.user.entity.User;
import com.smarthome.user.entity.UserRole;
import com.smarthome.user.repository.RoleRepository;
import com.smarthome.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultUserInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void initDefaultUsers() {
        UserRole adminRole = roleRepository.findByRoleName("ROLE_ADMIN")
                .orElseGet(() -> {
                    UserRole role = new UserRole();
                    role.setRoleName("ROLE_ADMIN");
                    role.setDescription("系统管理员");
                    role.setPermissions(Collections.emptyList());
                    return roleRepository.save(role);
                });

        roleRepository.findByRoleName("ROLE_USER").orElseGet(() -> {
            UserRole role = new UserRole();
            role.setRoleName("ROLE_USER");
            role.setDescription("普通用户");
            role.setPermissions(Collections.emptyList());
            return roleRepository.save(role);
        });

        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setEmail("admin@example.com");
            admin.setFullName("系统管理员");
            admin.setEnabled(true);
            admin.setRole(adminRole);
            userRepository.save(admin);
            log.info("已创建默认管理员账号: admin / Admin@123");
        }
    }
}
