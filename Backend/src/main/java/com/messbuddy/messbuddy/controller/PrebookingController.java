package com.messbuddy.messbuddy.controller;

import com.messbuddy.messbuddy.service.PrebookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/prebooking")
@RequiredArgsConstructor
public class PrebookingController {

    private final PrebookingService prebookingService;

    @PostMapping
    public ResponseEntity<?> createPrebooking(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(prebookingService.createPrebooking(body));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserPrebookings(@PathVariable String userId) {
        return ResponseEntity.ok(prebookingService.getUserPrebookings(userId));
    }

    @GetMapping
    public ResponseEntity<?> getAllPrebookings() {
        return ResponseEntity.ok(prebookingService.getAllPrebookings());
    }

    @GetMapping("/mess/{messId}")
    public ResponseEntity<?> getMessPrebookings(@PathVariable String messId) {
        return ResponseEntity.ok(prebookingService.getMessPrebookings(messId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(prebookingService.updateStatus(id, body));
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> deletePrebooking(@PathVariable String bookingId) {
        prebookingService.deletePrebooking(bookingId);
        return ResponseEntity.ok(Map.of("message", "Prebooking deleted successfully"));
    }
}
