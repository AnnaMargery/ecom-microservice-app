package com.ecom.app.user.mapper;

import com.ecom.app.user.dto.AddressDto;
import com.ecom.app.user.model.Address;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class AddressMapperTest {

    @Autowired
    private AddressMapper addressMapper;

    @Test
    void shouldMapToAddressDto() {
        //Given
        Address address = mockAddress();
        //When
        AddressDto addressDto = addressMapper.mapToAddressDto(address);
        //Then
        assertThat(addressDto)
                .usingRecursiveComparison()
                .isEqualTo(address);

    }

    @Test
    void mapToAddressFromDto() {
        //Given
        AddressDto addressDto = mockAddressDto();
        //When
        Address address = addressMapper.mapToAddressFromDto(addressDto);
        //Then
        assertThat(address)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(addressDto);
    }

    private AddressDto mockAddressDto() {
        return AddressDto.builder()
                .street("Dummy street")
                .city("Dummy town")
                .state("Dummy state")
                .zipcode("22-222")
                .country("Poland")
                .build();
    }

    private Address mockAddress() {
        return Address.builder()
                .street("Dummy street")
                .city("Dummy town")
                .state("Dummy state")
                .zipcode("22-222")
                .country("Poland")
                .build();
    }
}