package com.messbuddy.messbuddy.service;

import com.messbuddy.messbuddy.entity.CheckIn;
import com.messbuddy.messbuddy.entity.MealPass;
import com.messbuddy.messbuddy.entity.Mess;
import com.messbuddy.messbuddy.entity.SubscriptionPlan;
import com.messbuddy.messbuddy.entity.User;
import com.messbuddy.messbuddy.entity.UserSubscription;
import com.messbuddy.messbuddy.repository.AuthRepository;
import com.messbuddy.messbuddy.repository.CheckInRepository;
import com.messbuddy.messbuddy.repository.MealPassRepository;
import com.messbuddy.messbuddy.repository.MessRepository;
import com.messbuddy.messbuddy.repository.SubscriptionPlanRepository;
import com.messbuddy.messbuddy.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private final MessRepository messRepository;
    private final CheckInRepository checkInRepository;
    private final MealPassRepository mealPassRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final AuthRepository authRepository;

    public Map<String, Object> markAttendance(String ownerOrMessId, Map<String, Object> body) {
        Mess mess = resolveMess(ownerOrMessId);
        Set<String> acceptedMessIds = resolveAcceptedMessIds(mess);
        String mealPassId = stringValue(body.get("mealPassId"));
        String mealType = normalizeMealType(stringValue(body.get("mealType")));

        if (mealPassId == null || mealPassId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Meal pass is required");
        }
        if (mealType == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Meal type is required");
        }

        MealPass mealPass = mealPassRepository.findById(mealPassId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal pass not found"));

        if (!acceptedMessIds.contains(mealPass.getMessId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Meal pass is not valid for this mess");
        }

        validateMealPassState(mealPass);

        UserSubscription subscription = userSubscriptionRepository.findById(mealPass.getSubscriptionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Subscription is not active"));
        if (!Objects.equals(subscription.getStatus(), "Active")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Subscription is not active");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        boolean alreadyCheckedIn = findCheckInsForMess(mess, startOfDay, endOfDay)
                .stream()
                .anyMatch(record -> Objects.equals(record.getMealPassId(), mealPassId)
                        && Objects.equals(normalizeMealType(record.getMealType()), mealType));

        if (alreadyCheckedIn) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Attendance already marked for this meal");
        }

        CheckIn checkIn = CheckIn.builder()
                .userId(mealPass.getUserId())
            .messId(mealPass.getMessId())
                .mealPassId(mealPassId)
                .mealType(mealType)
                .status("success")
                .createdAt(now)
                .build();

        CheckIn saved = checkInRepository.save(checkIn);
        return Map.of(
                "success", true,
                "message", "Attendance marked successfully",
                "checkIn", toCheckInResponse(saved)
        );
    }

    public Map<String, Integer> getTodayStats(String ownerOrMessId) {
        Mess mess = resolveMess(ownerOrMessId);
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        return buildMealCounts(findCheckInsForMess(mess, startOfDay, endOfDay));
    }

    public List<Map<String, Object>> getAttendanceRecords(String ownerOrMessId, String dateValue) {
        Mess mess = resolveMess(ownerOrMessId);
        LocalDate targetDate = (dateValue == null || dateValue.isBlank())
                ? LocalDate.now()
                : LocalDate.parse(dateValue);
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.plusDays(1).atStartOfDay();

        return findCheckInsForMess(mess, startOfDay, endOfDay)
                .stream()
                .sorted(Comparator.comparing(CheckIn::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::toCheckInResponse)
                .toList();
    }

    private List<CheckIn> findCheckInsForMess(Mess mess, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        Set<String> acceptedMessIds = resolveAcceptedMessIds(mess);
        List<CheckIn> records = new ArrayList<>();
        for (String messId : acceptedMessIds) {
            records.addAll(checkInRepository.findByMessIdAndCreatedAtBetweenAndStatus(messId, startOfDay, endOfDay, "success"));
        }
        return records;
    }

    private Set<String> resolveAcceptedMessIds(Mess mess) {
        Set<String> ids = new LinkedHashSet<>();
        if (mess.getId() != null && !mess.getId().isBlank()) {
            ids.add(mess.getId());
        }
        if (mess.getOwner_ID() != null && !mess.getOwner_ID().isBlank()) {
            ids.add(mess.getOwner_ID());
        }
        return ids;
    }

    private Map<String, Integer> buildMealCounts(List<CheckIn> records) {
        int breakfast = 0;
        int lunch = 0;
        int dinner = 0;

        for (CheckIn record : records) {
            String mealType = normalizeMealType(record.getMealType());
            if ("breakfast".equals(mealType)) {
                breakfast++;
            } else if ("lunch".equals(mealType)) {
                lunch++;
            } else if ("dinner".equals(mealType)) {
                dinner++;
            }
        }

        return Map.of(
                "breakfast", breakfast,
                "lunch", lunch,
                "dinner", dinner
        );
    }

    private void validateMealPassState(MealPass mealPass) {
        if (Boolean.TRUE.equals(mealPass.getIsBlocked())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is blocked");
        }
        if (!Boolean.TRUE.equals(mealPass.getIsActive()) || mealPass.getValidTill() == null || mealPass.getValidTill().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Subscription expired");
        }
    }

    private Map<String, Object> toCheckInResponse(CheckIn checkIn) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_id", checkIn.getId());
        response.put("userId", toUserResponse(checkIn.getUserId()));
        response.put("messId", checkIn.getMessId());
        response.put("mealPassId", toMealPassResponse(checkIn.getMealPassId()));
        response.put("mealType", checkIn.getMealType());
        response.put("status", checkIn.getStatus());
        response.put("failureReason", checkIn.getFailureReason());
        response.put("createdAt", checkIn.getCreatedAt());
        return response;
    }

    private Map<String, Object> toMealPassResponse(String mealPassId) {
        MealPass mealPass = mealPassRepository.findById(mealPassId).orElse(null);
        if (mealPass == null) {
            return Map.of("_id", mealPassId);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_id", mealPass.getId());
        response.put("userId", toUserResponse(mealPass.getUserId()));
        response.put("subscriptionId", toSubscriptionResponse(mealPass.getSubscriptionId()));
        response.put("messId", mealPass.getMessId());
        response.put("qrCode", mealPass.getQrCode());
        response.put("isActive", mealPass.getIsActive());
        response.put("isBlocked", mealPass.getIsBlocked());
        response.put("blockReason", mealPass.getBlockReason());
        response.put("validFrom", mealPass.getValidFrom());
        response.put("validTill", mealPass.getValidTill());
        response.put("createdAt", mealPass.getCreatedAt());
        return response;
    }

    private Map<String, Object> toSubscriptionResponse(String subscriptionId) {
        UserSubscription subscription = userSubscriptionRepository.findById(subscriptionId).orElse(null);
        if (subscription == null) {
            return Map.of("_id", subscriptionId);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_id", subscription.getId());
        response.put("userId", toUserResponse(subscription.getUserId()));
        response.put("planId", toPlanResponse(subscription.getPlanId()));
        response.put("startDate", subscription.getStartDate());
        response.put("endDate", subscription.getEndDate());
        response.put("status", subscription.getStatus());
        response.put("paymentId", subscription.getPaymentId());
        response.put("paymentStatus", subscription.getPaymentStatus());
        response.put("cancellationReason", subscription.getCancellationReason());
        response.put("createdAt", subscription.getCreatedAt());
        return response;
    }

    private Map<String, Object> toPlanResponse(String planId) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId).orElse(null);
        if (plan == null) {
            return Map.of("_id", planId);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_id", plan.getId());
        response.put("messId", plan.getMessId());
        response.put("planName", plan.getPlanName());
        response.put("duration", plan.getDuration());
        response.put("mealType", plan.getMealType());
        response.put("price", plan.getPrice());
        response.put("description", plan.getDescription());
        response.put("isActive", plan.getIsActive());
        return response;
    }

    private Map<String, Object> toUserResponse(String userId) {
        User user = authRepository.findById(userId).orElse(null);
        if (user == null) {
            return Map.of("_id", userId);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_id", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("Login_Role", user.getLoginRoleValue());
        return response;
    }

    private Mess resolveMess(String ownerOrMessId) {
        return messRepository.findByOwnerId(ownerOrMessId)
                .orElseGet(() -> messRepository.findById(ownerOrMessId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mess not found")));
    }

    private String normalizeMealType(String mealType) {
        return mealType == null ? null : mealType.trim().toLowerCase();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }
}