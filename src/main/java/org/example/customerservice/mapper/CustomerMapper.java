package org.example.customerservice.mapper;

import org.example.customerservice.dto.CustomerRegisterRequest;
import org.example.customerservice.entity.Customer;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CustomerMapper {

    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);




    Customer customerDtoToEntity(CustomerRegisterRequest customerCreateRequest);


    @AfterMapping
    default void setDefaultActive(@MappingTarget Customer customer) {
        // Set 'active' to true if not explicitly set
        customer.setActive(true);
    }



}
