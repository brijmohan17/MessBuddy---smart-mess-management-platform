package com.messbuddy.messbuddy.controller;

import com.messbuddy.messbuddy.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping("/{ownerOrMessId}")
    public ResponseEntity<?> markAttendance(@PathVariable String ownerOrMessId, @RequestBody Map<String, Object> body) {
        return ResponseEntity.status(201).body(checkInService.markAttendance(ownerOrMessId, body));
    }

    @GetMapping("/today-stats/{ownerOrMessId}")
    public ResponseEntity<?> getTodayStats(@PathVariable String ownerOrMessId) {
        return ResponseEntity.ok(checkInService.getTodayStats(ownerOrMessId));
    }

    @GetMapping("/{ownerOrMessId}")
    public ResponseEntity<?> getAttendanceRecords(@PathVariable String ownerOrMessId, @RequestParam(required = false) String date) {
        return ResponseEntity.ok(checkInService.getAttendanceRecords(ownerOrMessId, date));
    }
}
