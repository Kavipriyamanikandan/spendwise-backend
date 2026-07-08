package com.example.demo.repository;

import com.example.demo.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUser_Id(Long userId);

    List<Budget> findByUser_IdAndMonthYear(Long userId, String monthYear);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.category.name = :category AND b.monthYear = :monthYear")
    Optional<Budget> findByUser_IdAndCategory_NameAndMonthYear(@Param("userId") Long userId,
                                                                @Param("category") String category,
                                                                @Param("monthYear") String monthYear);
}
