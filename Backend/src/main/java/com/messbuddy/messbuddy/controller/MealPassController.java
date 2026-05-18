package com.messbuddy.messbuddy.controller;

import com.messbuddy.messbuddy.service.MealPassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/mealpass")
@RequiredArgsConstructor
public class MealPassController {

    private final MealPassService mealPassService;

    @GetMapping("/current/{userId}")
    public ResponseEntity<?> getCurrentMealPass(@PathVariable String userId) {
        return ResponseEntity.ok(mealPassService.getCurrentMealPasses(userId));
    }

    @PostMapping("/validate/{ownerOrMessId}")
    public ResponseEntity<?> validateMealPass(@PathVariable String ownerOrMessId, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(mealPassService.validateMealPass(ownerOrMessId, body));
    }
}
