package com.budgetwise.repository;

import com.budgetwise.entity.Budget;
import com.budgetwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {


    // Find all budgets for a user
    List<Budget> findByUser(User user);

    // Find specific budget for user, category, and month
    Optional<Budget> findByUserAndCategoryAndCurrentMonth(
            User user,
            String category,
            LocalDate currentMonth
    );

    // Find budgets for current month
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.currentMonth = :month")
    List<Budget> findByUserAndMonth(
            @Param("user") User user,
            @Param("month") LocalDate month
    );

    // Check if budget exists for category in current month
    boolean existsByUserAndCategoryAndCurrentMonth(
            User user,
            String category,
            LocalDate currentMonth
    );

    // Delete budget by user and category
    void deleteByUserAndCategoryAndCurrentMonth(
            User user,
            String category,
            LocalDate currentMonth
    );

    // Get distinct categories used by user
    @Query("SELECT DISTINCT b.category FROM Budget b WHERE b.user = :user")
    List<String> findDistinctCategoriesByUser(@Param("user") User user);
}