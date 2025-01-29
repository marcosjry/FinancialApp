package com.marcos.usermanagement.service.domain.repository;

import com.marcos.usermanagement.service.domain.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {

}
