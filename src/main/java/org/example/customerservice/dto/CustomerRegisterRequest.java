package org.example.customerservice.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRegisterRequest {

    @NotNull
    @NotBlank(message = "Name is mandatory")
    @Size(min = 4, max = 15, message = "Name must be between 4 and 15 characters")
    private String name;

    @NotNull
    @NotBlank(message = "Email Id is mandatory")
    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "Invalid email format"
    )
    private String emailId;

}
