package com.ridelink.account.service;

import com.ridelink.account.dto.*;
import com.ridelink.account.exception.ApiException;
import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Role;
import com.ridelink.account.model.UserAccount;
import com.ridelink.account.repository.UserAccountRepository;
import com.ridelink.account.security.JwtService;
import com.ridelink.account.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AccountService(UserAccountRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.role() == Role.ADMIN) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_ROLE",
                    "Admin accounts cannot be self-registered");
        }
        if (repository.existsByEmailIgnoreCase(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_EXISTS", "Email is already registered");
        }
        UserAccount account = new UserAccount();
        account.setEmail(request.email().trim().toLowerCase());
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setFullName(request.fullName().trim());
        account.setPhone(request.phone());
        account.setRole(request.role());
        account.setStatus(AccountStatus.ACTIVE);
        UserAccount saved = repository.save(account);
        return issueToken(saved);
    }

    public AuthResponse login(LoginRequest request) {
        UserAccount account = repository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                        "Invalid email or password"));
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_INACTIVE",
                    "Account is " + account.getStatus());
        }
        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                    "Invalid email or password");
        }
        return issueToken(account);
    }

    public UserResponse getById(Long id) {
        return toResponse(find(id));
    }

    public UserResponse getMe(UserPrincipal principal) {
        return toResponse(find(principal.getId()));
    }

    @Transactional
    public UserResponse updateProfile(Long id, UpdateProfileRequest request, UserPrincipal actor) {
        UserAccount account = find(id);
        boolean self = actor.getId().equals(id);
        boolean admin = "ADMIN".equals(actor.getRole());
        if (!self && !admin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Cannot update another user's profile");
        }
        if (request.fullName() != null && !request.fullName().isBlank()) {
            account.setFullName(request.fullName().trim());
        }
        if (request.phone() != null) {
            account.setPhone(request.phone());
        }
        if (request.status() != null) {
            if (!admin) {
                throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only ADMIN can change account status");
            }
            account.setStatus(request.status());
        }
        return toResponse(repository.save(account));
    }

    private UserAccount find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));
    }

    private AuthResponse issueToken(UserAccount account) {
        String token = jwtService.generateToken(account.getId(), account.getEmail(), account.getRole().name());
        return AuthResponse.bearer(token, toResponse(account));
    }

    private UserResponse toResponse(UserAccount account) {
        return new UserResponse(
                account.getId(),
                account.getEmail(),
                account.getFullName(),
                account.getPhone(),
                account.getRole(),
                account.getStatus());
    }
}
