package com.example.demo.repository;

import com.example.demo.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUser_Id(Long userId);

    List<Expense> findByUser_IdAndCategory(Long userId, String category);

    // Alias matching SRS naming convention; category is a plain String field on Expense,
    // not a relation, so this delegates to the same query as findByUser_IdAndCategory.
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.category = :category")
    List<Expense> findByUser_IdAndCategory_Name(@Param("userId") Long userId, @Param("category") String category);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.user.id = :userId AND e.category = :category AND e.date LIKE CONCAT(:month, '%')")
    Double sumByUser_IdAndCategory_NameAndMonth(@Param("userId") Long userId,
                                                 @Param("category") String category,
                                                 @Param("month") String month);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.date LIKE CONCAT(:month, '%')")
    List<Expense> findByUser_IdAndMonth(@Param("userId") Long userId, @Param("month") String month);
}
