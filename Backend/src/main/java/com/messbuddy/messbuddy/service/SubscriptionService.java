package com.messbuddy.messbuddy.service;

import com.messbuddy.messbuddy.entity.MealPass;
import com.messbuddy.messbuddy.entity.Mess;
import com.messbuddy.messbuddy.entity.SubscriptionPlan;
import com.messbuddy.messbuddy.entity.User;
import com.messbuddy.messbuddy.entity.UserSubscription;
import com.messbuddy.messbuddy.entity.type.LoginRole;
import com.messbuddy.messbuddy.repository.AuthRepository;
import com.messbuddy.messbuddy.repository.MealPassRepository;
import com.messbuddy.messbuddy.repository.MessRepository;
import com.messbuddy.messbuddy.repository.SubscriptionPlanRepository;
import com.messbuddy.messbuddy.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final AuthRepository authRepository;
    private final MessRepository messRepository;
    private final MealPassRepository mealPassRepository;

    public SubscriptionPlan createPlan(Map<String, Object> body) {
        String userId = stringValue(body.get("userId"));
        User user = requireUser(userId);
        if (user.getLoginRole() != LoginRole.MESS_OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only mess owners can create subscription plans");
        }

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .messId(userId)
                .planName(stringValue(body.get("planName")))
                .duration(stringValue(body.get("duration")))
                .mealType(stringValue(body.get("mealType")))
                .price(doubleValue(body.get("price")))
                .description(stringValue(body.get("description")))
                .isActive(true)
                .build();
        return subscriptionPlanRepository.save(plan);
    }

    public SubscriptionPlan updatePlan(String planId, Map<String, Object> body) {
        String userId = stringValue(body.get("userId"));
        User user = requireUser(userId);
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));

        if (user.getLoginRole() != LoginRole.MESS_OWNER || !Objects.equals(plan.getMessId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own plans");
        }

        if (body.containsKey("planName")) plan.setPlanName(stringValue(body.get("planName")));
        if (body.containsKey("duration")) plan.setDuration(stringValue(body.get("duration")));
        if (body.containsKey("mealType")) plan.setMealType(stringValue(body.get("mealType")));
        if (body.containsKey("price")) plan.setPrice(doubleValue(body.get("price")));
        if (body.containsKey("description")) plan.setDescription(stringValue(body.get("description")));

        return subscriptionPlanRepository.save(plan);
    }

    public void deletePlan(String planId, String messId) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));

        if (!Objects.equals(plan.getMessId(), messId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own plans");
        }

        List<UserSubscription> subscriptions = userSubscriptionRepository.findAll().stream()
                .filter(subscription -> Objects.equals(subscription.getPlanId(), planId))
                .toList();
        for (UserSubscription subscription : subscriptions) {
            subscription.setStatus("Plan Removed");
            subscription.setEndDate(LocalDateTime.now());
            subscription.setCancellationReason("Plan deleted by mess owner");
            userSubscriptionRepository.save(subscription);
        }

        subscriptionPlanRepository.delete(plan);
    }

    public List<Map<String, Object>> getMessPlans(String messId) {
        return subscriptionPlanRepository.findByMessIdAndIsActive(messId, true).stream()
                .map(this::toPlanResponse)
                .toList();
    }

    public List<Map<String, Object>> getAllPlans() {
        return subscriptionPlanRepository.findAll().stream()
                .filter(plan -> Boolean.TRUE.equals(plan.getIsActive()))
                .map(this::toPlanResponse)
                .toList();
    }

    public UserSubscription subscribeToPlan(Map<String, Object> body) {
        String planId = stringValue(body.get("planId"));
        String userId = stringValue(body.get("userId"));
        User user = requireUser(userId);
        if (user.getLoginRole() != LoginRole.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only users can subscribe to plans");
        }

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found or inactive"));
        if (!Boolean.TRUE.equals(plan.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found or inactive");
        }

        boolean alreadySubscribed = userSubscriptionRepository.findAll().stream()
                .anyMatch(subscription -> Objects.equals(subscription.getUserId(), userId) && Objects.equals(subscription.getPlanId(), planId));
        if (alreadySubscribed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already subscribed to this plan before. Each plan can only be subscribed once.");
        }

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = calculateEndDate(startDate, plan.getDuration());

        UserSubscription subscription = UserSubscription.builder()
                .userId(userId)
                .planId(planId)
                .startDate(startDate)
                .endDate(endDate)
                .status("Pending")
                .paymentStatus("Pending")
                .build();
        UserSubscription savedSubscription = userSubscriptionRepository.save(subscription);

        MealPass mealPass = MealPass.builder()
                .userId(userId)
                .subscriptionId(savedSubscription.getId())
                .messId(plan.getMessId())
                .qrCode(generateQrCode(userId, savedSubscription.getId(), plan.getMessId(), plan.getMealType()))
                .validFrom(startDate)
                .validTill(endDate)
                .isActive(true)
                .isBlocked(false)
                .build();
        mealPassRepository.save(mealPass);

        return savedSubscription;
    }

    public List<Map<String, Object>> getUserSubscriptions(String userId) {
        requireUser(userId);
        return userSubscriptionRepository.findByUserId(userId).stream()
                .sorted(Comparator.comparing(UserSubscription::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::toUserSubscriptionResponse)
                .toList();
    }

    public List<Map<String, Object>> getMessSubscribers(String messId) {
        List<UserSubscription> subscriptions = userSubscriptionRepository.findAll().stream()
                .filter(subscription -> {
                    SubscriptionPlan plan = subscriptionPlanRepository.findById(subscription.getPlanId()).orElse(null);
                    return plan != null && Objects.equals(plan.getMessId(), messId);
                })
                .sorted(Comparator.comparing(UserSubscription::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (UserSubscription subscription : subscriptions) {
            result.add(toMessSubscriberResponse(subscription));
        }
        return result;
    }

    public UserSubscription cancelSubscription(String subscriptionId, String userId) {
        UserSubscription subscription = userSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found"));
        if (!Objects.equals(subscription.getUserId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only cancel your own subscriptions");
        }
        if (!Objects.equals(subscription.getStatus(), "Active")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subscription is not active");
        }
        subscription.setStatus("Cancelled");
        return userSubscriptionRepository.save(subscription);
    }

    public UserSubscription activateSubscription(String subscriptionId, Map<String, Object> body) {
        UserSubscription subscription = userSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found"));
        SubscriptionPlan plan = subscriptionPlanRepository.findById(subscription.getPlanId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));

        String status = body.containsKey("status") ? stringValue(body.get("status")) : "Active";
        if (body.containsKey("messId") && !Objects.equals(plan.getMessId(), stringValue(body.get("messId")))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to update this subscription");
        }

        if ("Active".equals(status)) {
            subscription.setStartDate(LocalDateTime.now());
            subscription.setEndDate(calculateEndDate(subscription.getStartDate(), plan.getDuration()));
        } else if ("Cancelled".equals(status) || "Expired".equals(status)) {
            subscription.setEndDate(LocalDateTime.now());
        }
        subscription.setStatus(status);
        UserSubscription saved = userSubscriptionRepository.save(subscription);

        mealPassRepository.findBySubscriptionId(saved.getId()).ifPresent(mealPass -> {
            mealPass.setValidFrom(saved.getStartDate());
            mealPass.setValidTill(saved.getEndDate());
            mealPass.setIsActive(!"Cancelled".equals(status));
            mealPassRepository.save(mealPass);
        });

        return saved;
    }

    public List<Map<String, Object>> getCurrentMealPasses(String userId) {
        requireUser(userId);
        return mealPassRepository.findByUserIdAndIsActiveTrueAndValidTillAfter(userId, LocalDateTime.now()).stream()
                .map(this::toMealPassResponse)
                .toList();
    }

    public Map<String, Object> validateMealPass(String messId, String qrCode) {
        MealPass mealPass = mealPassRepository.findByQrCode(qrCode);
        if (mealPass == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid QR code");
        }
        if (!Objects.equals(mealPass.getMessId(), messId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "QR code not valid for this mess");
        }
        if (Boolean.TRUE.equals(mealPass.getIsBlocked())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is blocked");
        }

        UserSubscription subscription = userSubscriptionRepository.findById(mealPass.getSubscriptionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Subscription is not active"));
        if (!"Active".equals(subscription.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Subscription is not active");
        }
        if (!Boolean.TRUE.equals(mealPass.getIsActive()) || mealPass.getValidTill().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Subscription expired");
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("valid", true);
        response.put("mealPass", toDetailedMealPassResponse(mealPass));
        return response;
    }

    private Map<String, Object> toPlanResponse(SubscriptionPlan plan) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_id", plan.getId());
        response.put("messId", plan.getMessId());
        response.put("planName", plan.getPlanName());
        response.put("duration", plan.getDuration());
        response.put("mealType", plan.getMealType());
        response.put("price", plan.getPrice());
        response.put("description", plan.getDescription());
        response.put("isActive", plan.getIsActive());
        Mess mess = messRepository.findByOwnerId(plan.getMessId()).orElse(null);
        if (mess != null) {
            response.put("messDetails", mess);
        }
        return response;
    }

    private Map<String, Object> toUserSubscriptionResponse(UserSubscription subscription) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_id", subscription.getId());
        response.put("userId", requireUser(subscription.getUserId()));
        response.put("planId", toPlanResponse(subscriptionPlanRepository.findById(subscription.getPlanId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"))));
        response.put("startDate", subscription.getStartDate());
        response.put("endDate", subscription.getEndDate());
        response.put("status", subscription.getStatus());
        response.put("paymentId", subscription.getPaymentId());
        response.put("paymentStatus", subscription.getPaymentStatus());
        response.put("cancellationReason", subscription.getCancellationReason());
        return response;
    }

    private Map<String, Object> toMessSubscriberResponse(UserSubscription subscription) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_id", subscription.getId());
        response.put("userId", Map.of(
                "_id", requireUser(subscription.getUserId()).getId(),
                "username", requireUser(subscription.getUserId()).getUsername(),
                "email", requireUser(subscription.getUserId()).getEmail(),
                "Login_Role", requireUser(subscription.getUserId()).getLoginRoleValue()));
        response.put("planId", toPlanResponse(subscriptionPlanRepository.findById(subscription.getPlanId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"))));
        response.put("startDate", subscription.getStartDate());
        response.put("endDate", subscription.getEndDate());
        response.put("status", subscription.getStatus());
        response.put("paymentId", subscription.getPaymentId());
        response.put("paymentStatus", subscription.getPaymentStatus());
        response.put("cancellationReason", subscription.getCancellationReason());
        return response;
    }

    private Map<String, Object> toMealPassResponse(MealPass mealPass) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_id", mealPass.getId());
        response.put("userId", requireUser(mealPass.getUserId()));
        response.put("subscriptionId", toSubscriptionWithPlan(mealPass.getSubscriptionId()));
        response.put("messId", mealPass.getMessId());
        Mess mess = messRepository.findByOwnerId(mealPass.getMessId()).orElse(null);
        if (mess != null) {
            response.put("messDetails", mess);
        }
        response.put("qrCode", mealPass.getQrCode());
        response.put("isActive", mealPass.getIsActive());
        response.put("isBlocked", mealPass.getIsBlocked());
        response.put("blockReason", mealPass.getBlockReason());
        response.put("validFrom", mealPass.getValidFrom());
        response.put("validTill", mealPass.getValidTill());
        return response;
    }

    private Map<String, Object> toDetailedMealPassResponse(MealPass mealPass) {
        return toMealPassResponse(mealPass);
    }

    private Map<String, Object> toSubscriptionWithPlan(String subscriptionId) {
        UserSubscription subscription = userSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found"));
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_id", subscription.getId());
        response.put("userId", requireUser(subscription.getUserId()));
        response.put("planId", toPlanResponse(subscriptionPlanRepository.findById(subscription.getPlanId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"))));
        response.put("startDate", subscription.getStartDate());
        response.put("endDate", subscription.getEndDate());
        response.put("status", subscription.getStatus());
        response.put("paymentId", subscription.getPaymentId());
        response.put("paymentStatus", subscription.getPaymentStatus());
        response.put("cancellationReason", subscription.getCancellationReason());
        return response;
    }

    private User requireUser(String userId) {
        return authRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private LocalDateTime calculateEndDate(LocalDateTime startDate, String duration) {
        return switch (duration) {
            case "Daily" -> startDate.plusDays(1);
            case "Weekly" -> startDate.plusDays(7);
            case "Monthly" -> startDate.plusMonths(1);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid duration");
        };
    }

    private String generateQrCode(String userId, String subscriptionId, String messId, String mealType) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = userId + ":" + subscriptionId + ":" + messId + ":" + mealType + ":" + System.currentTimeMillis();
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Unable to generate QR code hash", exception);
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Double doubleValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(String.valueOf(value));
    }
}
