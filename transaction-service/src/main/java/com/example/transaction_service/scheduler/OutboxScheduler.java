package com.example.transaction_service.scheduler;

import com.example.transaction_service.dto.TransactionEvent;
import com.example.transaction_service.entity.OutboxEvent;
import com.example.transaction_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxScheduler {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC="outbox_events";

    @Scheduled(fixedRate = 5000)
    public void publishPendingEvents(){

        List<OutboxEvent>  pendingEvents = outboxEventRepository.findByStatus("PENDING");
        if(!pendingEvents.isEmpty()){
            log.info("Relay woke up! Found {} PENDING events. Publishing to Kafka...", pendingEvents.size());
        }

        for(OutboxEvent event: pendingEvents){
            try{
                kafkaTemplate.send(TOPIC,event.getPayload());
                event.setStatus("COMPLETED");
                outboxEventRepository.save(event);
                log.info("Successfully published event for Transaction ID: {}", event.getAggregateId());
            } catch (Exception e) {
                log.error("Failed to publish event ID: {}. Will retry.", event.getId(), e);
            }
        }


    }
}
