package com.messbuddy.messbuddy.controller;

import com.messbuddy.messbuddy.dto.SignInRequestDto;
import com.messbuddy.messbuddy.dto.SignInResponseDto;
import com.messbuddy.messbuddy.dto.SignUpRequestDto;
import com.messbuddy.messbuddy.dto.SignUpResponseDto;
import com.messbuddy.messbuddy.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDto> signUp(@Valid @RequestBody SignUpRequestDto signUpRequestDto){
    AuthService.AuthResult<SignUpResponseDto> result = authService.signUp(signUpRequestDto);
    ResponseCookie cookie = ResponseCookie.from("access_token", result.getToken())
        .httpOnly(true)
        .secure(true)
        .sameSite("None")
        .path("/")
        .build();

    return ResponseEntity.status(201)
        .header("Set-Cookie", cookie.toString())
        .body(result.getBody());
    }

    @PostMapping("/signin")
    public ResponseEntity<SignInResponseDto> signIn(@Valid @RequestBody SignInRequestDto signInRequestDto){
    AuthService.AuthResult<SignInResponseDto> result = authService.signIn(signInRequestDto);
    ResponseCookie cookie = ResponseCookie.from("access_token", result.getToken())
        .httpOnly(true)
        .secure(true)
        .sameSite("None")
        .path("/")
        .build();

    return ResponseEntity.status(200)
        .header("Set-Cookie", cookie.toString())
        .body(result.getBody());
    }

}
