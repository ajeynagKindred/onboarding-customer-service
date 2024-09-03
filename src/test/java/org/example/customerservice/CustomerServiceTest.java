package org.example.customerservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.customerservice.dto.CustomerRegisterRequest;
import org.example.customerservice.dto.CustomerUpdateEvent;
import org.example.customerservice.entity.Customer;
import org.example.customerservice.exceptionHandler.CustomerAlreadyExistsException;
import org.example.customerservice.exceptionHandler.DBException;
import org.example.customerservice.exceptionHandler.EventPublishException;
import org.example.customerservice.exceptionHandler.InvalidCustomerException;

import org.example.customerservice.repository.CustomerRepository;
import org.example.customerservice.service.CustomerService;
import org.example.customerservice.service.EventPublisherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataAccessException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Slf4j
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private EventPublisherService eventPublisherService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CustomerService customerService;

    private CustomerRegisterRequest customerRegisterRequest;
    private Customer customer;

    @BeforeEach
    void setUp() {
        log.info("Mocks initialized successfully.");
        MockitoAnnotations.openMocks(this);

        customerRegisterRequest = new CustomerRegisterRequest();
        customerRegisterRequest.setEmailId("test@example.com");

        customer = new Customer();
        customer.setEmailId("test@example.com");
    }

    @Test
    void registerCustomer_Success() throws JsonProcessingException {
        // Given
        CustomerRegisterRequest request = new CustomerRegisterRequest();
        request.setEmailId("test@example.com");

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setEmailId("test@example.com");

        when(customerRepository.findByEmailId(request.getEmailId())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(objectMapper.writeValueAsString(any(CustomerUpdateEvent.class))).thenReturn("dummyJson");

        // When
        Customer registeredCustomer = customerService.registerCustomer(request);

        // Then
        assertNotNull(registeredCustomer);
        assertEquals(customer.getEmailId(), registeredCustomer.getEmailId());
        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(eventPublisherService, times(1)).sendMessage(eq("dummyJson"));
    }

    @Test
    void registerCustomer_CustomerAlreadyExists() {
        // Given
        CustomerRegisterRequest request = new CustomerRegisterRequest();
        request.setEmailId("test@example.com");

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setEmailId("test@example.com");

        when(customerRepository.findByEmailId(request.getEmailId())).thenReturn(Optional.of(customer));

        // When & Then
        assertThrows(CustomerAlreadyExistsException.class, () -> customerService.registerCustomer(request));
        verify(customerRepository, never()).save(any(Customer.class));
        verify(eventPublisherService, never()).sendMessage(anyString());
    }

    @Test
    void registerCustomer_EventPublishException() throws JsonProcessingException {
        // Given
        CustomerRegisterRequest request = new CustomerRegisterRequest();
        request.setEmailId("test@example.com");

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setEmailId("test@example.com");

        when(customerRepository.findByEmailId(request.getEmailId())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(objectMapper.writeValueAsString(any(CustomerUpdateEvent.class))).thenThrow(JsonProcessingException.class);

        // When & Then
        assertThrows(EventPublishException.class, () -> customerService.registerCustomer(request));
        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(eventPublisherService, never()).sendMessage(anyString());
    }


    @Test
    void getCustomerDetails_Success() {
        // Given
        Long customerId = 1L;
        Customer customer = new Customer();
        customer.setId(customerId);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // When
        Customer foundCustomer = customerService.getCustomerDetails(customerId);

        // Then
        assertNotNull(foundCustomer);
        assertEquals(customerId, foundCustomer.getId());
    }

    @Test
    void testRegisterCustomerRetries() throws DBException {
        // Arrange
        CustomerRegisterRequest customerRegisterRequest = new CustomerRegisterRequest("test@example.com", "Test User");

        // Mock behavior: Throw DBException to trigger retry
        when(customerRepository.findByEmailId(customerRegisterRequest.getEmailId()))
                .thenThrow(new DBException("Database connection issue")) // First call throws exception
                .thenThrow(new DBException("Database connection issue")) // Second call throws exception
                .thenReturn(Optional.empty());  // Third call returns a valid response

        Customer customer = new Customer(1L, "test@example.com", "Test User", true);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        doNothing().when(eventPublisherService).sendMessage(anyString());

    }


    @Test
    void getCustomerDetails_CustomerNotFound() {
        // Given
        Long customerId = 1L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvalidCustomerException.class, () -> customerService.getCustomerDetails(customerId));
    }



    @Test
    void recoverMethodTest() {
        // Given
        DataAccessException exception = mock(DataAccessException.class);
        CustomerRegisterRequest request = new CustomerRegisterRequest();

        // When & Then
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> customerService.recover(exception, request));
        assertEquals("Failed to register customer after multiple attempts", thrown.getMessage());
    }
}
