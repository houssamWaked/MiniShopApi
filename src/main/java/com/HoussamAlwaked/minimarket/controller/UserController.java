package com.HoussamAlwaked.minimarket.controller;

import com.HoussamAlwaked.minimarket.dto.UserRequest;
import com.HoussamAlwaked.minimarket.entity.User;
import com.HoussamAlwaked.minimarket.entity.UserRole;
import com.HoussamAlwaked.minimarket.repository.UserRepository;
import com.HoussamAlwaked.minimarket.service.AccessControlService;
import com.HoussamAlwaked.minimarket.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AccessControlService accessControlService;

    public UserController(UserService userService,
                          UserRepository userRepository,
                          AccessControlService accessControlService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.accessControlService = accessControlService;
    }

    @PostMapping("/api/users")
    public ResponseEntity<User> registerCustomer(@RequestBody UserRequest request) {
        User created = userService.create(request, UserRole.CUSTOMER);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/api/users/me")
    public User getCurrentUser(HttpServletRequest servletRequest) {
        return accessControlService.requireUser(servletRequest);
    }

    @PostMapping("/api/admin/users")
    public ResponseEntity<User> createUser(@RequestBody UserRequest request,
                                           HttpServletRequest servletRequest) {
        accessControlService.requireSuperAdmin(servletRequest);
        User created = userService.create(request, request.getRole());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/api/admin/users/{id}")
    public User updateUser(@PathVariable String id,
                           @RequestBody UserRequest request,
                           HttpServletRequest servletRequest) {
        accessControlService.requireSuperAdmin(servletRequest);
        return userService.update(id, request);
    }

    @GetMapping("/api/admin/users")
    public List<User> listUsers(HttpServletRequest servletRequest) {
        accessControlService.requireSuperAdmin(servletRequest);
        return userRepository.findAll();
    }
}
