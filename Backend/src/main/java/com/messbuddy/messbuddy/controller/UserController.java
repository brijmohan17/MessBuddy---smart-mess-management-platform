package com.messbuddy.messbuddy.controller;

import com.messbuddy.messbuddy.entity.User;
import com.messbuddy.messbuddy.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/signout")
	public ResponseEntity<?> signout() {
		ResponseCookie cookie = ResponseCookie.from("access_token", "")
				.httpOnly(true)
				.secure(true)
				.sameSite("None")
				.path("/")
				.maxAge(0)
				.build();
		return ResponseEntity.ok().header("Set-Cookie", cookie.toString())
				.body(Map.of("success", true, "message", "User has been signed out"));
	}

	@DeleteMapping("/delete-account/{userId}")
	public ResponseEntity<?> deleteUserAccount(@PathVariable String userId) {
		userService.deleteUserAccount(userId);
		return ResponseEntity.ok(Map.of("success", true, "message", "User account deleted successfully"));
	}

	@GetMapping("/getuser/{userId}")
	public ResponseEntity<?> getUser(@PathVariable String userId) {
		User user = userService.getUser(userId);
		return ResponseEntity.ok(Map.of("username", user.getUsername(), "role", user.getLoginRoleValue()));
	}
}
