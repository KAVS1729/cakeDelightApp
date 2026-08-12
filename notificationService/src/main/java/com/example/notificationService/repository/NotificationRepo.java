package com.example.notificationService.repository;

import com.example.notificationService.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepo extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserId(String userId);
}
