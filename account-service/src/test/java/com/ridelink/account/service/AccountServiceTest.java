package com.ridelink.account.service;

import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.exception.ApiException;
import com.ridelink.account.model.Role;
import com.ridelink.account.model.UserAccount;
import com.ridelink.account.repository.UserAccountRepository;
import com.ridelink.account.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock UserAccountRepository repository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;

    @InjectMocks AccountService accountService;

    @BeforeEach
    void stubs() {
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        lenient().when(jwtService.generateToken(any(), anyString(), anyString())).thenReturn("token-123");
    }

    @Test
    void register_createsPassengerAndReturnsToken() {
        when(repository.existsByEmailIgnoreCase("a@test.com")).thenReturn(false);
        when(repository.save(any(UserAccount.class))).thenAnswer(inv -> {
            UserAccount a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        var response = accountService.register(new RegisterRequest(
                "a@test.com", "secret1", "Ayesha", "077", Role.PASSENGER));

        assertEquals("token-123", response.accessToken());
        assertEquals(Role.PASSENGER, response.user().role());
        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(repository).save(captor.capture());
        assertEquals("a@test.com", captor.getValue().getEmail());
        assertEquals("hashed", captor.getValue().getPasswordHash());
    }

    @Test
    void register_rejectsDuplicateEmail() {
        when(repository.existsByEmailIgnoreCase("a@test.com")).thenReturn(true);
        ApiException ex = assertThrows(ApiException.class, () ->
                accountService.register(new RegisterRequest(
                        "a@test.com", "secret1", "Ayesha", null, Role.PASSENGER)));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("EMAIL_EXISTS", ex.getError());
    }

    @Test
    void register_rejectsAdminSelfRegistration() {
        ApiException ex = assertThrows(ApiException.class, () ->
                accountService.register(new RegisterRequest(
                        "admin@test.com", "secret1", "Admin", null, Role.ADMIN)));
        assertEquals("INVALID_ROLE", ex.getError());
        verify(repository, never()).save(any());
    }

    @Test
    void login_rejectsBadPassword() {
        UserAccount account = new UserAccount();
        account.setId(1L);
        account.setEmail("a@test.com");
        account.setPasswordHash("hashed");
        account.setFullName("A");
        account.setRole(Role.PASSENGER);
        when(repository.findByEmailIgnoreCase("a@test.com")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () ->
                accountService.login(new LoginRequest("a@test.com", "wrong")));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }
}
