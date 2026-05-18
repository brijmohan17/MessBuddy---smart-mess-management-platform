package com.messbuddy.messbuddy.controller;

import com.messbuddy.messbuddy.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/plans")
    public ResponseEntity<?> createPlan(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(201).body(subscriptionService.createPlan(body));
    }

    @PutMapping("/plans/{planId}")
    public ResponseEntity<?> updatePlan(@PathVariable String planId, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(subscriptionService.updatePlan(planId, body));
    }

    @DeleteMapping("/plans/{planId}/{messId}")
    public ResponseEntity<?> deletePlan(@PathVariable String planId, @PathVariable String messId) {
        subscriptionService.deletePlan(planId, messId);
        return ResponseEntity.ok(Map.of("message", "Plan deleted successfully and related subscriptions updated"));
    }

    @GetMapping("/mess/{messId}/plans")
    public ResponseEntity<?> getMessPlans(@PathVariable String messId) {
        return ResponseEntity.ok(subscriptionService.getMessPlans(messId));
    }

    @GetMapping("/mess/{messId}/subscribers")
    public ResponseEntity<?> getMessSubscribers(@PathVariable String messId) {
        return ResponseEntity.ok(subscriptionService.getMessSubscribers(messId));
    }

    @GetMapping("/plans")
    public ResponseEntity<?> getAllPlans() {
        return ResponseEntity.ok(subscriptionService.getAllPlans());
    }

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribeToPlan(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(201).body(subscriptionService.subscribeToPlan(body));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserSubscriptions(@PathVariable String userId) {
        return ResponseEntity.ok(subscriptionService.getUserSubscriptions(userId));
    }

    @PutMapping("/cancel/{subscriptionId}")
    public ResponseEntity<?> cancelSubscription(@PathVariable String subscriptionId, @RequestBody Map<String, Object> body) {
        String userId = body.get("userId") == null ? null : String.valueOf(body.get("userId"));
        return ResponseEntity.ok(Map.of("message", "Subscription cancelled successfully", "subscription", subscriptionService.cancelSubscription(subscriptionId, userId)));
    }

    @PutMapping("/{subscriptionId}/activate")
    public ResponseEntity<?> activateSubscription(@PathVariable String subscriptionId, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(subscriptionService.activateSubscription(subscriptionId, body));
    }
}
