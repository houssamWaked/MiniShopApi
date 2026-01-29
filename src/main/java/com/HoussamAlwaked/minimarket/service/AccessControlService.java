package com.HoussamAlwaked.minimarket.service;

import com.HoussamAlwaked.minimarket.entity.User;
import com.HoussamAlwaked.minimarket.entity.UserRole;
import com.HoussamAlwaked.minimarket.exception.BadRequestException;
import com.HoussamAlwaked.minimarket.exception.ForbiddenException;
import com.HoussamAlwaked.minimarket.exception.NotFoundException;
import com.HoussamAlwaked.minimarket.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService {

    private static final String USER_ID_HEADER = "X-USER-ID";
    private static final String USER_EMAIL_HEADER = "X-USER-EMAIL";
    private static final String SUPER_ADMIN_EMAIL_ENV = "SUPER_ADMIN_EMAIL";

    private final UserRepository userRepository;

    public AccessControlService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User requireUser(HttpServletRequest request) {
        String userId = readHeader(request, USER_ID_HEADER);
        String email = readHeader(request, USER_EMAIL_HEADER);

        String superAdminEmail = System.getenv(SUPER_ADMIN_EMAIL_ENV);
        if (email != null && superAdminEmail != null
                && email.toLowerCase(Locale.ROOT).equals(superAdminEmail.toLowerCase(Locale.ROOT))) {
            return ensureSuperAdmin(email, userId);
        }

        Optional<User> user = Optional.empty();
        if (userId != null) {
            user = userRepository.findById(userId);
        }
        if (user.isEmpty() && email != null) {
            user = userRepository.findByEmail(email);
        }
        if (user.isEmpty() && email != null) {
            user = userRepository.findByEmailIgnoreCase(email);
        }
        if (userId == null && email == null) {
            throw new BadRequestException("X-USER-ID or X-USER-EMAIL header is required.");
        }

        return user.orElseThrow(() -> new NotFoundException("User not found."));
    }

    public User requireSuperAdmin(HttpServletRequest request) {
        User user = requireUser(request);
        if (user.getRole() != UserRole.SUPER_ADMIN) {
            throw new ForbiddenException("Super admin access required.");
        }
        return user;
    }

    public User requireStoreManager(HttpServletRequest request, String storeId) {
        User user = requireUser(request);
        if (user.getRole() == UserRole.SUB_ADMIN && storeId != null
                && storeId.equals(user.getAssignedStoreId())) {
            return user;
        }
        throw new ForbiddenException("Store manager access required.");
    }

    public boolean isSuperAdmin(User user) {
        return user != null && user.getRole() == UserRole.SUPER_ADMIN;
    }

    public boolean isSubAdmin(User user) {
        return user != null && user.getRole() == UserRole.SUB_ADMIN;
    }

    private User ensureSuperAdmin(String email, String userId) {
        Optional<User> existing = userRepository.findByEmail(email.trim().toLowerCase());
        if (existing.isPresent()) {
            User user = existing.get();
            if (user.getRole() != UserRole.SUPER_ADMIN) {
                user.setRole(UserRole.SUPER_ADMIN);
                userRepository.save(user);
            }
            return user;
        }

        User user = new User();
        user.setId(userId == null || userId.isBlank() ? UUID.randomUUID().toString() : userId);
        user.setEmail(email.trim().toLowerCase());
        user.setRole(UserRole.SUPER_ADMIN);
        return userRepository.save(user);
    }

    private String readHeader(HttpServletRequest request, String name) {
        if (request == null) {
            return null;
        }
        String value = request.getHeader(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
