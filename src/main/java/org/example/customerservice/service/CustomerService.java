package org.example.customerservice.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.customerservice.dto.CustomerRegisterRequest;
import org.example.customerservice.dto.CustomerUpdateEvent;
import org.example.customerservice.entity.Customer;
import org.example.customerservice.exceptionHandler.*;
import org.example.customerservice.mapper.CustomerMapper;
import org.example.customerservice.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Optional;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.customerservice.dto.CustomerRegisterRequest;
import org.example.customerservice.dto.CustomerUpdateEvent;
import org.example.customerservice.entity.Customer;
import org.example.customerservice.exceptionHandler.CustomerAlreadyExistsException;
import org.example.customerservice.exceptionHandler.EventPublishException;
import org.example.customerservice.exceptionHandler.InvalidCustomerException;
import org.example.customerservice.mapper.CustomerMapper;
import org.example.customerservice.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final EventPublisher eventPublisherService;
    private final ObjectMapper objectMapper;

    @Autowired
    public CustomerService(CustomerRepository customerRepository,
                           EventPublisherService eventPublisherService,
                           ObjectMapper objectMapper) {
        this.customerRepository = customerRepository;
        this.eventPublisherService = eventPublisherService;
        this.objectMapper = objectMapper;
    }


    @Retryable(
            value = {DBException.class, EventPublishException.class}, // Specify the exceptions to retry, for db failures, find by and save exceptions and publish event failure
            maxAttempts = 2, // Maximum number of attempts
            backoff = @Backoff(delay = 200) // Delay between retries (200 ms)
    )
    public Customer registerCustomer(CustomerRegisterRequest customerRegisterRequest) throws DBException {

        Optional<Customer> existingCustomer = Optional.empty();
        try {
            existingCustomer = customerRepository.findByEmailId(customerRegisterRequest.getEmailId());
        } catch (Exception ex) {
            throw new DBException(ex.getMessage());
        }
        if (existingCustomer.isPresent()) {
            log.info("Customer Already Present: {}", existingCustomer);
            throw new CustomerAlreadyExistsException("Customer with email " + customerRegisterRequest.getEmailId() + " already exists.");
        }

        Customer customer = CustomerMapper.INSTANCE.customerDtoToEntity(customerRegisterRequest);
        Customer savedCustomer;
        try {
            savedCustomer = customerRepository.save(customer);
            publishCustomerUpdateEvent(savedCustomer);
        } catch (EventPublishException ex) {
            throw new EventPublishException(ex.getMessage());
        } catch (Exception ex) {
            throw new DBException(ex.getMessage());
        }
        return savedCustomer;
    }



    private void publishCustomerUpdateEvent(Customer customer) throws EventPublishException {
        CustomerUpdateEvent customerUpdateEvent = CustomerUpdateEvent.builder()
                .customerId(customer.getId())
                .build();
        try {
            String message = objectMapper.writeValueAsString(customerUpdateEvent);
            eventPublisherService.sendMessage(message);
        } catch (JsonProcessingException ex) {
            log.error("Error Parsing Json for Message:{}", ex.getMessage(), ex);
            throw new EventPublishException("Error Parsing Json for message");
        } catch (Exception ex) {
            log.error("Exception while publishing message", ex);
            throw new EventPublishException("Error publishing event for customer");
        }
    }

    public Customer getCustomerDetails(Long id) {
        return customerRepository.findById(id).orElseThrow(() -> new InvalidCustomerException("Customer not found"));
    }


    @Recover
    public Customer recover(DataAccessException e, CustomerRegisterRequest customerRegisterRequest) {
        log.error("Failed to register customer after retries: {}", e.getMessage(), e);
        throw new RuntimeException("Failed to register customer after multiple attempts", e);
    }
}
