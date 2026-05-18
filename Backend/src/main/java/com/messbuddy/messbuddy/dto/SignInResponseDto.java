package com.messbuddy.messbuddy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignInResponseDto {
	private boolean success;
	private UserSummary user;
	private String token;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class UserSummary {
		@JsonProperty("_id")
		private String id;
		private String username;
		private String email;
		@JsonProperty("Login_Role")
		private String loginRole;
	}
}
