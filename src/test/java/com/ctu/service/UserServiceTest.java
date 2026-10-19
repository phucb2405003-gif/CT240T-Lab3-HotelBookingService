package com.ctu.service;

import com.ctu.model.User;
import com.ctu.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Test Suite")
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private User newUser;

    @BeforeEach
    void setUp() {
        newUser = new User("U001", "An", "an@ctu.edu.vn");
    }

    @Test
    @DisplayName("Đăng ký thành công và gửi email chào mừng")
    void registerUser_success() {
        when(userRepository.existsByEmail(newUser.getEmail())).thenReturn(false);
        when(userRepository.save(newUser)).thenReturn(newUser);

        assertTrue(userService.registerUser(newUser));

        verify(userRepository).save(newUser);
        verify(emailService).sendWelcomeEmail(newUser.getEmail());
    }

    @Test
    @DisplayName("Email đã tồn tại thì ném ngoại lệ")
    void registerUser_duplicateEmail_throwsException() {
        when(userRepository.existsByEmail(newUser.getEmail())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(newUser));

        assertEquals("Email da ton tai!", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendWelcomeEmail(any());
    }

    @Test
    @DisplayName("Thông tin người dùng null không hợp lệ")
    void registerUser_nullUser_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(null));
        verifyNoInteractions(userRepository, emailService);
    }

    @Test
    @DisplayName("Email null không hợp lệ")
    void registerUser_nullEmail_throwsException() {
        User user = new User("U002", "Binh", null);
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(user));
        verifyNoInteractions(userRepository, emailService);
    }

    @Test
    @DisplayName("Email rỗng không hợp lệ")
    void registerUser_emptyEmail_throwsException() {
        User user = new User("U003", "Chi", "   ");
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(user));
        verifyNoInteractions(userRepository, emailService);
    }

    @Test
    @DisplayName("Repository không lưu được thì trả về false và không gửi email")
    void registerUser_repositoryReturnsNull_returnsFalse() {
        when(userRepository.existsByEmail(newUser.getEmail())).thenReturn(false);
        when(userRepository.save(newUser)).thenReturn(null);

        assertFalse(userService.registerUser(newUser));
        verify(emailService, never()).sendWelcomeEmail(any());
    }
}
