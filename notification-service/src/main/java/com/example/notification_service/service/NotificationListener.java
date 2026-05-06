package com.example.notification_service.service;

import com.example.notification_service.dto.TransactionEvent;
import com.example.notification_service.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final EmailService emailService;
    private final RestTemplate restTemplate = new RestTemplate();

    @KafkaListener(topics = "outbox_events", groupId = "notification-group")
    public void consumeTransactionEvent(TransactionEvent event) {
        log.info(" Received Transaction Event from Kafka: {}", event.getTransactionId());

        if (!"SUCCESS".equals(event.getStatus())) {
            return;
        }

        try {
            // 1. Fetch Sender (Port 7005)
            UserResponse sender = restTemplate.getForObject(
                    "http://localhost:7005/api/v1/auth/users/" + event.getFromUserId(),
                    UserResponse.class
            );

            // 2. Fetch Receiver (Port 7005)
            UserResponse receiver = restTemplate.getForObject(
                    "http://localhost:7005/api/v1/auth/users/" + event.getToUserId(),
                    UserResponse.class
            );

            // 3. Send Emails using the correct getUserName() method!
            if (sender != null && sender.getEmail() != null) {
                emailService.sendTransferSuccessfulMail(
                        sender.getEmail(),
                        event.getAmount().toString(),
                        receiver.getUserName() // <-- Fixed
                );
            }

            if (receiver != null && receiver.getEmail() != null) {
                emailService.sendMoneyReceivedEmail(
                        receiver.getEmail(),
                        event.getAmount().toString(),
                        sender.getUserName() // <-- Fixed
                );
            }

        } catch (Exception e) {
            log.error("Failed to process notification for Transaction: {}", event.getTransactionId(), e);
        }
    }
}