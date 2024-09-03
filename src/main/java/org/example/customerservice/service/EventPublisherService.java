package org.example.customerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.example.customerservice.exceptionHandler.EventPublishException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventPublisherService implements EventPublisher {

    @Autowired
    private JmsTemplate jmsTemplate;

    private static final String CUSTOMER_UPDATE_QUEUE = "customer-update-queue";
    private static final String DEAD_LETTER_QUEUE = "dead-letter-queue";

    @Retryable(
            value = {EventPublishException.class}, // Replace with the actual exception type for Solace
            maxAttempts = 5,
            backoff = @Backoff(delay = 1000) // Retry with a 1-second delay
    )
    public void sendMessage(String message) {
        try {
            jmsTemplate.convertAndSend(CUSTOMER_UPDATE_QUEUE, message);
            log.info("Successfully published: {}", message);
        } catch (Exception ex) {
            log.error("Failed to publish: {}", ex.getMessage());
            throw new EventPublishException("Publishing Failed");
        }
    }

    @Recover
    public void recover(Exception e, String message) {
        log.error("Failed to publish message after retries: {}", message, e);
        sendToDeadLetterQueue(message);
    }

    public void sendToDeadLetterQueue(String message) {
        jmsTemplate.convertAndSend(DEAD_LETTER_QUEUE, message);
        log.info("Message sent to dead-letter queue: {}", message);
    }
}
