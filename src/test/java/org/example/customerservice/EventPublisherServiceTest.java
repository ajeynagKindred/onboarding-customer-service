package org.example.customerservice;

import org.example.customerservice.service.EventPublisherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jms.core.JmsTemplate;

import static org.mockito.Mockito.*;

class EventPublisherServiceTest {

    @InjectMocks
    private EventPublisherService eventPublisherService;

    @Mock
    private JmsTemplate jmsTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendMessageSuccess() {
        String message = "Test message";

        // No exceptions are thrown
        doNothing().when(jmsTemplate).convertAndSend("customer-update-queue", message);

        eventPublisherService.sendMessage(message);

        verify(jmsTemplate, times(1)).convertAndSend("customer-update-queue", message);
    }

    @Test
    void testSendMessageRetryAndFallback() {
        String message = "Test message";
        // Simulate failure by throwing an exception
        doThrow(new RuntimeException("Send failed"))
                .when(jmsTemplate).convertAndSend("customer-update-queue", message);

        // Since we're simulating retries and the method is final, we need to verify after retries
        // Use reflection to call private methods or spy on the class to verify internal state changes
        // Here we use a spy and verify the behavior

        EventPublisherService spyService = spy(eventPublisherService);
        doNothing().when(spyService).sendToDeadLetterQueue(message);

        try {
            spyService.sendMessage(message);
        } catch (Exception ignored) {
            // Expected exception after retries
        }

        // Verify that the DLQ method was called
        verify(spyService, times(1)).sendToDeadLetterQueue(message);
    }

    @Test
    void testSendMessageToDeadLetterQueueOnFailure() {
        String message = "Test message";
        RuntimeException exception = new RuntimeException("Send failed");

        // Simulate failure by throwing an exception
        doThrow(exception).when(jmsTemplate).convertAndSend("customer-update-queue", message);

        EventPublisherService spyService = spy(eventPublisherService);
        doNothing().when(spyService).sendToDeadLetterQueue(message);

        try {
            spyService.sendMessage(message);
        } catch (Exception ignored) {
            // Expected exception after retries
        }

        // Verify that the DLQ method was called
        verify(spyService, times(1)).sendToDeadLetterQueue(message);
    }
}

