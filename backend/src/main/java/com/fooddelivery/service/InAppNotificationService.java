package com.fooddelivery.service;

import com.fooddelivery.entity.Notification;
import com.fooddelivery.entity.User;
import com.fooddelivery.repository.NotificationRepository;
import com.fooddelivery.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InAppNotificationService {

    private static final Logger log = LoggerFactory.getLogger(InAppNotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Persist a notification for a real database user.
     *
     * The caller may hand us a lazily loaded proxy, so the user is always
     * reloaded from the users table inside this transaction. This makes this
     * method the single place that guarantees notifications.user_id is valid.
     */
    @Transactional
    public void createNotification(User user, String title, String message, String type) {
        if (user == null) {
            log.error(
                    "Refusing to create notification '{}' (type={}): user is null",
                    title, type);
            throw new IllegalArgumentException("Cannot create notification for null user");
        }

        if (user.getId() == null) {
            // Log the throwable so the stack trace shows which caller supplied
            // the transient User instance.
            log.error(
                    "Refusing to create notification '{}' (type={}): user has null id "
                            + "(email={}, class={})",
                    title, type, user.getEmail(), user.getClass().getName(),
                    new IllegalStateException("Transient User passed to createNotification"));
            throw new IllegalStateException("Cannot create notification for user with null id");
        }

        Long userId = user.getId();
        User managedUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error(
                            "Refusing to create notification '{}' (type={}): "
                                    + "user id={} (email={}) does not exist in users",
                            title, type, userId, user.getEmail());
                    return new RuntimeException("User not found: " + userId);
                });

        Notification notification = new Notification();
        notification.setUser(managedUser);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        log.info("Notification userId={}", notification.getUser().getId());
        notificationRepository.save(notification);
    }

    public List<Notification> getMyNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public long getUnreadCount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Transactional
    public void markAsRead(String email, Long notificationId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You cannot modify another user's notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(String email) {
        List<Notification> notifications = getMyNotifications(email);
        notifications.stream()
                .filter(notification -> !notification.isRead())
                .forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notifications);
    }
}
