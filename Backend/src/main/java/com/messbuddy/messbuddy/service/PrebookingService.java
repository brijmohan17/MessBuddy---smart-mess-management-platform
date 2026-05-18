package com.messbuddy.messbuddy.service;

import com.messbuddy.messbuddy.entity.Menu;
import com.messbuddy.messbuddy.entity.Mess;
import com.messbuddy.messbuddy.entity.Prebooking;
import com.messbuddy.messbuddy.entity.User;
import com.messbuddy.messbuddy.repository.AuthRepository;
import com.messbuddy.messbuddy.repository.MenuRepository;
import com.messbuddy.messbuddy.repository.MessRepository;
import com.messbuddy.messbuddy.repository.PrebookingRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PrebookingService {

    private final PrebookingRepository prebookingRepository;
    private final MenuRepository menuRepository;
    private final MessRepository messRepository;
    private final AuthRepository authRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:${EMAIL_USER:}}")
    private String emailUser;

    public Map<String, Object> createPrebooking(Map<String, Object> body) {
        String menuId = stringValue(body.get("menuId"));
        String messId = stringValue(body.get("messId"));
        String userId = stringValue(body.get("userId"));
        User user = requireUser(userId);
        Mess mess = requireMess(messId);
        requireMenu(menuId);
        // Validate required fields
        String date = stringValue(body.get("date"));
        String time = stringValue(body.get("time"));
        Integer quantity = integerValue(body.get("quantity"));
        if (date == null || date.isBlank() || time == null || time.isBlank() || quantity == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date, time and quantity are required");
        }
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be greater than 0");
        }
        Prebooking prebooking = Prebooking.builder()
                .menuId(menuId)
                .messId(messId)
                .userId(userId)
                .date(stringValue(body.get("date")))
                .time(stringValue(body.get("time")))
                .quantity(integerValue(body.get("quantity")))
                .status("Pending")
                .build();
        Prebooking saved = prebookingRepository.save(prebooking);

        // Send email but do not fail the request if email sending fails
        try {
            sendEmail(user.getEmail(), "Prebooking Confirmation",
                    buildCreateEmail(user.getUsername(), mess.getMess_Name(), saved));
        } catch (Exception e) {
            // Log and continue
            System.err.println("Warning: failed to send prebooking confirmation email: " + e.getMessage());
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Prebooking created successfully, and email notification sent.");
        response.put("prebooking", toPrebookingResponse(saved));
        return response;
    }

    public List<Map<String, Object>> getUserPrebookings(String userId) {
        requireUser(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Prebooking prebooking : prebookingRepository.findByUserId(userId)) {
            result.add(toPrebookingResponse(prebooking));
        }
        return result;
    }

    public Map<String, Object> getAllPrebookings() {
        List<Map<String, Object>> all = new ArrayList<>();
        for (Prebooking prebooking : prebookingRepository.findAll()) {
            all.add(toPrebookingResponse(prebooking));
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("prebooking", all);
        return response;
    }

    public List<Map<String, Object>> getMessPrebookings(String messId) {
        requireMess(messId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Prebooking prebooking : prebookingRepository.findByMessId(messId)) {
            result.add(toPrebookingResponse(prebooking));
        }
        return result;
    }

    public Map<String, Object> updateStatus(String prebookingId, Map<String, Object> body) {
        String status = stringValue(body.get("status"));
        if (!List.of("Pending", "Confirmed", "Cancelled").contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status.");
        }

        Prebooking prebooking = prebookingRepository.findById(prebookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prebooking not found"));
        prebooking.setStatus(status);
        Prebooking saved = prebookingRepository.save(prebooking);

        User user = requireUser(prebooking.getUserId());
        Mess mess = requireMess(prebooking.getMessId());
        try {
            sendEmail(user.getEmail(), "Prebooking Status Update: " + status,
                    buildStatusEmail(user.getUsername(), mess.getMess_Name(), saved, status));
        } catch (Exception e) {
            System.err.println("Warning: failed to send status update email: " + e.getMessage());
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Prebooking status updated successfully, and email notification sent.");
        response.put("prebooking", toPrebookingResponse(saved));
        return response;
    }

    public void deletePrebooking(String prebookingId) {
        Prebooking prebooking = prebookingRepository.findById(prebookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prebooking not found"));
        prebookingRepository.delete(prebooking);
    }

    private Map<String, Object> toPrebookingResponse(Prebooking prebooking) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_id", prebooking.getId());
        response.put("menuId", requireMenu(prebooking.getMenuId()));
        response.put("messId", requireMess(prebooking.getMessId()));
        response.put("userId", requireUser(prebooking.getUserId()));
        response.put("date", prebooking.getDate());
        response.put("time", prebooking.getTime());
        response.put("quantity", prebooking.getQuantity());
        response.put("status", prebooking.getStatus());
        return response;
    }

    private User requireUser(String userId) {
        return authRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Mess requireMess(String messId) {
        return messRepository.findById(messId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mess not found"));
    }

    private Menu requireMenu(String menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu not found"));
    }

    private void sendEmail(String to, String subject, String text) {
        if (to == null || to.isBlank() || emailUser == null || emailUser.isBlank()) {
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(emailUser);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);
            mailSender.send(message);
        } catch (Exception exception) {
            // do not fail API call on email errors; log and continue
            System.err.println("Failed to send email to " + to + ": " + exception.getMessage());
        }
    }

    private String buildCreateEmail(String username, String messName, Prebooking prebooking) {
        return "Dear " + username + ",\n\nThank you for your prebooking request with " + messName + ".\n\n" +
                "Your prebooking details are as follows:\n- Date: " + prebooking.getDate() +
                "\n- Time: " + prebooking.getTime() +
                "\n- Quantity: " + prebooking.getQuantity() +
                "\n- Mess Name: " + messName +
                "\n\nYour request has been successfully submitted and is currently under review. You will receive an email notification once your prebooking status is updated.\n\nKind regards,\n" + messName + " Team";
    }

    private String buildStatusEmail(String username, String messName, Prebooking prebooking, String status) {
        return switch (status) {
            case "Pending" -> "Dear " + username + ",\n\nThank you for your prebooking with " + messName + ". We have received your request and it is currently pending.\n\nPrebooking Details:\n- Date: " + prebooking.getDate() + "\n- Time: " + prebooking.getTime() + "\n- Quantity: " + prebooking.getQuantity() + "\n- Mess Name: " + messName + "\n\nKind regards,\n" + messName + " Team";
            case "Confirmed" -> "Dear " + username + ",\n\nWe are pleased to inform you that your prebooking with " + messName + " has been confirmed!\n\nPrebooking Details:\n- Date: " + prebooking.getDate() + "\n- Time: " + prebooking.getTime() + "\n- Quantity: " + prebooking.getQuantity() + "\n- Mess Name: " + messName + "\n\nKind regards,\n" + messName + " Team";
            case "Cancelled" -> "Dear " + username + ",\n\nWe regret to inform you that your prebooking with " + messName + " has been cancelled.\n\nPrebooking Details:\n- Date: " + prebooking.getDate() + "\n- Time: " + prebooking.getTime() + "\n- Quantity: " + prebooking.getQuantity() + "\n- Mess Name: " + messName + "\n\nKind regards,\n" + messName + " Team";
            default -> "";
        };
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer integerValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }
}
