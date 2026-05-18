package com.messbuddy.messbuddy.service;

import com.messbuddy.messbuddy.dto.SignInResponseDto;
import com.messbuddy.messbuddy.dto.SignInRequestDto;
import com.messbuddy.messbuddy.dto.SignUpRequestDto;
import com.messbuddy.messbuddy.dto.SignUpResponseDto;
import com.messbuddy.messbuddy.entity.Mess;
import com.messbuddy.messbuddy.entity.User;
import com.messbuddy.messbuddy.entity.type.LoginRole;
import com.messbuddy.messbuddy.repository.AuthRepository;
import com.messbuddy.messbuddy.repository.MessRepository;
import com.messbuddy.messbuddy.security.JwtService;
import lombok.Builder;
import lombok.AllArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthService {
    private final AuthRepository authRepository;
    private final MessRepository messRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthResult<SignUpResponseDto> signUp(SignUpRequestDto signUpRequestDto){
        String username = signUpRequestDto.getUsername().trim();
        String email = signUpRequestDto.getEmail().trim().toLowerCase(Locale.ROOT);
        LoginRole loginRole = LoginRole.fromValue(signUpRequestDto.getLoginRole());

        if (authRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (authRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        long nowMillis = System.currentTimeMillis();
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(signUpRequestDto.getPassword()))
                .loginRole(loginRole)
                .userId(nowMillis)
                .build();

        User savedUser = authRepository.save(user);

        if (loginRole == LoginRole.MESS_OWNER) {
            Mess mess = Mess.builder()
                    .messId(System.currentTimeMillis())
                    .Mess_Name("Mess" + (int) (Math.random() * 1000))
                    .Mobile_No("")
                    .Capacity(0)
                    .Address("")
                    .Owner_ID(savedUser.getId())
                    .Description("")
                    .UserID(nowMillis)
                    .Image("http://res.cloudinary.com/dq3ro4o3c/image/upload/v1734445757/gngcgm82wwo5t0desu0w.jpg")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            messRepository.save(mess);
        }

        String token = jwtService.generateToken(savedUser);

        SignUpResponseDto response = SignUpResponseDto.builder()
                .success(true)
                .message("User created successfully")
                .user(SignUpResponseDto.UserSummary.builder()
                        .username(savedUser.getUsername())
                        .email(savedUser.getEmail())
                        .loginRole(savedUser.getLoginRoleValue())
                        .build())
                .build();

        return AuthResult.<SignUpResponseDto>builder()
                .body(response)
                .token(token)
                .build();
    }

    public AuthResult<SignInResponseDto> signIn(SignInRequestDto signInRequestDto) {
        String username = signInRequestDto.getUsername().trim();
        LoginRole loginRole = LoginRole.fromValue(signInRequestDto.getLoginRole());

        User user = authRepository.findByUsernameAndLoginRole(username, loginRole)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Credentials"));

        if (!passwordEncoder.matches(signInRequestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid Credentials");
        }

        String token = jwtService.generateToken(user);

        SignInResponseDto response = SignInResponseDto.builder()
                .success(true)
                .user(SignInResponseDto.UserSummary.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .loginRole(user.getLoginRoleValue())
                        .build())
                .token(token)
                .build();

        return AuthResult.<SignInResponseDto>builder()
                .body(response)
                .token(token)
                .build();
    }

    @Builder
    public static class AuthResult<T> {
        private T body;
        private String token;

        public T getBody() {
            return body;
        }

        public String getToken() {
            return token;
        }
    }
}
