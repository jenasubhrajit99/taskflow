package com.taskflow.user;

import com.taskflow.common.exception.ResourceNotFoundException;
import com.taskflow.common.exception.ValidationException;
import com.taskflow.user.dto.request.ChangePasswordRequest;
import com.taskflow.user.entity.User;
import com.taskflow.user.entity.UserRole;
import com.taskflow.user.entity.UserStatus;
import com.taskflow.user.mapper.UserMapper;
import com.taskflow.user.repository.UserRepository;
import com.taskflow.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void findUserEntityByEmail_whenNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserEntityByEmail("missing@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    void findUserEntityByEmail_whenFound_returnsUser() {
        User user = buildUser("found@example.com");
        when(userRepository.findByEmail("found@example.com")).thenReturn(Optional.of(user));

        User result = userService.findUserEntityByEmail("found@example.com");

        assert result.getEmail().equals("found@example.com");
    }

    @Test
    void changePassword_whenCurrentPasswordWrong_throwsValidationException() {
        User user = buildUser("user@example.com");
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

        ChangePasswordRequest req = mock(ChangePasswordRequest.class);
        when(req.getCurrentPassword()).thenReturn("wrong");

        assertThatThrownBy(() -> userService.changePassword(user.getId(), req))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Current password is incorrect");
    }

    @Test
    void changePassword_whenPasswordsDoNotMatch_throwsValidationException() {
        User user = buildUser("user@example.com");
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        ChangePasswordRequest req = mock(ChangePasswordRequest.class);
        when(req.getCurrentPassword()).thenReturn("current");
        when(req.getNewPassword()).thenReturn("newpass1");
        when(req.getConfirmPassword()).thenReturn("newpass2");

        assertThatThrownBy(() -> userService.changePassword(user.getId(), req))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("do not match");
    }

    private User buildUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("encoded_password");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        return user;
    }
}
