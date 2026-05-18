package com.messbuddy.messbuddy.controller;

import com.messbuddy.messbuddy.entity.Mess;
import com.messbuddy.messbuddy.service.MessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mess")
@RequiredArgsConstructor
public class MessController {

	private final MessService messService;

	@PostMapping("/create/{ownerId}")
	public ResponseEntity<?> createMess(@PathVariable String ownerId, @RequestBody Mess request) {
		Mess mess = messService.createMess(ownerId, request);
		return ResponseEntity.status(201).body(Map.of("success", true, "message", "Mess created successfully", "mess", mess));
	}

	@PutMapping("/update/{ownerId}")
	public ResponseEntity<?> updateMess(@PathVariable String ownerId, @RequestBody Mess request) {
		Mess mess = messService.updateMess(ownerId, request);
		return ResponseEntity.ok(Map.of("success", true, "message", "Mess updated successfully", "mess", mess));
	}

	@GetMapping
	public ResponseEntity<?> getAllMess(@RequestParam(required = false) String searchTerm) {
		return ResponseEntity.ok(Map.of("success", true, "messes", messService.getAllMess(searchTerm)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getMess(@PathVariable String id) {
		return ResponseEntity.ok(Map.of("success", true, "mess", messService.getMess(id)));
	}

	@GetMapping("/read/{id}")
	public ResponseEntity<?> readMess(@PathVariable String id) {
		return ResponseEntity.ok(Map.of("success", true, "mess", messService.readMess(id)));
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteMess(@PathVariable String id) {
		messService.deleteMess(id);
		return ResponseEntity.ok(Map.of("success", true, "message", "Mess deleted successfully"));
	}

	@GetMapping("/rating/{id}")
	public ResponseEntity<?> getRating(@PathVariable String id) {
		return ResponseEntity.ok(Map.of("success", true, "rating", messService.getRating(id)));
	}

	@GetMapping("/hasrated/{messId}/{userId}")
	public ResponseEntity<?> hasUserRated(@PathVariable String messId, @PathVariable String userId) {
		MessService.RatingCheckResult result = messService.hasUserRated(messId, userId);
		return ResponseEntity.ok(Map.of("success", true, "hasRated", result.hasRated(), "rating", result.rating()));
	}

	@PutMapping("/rating/{id}/{userId}")
	public ResponseEntity<?> updateRating(@PathVariable String id, @PathVariable String userId, @RequestBody Map<String, Integer> body) {
		Integer rating = body.get("rating");
		return ResponseEntity.ok(Map.of("success", true, "message", "Rating submitted successfully", "rating", messService.updateRating(id, userId, rating)));
	}
}
