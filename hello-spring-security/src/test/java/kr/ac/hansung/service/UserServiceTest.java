package kr.ac.hansung.service;

import kr.ac.hansung.entity.User;
import kr.ac.hansung.repository.RoleRepository;
import kr.ac.hansung.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@DisplayName("UserService 테스트")
class UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserService userService = new UserService(userRepository, roleRepository, passwordEncoder);

    @Test
    @DisplayName("현재 비밀번호가 일치하면 새 비밀번호를 BCrypt로 인코딩해 저장한다")
    void changePassword_matchingCurrentPassword_encodesNewPassword() {
        User user = new User();
        user.setEmail("user@hansung.ac.kr");
        user.setPassword(passwordEncoder.encode("oldPassword"));
        given(userRepository.findByEmail("user@hansung.ac.kr")).willReturn(Optional.of(user));

        userService.changePassword("user@hansung.ac.kr", "oldPassword", "newPassword123");

        assertThat(passwordEncoder.matches("newPassword123", user.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("oldPassword", user.getPassword())).isFalse();
        then(userRepository).should().save(user);
    }

    @Test
    @DisplayName("현재 비밀번호가 일치하지 않으면 예외를 발생시키고 저장하지 않는다")
    void changePassword_wrongCurrentPassword_throwsException() {
        User user = new User();
        user.setEmail("user@hansung.ac.kr");
        user.setPassword(passwordEncoder.encode("oldPassword"));
        given(userRepository.findByEmail("user@hansung.ac.kr")).willReturn(Optional.of(user));

        assertThatThrownBy(() ->
            userService.changePassword("user@hansung.ac.kr", "wrongPassword", "newPassword123")
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("현재 비밀번호가 일치하지 않습니다");

        then(userRepository).should(never()).save(any(User.class));
    }
}
