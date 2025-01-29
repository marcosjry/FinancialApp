package com.marcos.usermanagement.service.domain.repository;

import com.marcos.usermanagement.service.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findById(String id);

    User findByEmail(String email);

    UserDetails findByCelular(String celular);

    Boolean existsByEmail(String email);
}
