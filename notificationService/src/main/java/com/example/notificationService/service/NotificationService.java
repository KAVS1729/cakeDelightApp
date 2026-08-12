package com.example.notificationService.service;

import com.example.notificationService.dto.CustomerOrderDto;
import com.example.notificationService.dto.OrderItemDto;
import com.example.notificationService.model.Notification;
import com.example.notificationService.repository.NotificationRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Service
public class NotificationService {

    @Autowired
    private NotificationRepo notificationRepo;

    @Autowired
    private JavaMailSender mailSender;

    @RabbitListener(queues = "order.completion.queue")
    public void handleOrderCompletion(CustomerOrderDto order) {
        log.info("Received order completion event: orderId={}, userId={}", order.getId(), order.getUserId());
        StringBuilder itemsList = new StringBuilder();
        if (order.getItems() != null) {
            for (OrderItemDto item : order.getItems()) {
                itemsList.append("- ").append(item.getCakeName())
                        .append(" x").append(item.getQuantity())
                        .append(" (Rs. ").append(item.getPrice()).append(")\n");
            }
        }

        String message = "Your order #" + order.getId() + " has been confirmed!\n\n"
                + "Delivery Address: " + order.getAddress()+ "\n\n"
                + "Items:\n" + itemsList
                + "\nTotal: Rs. " + order.getTotal();

        Notification notification = new Notification();
        notification.setUserId(order.getUserId());
        notification.setMessage(message);
        notification.setStatus("SENT");
        notification.setSentAt(LocalDateTime.now());
        notificationRepo.save(notification);
        log.info("In-app notification saved: orderId={}, userId={}", order.getId(), order.getUserId());
        try {
            if (order.getEmail() != null && !order.getEmail().isBlank()) {
                sendEmail(order.getEmail(), "Order Confirmation - Cake Delight", message);
                log.info("Email notification sent successfully to={}, orderId={}", order.getEmail(), order.getId());
            } else {
                log.warn("No email provided for orderId={}, skipping email notification", order.getId());
            }

        } catch (Exception e) {
            log.error("Email notification failed for orderId={}: {}", order.getId(), e.getMessage(), e);
        }
    }

    private void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(toEmail);
        mail.setSubject(subject);
        mail.setText(body);
        mailSender.send(mail);
    }

    public List<Notification> getNotificationsByUserId(String userId) {
        return notificationRepo.findByUserId(userId);
    }

    public Notification markAsRead(Integer notificationId) {
        Notification notification = notificationRepo.findById(notificationId).orElse(null);
        if (notification == null) {
            throw new RuntimeException("Notification_NotFound");
        }
        notification.setStatus("READ");
        return notificationRepo.save(notification);
    }
    public void deleteNotification(Integer notificationId) {
        if (!notificationRepo.existsById(notificationId)) {
            throw new RuntimeException("Notification_NotFound");
        }
        notificationRepo.deleteById(notificationId);
    }
}