package com.example.demo.service;

import com.example.demo.dto.AlertDTO;
import com.example.demo.entity.Alert;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AlertRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Alert> getAlertsByUser(Long userId) {
        return alertRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    public List<Alert> getUnreadAlerts(Long userId) {
        return alertRepository.findByUser_IdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public Long countUnreadAlerts(Long userId) {
        return alertRepository.countByUser_IdAndIsReadFalse(userId);
    }

    public Alert createAlert(AlertDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + dto.getUserId() + " not found."));

        Alert alert = new Alert();
        alert.setUser(user);
        alert.setType(dto.getType());
        alert.setMessage(dto.getMessage());
        alert.setCategory(dto.getCategory());
        alert.setIsRead(false);
        alert.setCreatedAt(LocalDateTime.now());
        return alertRepository.save(alert);
    }

    public Alert markAsRead(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert with ID " + id + " not found."));
        alert.setIsRead(true);
        return alertRepository.save(alert);
    }

    public void markAllAsRead(Long userId) {
        List<Alert> alerts = alertRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        for (Alert alert : alerts) {
            alert.setIsRead(true);
        }
        alertRepository.saveAll(alerts);
    }

    public void deleteAlert(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert with ID " + id + " not found."));
        alertRepository.delete(alert);
    }

    public void triggerBudgetAlert(Long userId, String category, Double percentUsed) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found."));

        Alert alert = new Alert();
        alert.setUser(user);
        alert.setType("THRESHOLD_REACHED");
        alert.setCategory(category);
        alert.setMessage(String.format("You've used %.1f%% of your budget for %s.", percentUsed, category));
        alert.setIsRead(false);
        alert.setCreatedAt(LocalDateTime.now());
        alertRepository.save(alert);
    }
}
