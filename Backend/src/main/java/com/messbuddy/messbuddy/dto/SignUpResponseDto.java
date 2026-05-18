package com.messbuddy.messbuddy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpResponseDto {
	private boolean success;
	private String message;
	private UserSummary user;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class UserSummary {
		private String username;
		private String email;
		private String loginRole;
	}
}
