package com.marcos.budget.service.domain.repository;

import com.marcos.budget.service.domain.model.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository <Budget, String> {

    Budget findByName(String nome);

    List<Budget> findByUserId(String userId);

    Budget findByNameAndUserId(String name, String userId);

    boolean existsByUserIdAndName(String userId, String name);

    @Query("SELECT b FROM Budget b WHERE b.userId = :userId")
    Page<Budget> findByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT b FROM Budget b WHERE b.userId = :userId AND b.name = :name")
    Optional<Budget> findSingleBudget(@Param("userId") String userId, @Param("name") String name);

}
