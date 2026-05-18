package com.messbuddy.messbuddy.service;

import com.messbuddy.messbuddy.entity.Mess;
import com.messbuddy.messbuddy.repository.MessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MealPassService {

    private final SubscriptionService subscriptionService;
    private final MessRepository messRepository;

    public List<Map<String, Object>> getCurrentMealPasses(String userId) {
        return subscriptionService.getCurrentMealPasses(userId);
    }

    public Map<String, Object> validateMealPass(String ownerOrMessId, Map<String, Object> body) {
        String qrCode = body.get("qrCode") == null ? null : String.valueOf(body.get("qrCode"));
        if (qrCode == null || qrCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "QR code is required");
        }

        String messId = resolveMessId(ownerOrMessId);
        return subscriptionService.validateMealPass(messId, qrCode);
    }

    private String resolveMessId(String ownerOrMessId) {
        Mess mess = messRepository.findByOwnerId(ownerOrMessId)
                .orElseGet(() -> messRepository.findById(ownerOrMessId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mess not found")));
        // Existing meal-pass records store mess reference as owner id.
        if (mess.getOwner_ID() != null && !mess.getOwner_ID().isBlank()) {
            return mess.getOwner_ID();
        }
        return Objects.requireNonNull(mess.getId(), "Mess id is required");
    }
}
