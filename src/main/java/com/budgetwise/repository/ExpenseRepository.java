package com.budgetwise.repository;

import com.budgetwise.entity.Expense;
import com.budgetwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // SIMPLE METHODS THAT WORK:
    List<Expense> findByUser(User user);

    List<Expense> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);

    List<Expense> findByUserAndIsIncome(User user, boolean isIncome);
}