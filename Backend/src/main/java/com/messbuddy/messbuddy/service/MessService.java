package com.messbuddy.messbuddy.service;

import com.messbuddy.messbuddy.entity.Mess;
import com.messbuddy.messbuddy.repository.MessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessService {

    private final MessRepository messRepository;

    public Mess createMess(String ownerId, Mess request) {
        Mess mess = Mess.builder()
                .messId(System.currentTimeMillis())
                .Mess_Name(request.getMess_Name())
                .Mobile_No(request.getMobile_No())
                .Capacity(request.getCapacity())
                .Address(request.getAddress())
                .Owner_ID(ownerId)
                .Description(request.getDescription())
                .Ratings(request.getRatings() == null ? new ArrayList<>() : request.getRatings())
                .RatedBy(request.getRatedBy() == null ? new ArrayList<>() : request.getRatedBy())
                .UserID(request.getUserID() == null ? System.currentTimeMillis() : request.getUserID())
                .Image(request.getImage())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return messRepository.save(mess);
    }

    public Mess updateMess(String ownerId, Mess request) {
        Mess mess = messRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mess not found"));

        mess.setMess_Name(request.getMess_Name());
        mess.setMobile_No(request.getMobile_No());
        mess.setCapacity(request.getCapacity());
        mess.setAddress(request.getAddress());
        mess.setDescription(request.getDescription());
        mess.setImage(request.getImage());
        mess.setUpdatedAt(LocalDateTime.now());
        return messRepository.save(mess);
    }

    public List<Mess> getAllMess(String searchTerm) {
        List<Mess> messes = messRepository.findAll();
        if (searchTerm != null && !searchTerm.isBlank()) {
            String term = searchTerm.toLowerCase();
            messes = messes.stream()
                    .filter(mess -> mess.getMess_Name() != null && mess.getMess_Name().toLowerCase().contains(term))
                    .toList();
        }
        if (messes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No messes found");
        }
        return messes.stream().sorted(Comparator.comparing(Mess::getMess_Name, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    public Mess getMess(String ownerId) {
        return messRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mess not found"));
    }

    public Mess readMess(String id) {
        return messRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mess not found"));
    }

    public void deleteMess(String ownerId) {
        Mess mess = messRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mess not found"));
        messRepository.delete(mess);
    }

    public double getRating(String id) {
        Mess mess = readMess(id);
        if (mess.getRatings() == null || mess.getRatings().isEmpty()) {
            return 0.0;
        }
        return mess.getRatings().stream().mapToDouble(Integer::doubleValue).average().orElse(0.0);
    }

    public RatingCheckResult hasUserRated(String messId, String userId) {
        Mess mess = readMess(messId);
        int index = mess.getRatedBy() == null ? -1 : mess.getRatedBy().indexOf(userId);
        boolean hasRated = index != -1;
        int rating = hasRated && mess.getRatings() != null && index < mess.getRatings().size() ? mess.getRatings().get(index) : 0;
        return new RatingCheckResult(hasRated, rating);
    }

    public double updateRating(String id, String userId, Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }
        Mess mess = readMess(id);
        if (mess.getRatedBy() != null && mess.getRatedBy().contains(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already rated this mess");
        }
        mess.getRatings().add(rating);
        mess.getRatedBy().add(userId);
        messRepository.save(mess);
        return getRating(id);
    }

    public record RatingCheckResult(boolean hasRated, int rating) {}
}
