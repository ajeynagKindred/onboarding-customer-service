package org.example.customerservice.controller;


import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.customerservice.dto.CustomerRegisterRequest;
import org.example.customerservice.entity.Customer;
import org.example.customerservice.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
@Slf4j
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping()
    public ResponseEntity<Customer> registerCustomer(@Valid @RequestBody CustomerRegisterRequest customerRequest) {
        log.info("Customer Register Request received:{}",customerRequest);

            return ResponseEntity.ok().body(customerService.registerCustomer(customerRequest));

    }

    @GetMapping("")
    public ResponseEntity<Customer> getCustomerDetails(@RequestHeader String userId) {
        return  ResponseEntity.ok().body(customerService.getCustomerDetails(Long.valueOf(userId)));
    }
}
