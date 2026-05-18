package com.messbuddy.messbuddy.controller;

import com.messbuddy.messbuddy.dto.FeedbackResponseDto;
import com.messbuddy.messbuddy.entity.User;
import com.messbuddy.messbuddy.entity.UserFeedback;
import com.messbuddy.messbuddy.repository.AuthRepository;
import com.messbuddy.messbuddy.repository.UserFeedbackRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

	private final UserFeedbackRepository feedbackRepository;
	private final AuthRepository authRepository;

	public FeedbackController(UserFeedbackRepository feedbackRepository, AuthRepository authRepository) {
		this.feedbackRepository = feedbackRepository;
		this.authRepository = authRepository;
	}

	@PostMapping
	public ResponseEntity<Map<String, Object>> createFeedback(@RequestBody UserFeedback feedback) {
		if (feedback.getUserID() == null || feedback.getComments() == null || feedback.getRating() == null) {
			return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
		}

		if (feedback.getSubmittedAt() == null) {
			feedback.setSubmittedAt(LocalDateTime.now());
		}

		UserFeedback savedFeedback = feedbackRepository.save(feedback);
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
				"message", "Feedback submitted successfully",
				"feedback", toDto(savedFeedback)
		));
	}

	@GetMapping
	public ResponseEntity<Map<String, List<FeedbackResponseDto>>> getAllFeedbacks() {
		List<FeedbackResponseDto> feedbacks = feedbackRepository.findAll().stream()
				.sorted(Comparator.comparing(UserFeedback::getSubmittedAt, Comparator.nullsLast(Comparator.reverseOrder())))
				.map(this::toDto)
				.toList();

		return ResponseEntity.ok(Map.of("feedbacks", feedbacks));
	}

	private FeedbackResponseDto toDto(UserFeedback feedback) {
		User user = feedback.getUserID() == null ? null : authRepository.findById(feedback.getUserID()).orElse(null);
		return new FeedbackResponseDto(
				feedback.getId(),
				user == null ? null : new FeedbackResponseDto.UserSummary(user.getId(), user.getUsername()),
				feedback.getComments(),
				feedback.getRating(),
				feedback.getSubmittedAt()
		);
	}
}
