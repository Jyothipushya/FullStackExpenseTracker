package com.budgetwise.service;

import com.budgetwise.entity.Expense;
import com.budgetwise.entity.User;
import com.budgetwise.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    // === CORE METHODS (Always work) ===
    public Expense addExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    public List<Expense> getUserExpenses(User user) {
        return expenseRepository.findByUser(user);
    }

    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

    // === DASHBOARD DATA METHODS (Safe with fallbacks) ===
    public BigDecimal getCurrentMonthTotalIncome(User user) {
        try {
            LocalDate now = LocalDate.now();
            LocalDate start = LocalDate.of(now.getYear(), now.getMonth(), 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, start, end);

            return expenses.stream()
                    .filter(Expense::isIncome)
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

        } catch (Exception e) {
            return BigDecimal.ZERO; // Safe fallback
        }
    }

    public BigDecimal getCurrentMonthTotalExpenses(User user) {
        try {
            LocalDate now = LocalDate.now();
            LocalDate start = LocalDate.of(now.getYear(), now.getMonth(), 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, start, end);

            return expenses.stream()
                    .filter(expense -> !expense.isIncome())
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

        } catch (Exception e) {
            return BigDecimal.ZERO; // Safe fallback
        }
    }

    public Map<String, BigDecimal> getCategoryTotals(User user) {
        Map<String, BigDecimal> categoryMap = new HashMap<>();

        try {
            LocalDate now = LocalDate.now();
            LocalDate start = LocalDate.of(now.getYear(), now.getMonth(), 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, start, end);

            for (Expense expense : expenses) {
                if (!expense.isIncome()) { // Only expenses, not income
                    String category = expense.getCategory();
                    BigDecimal amount = expense.getAmount();

                    categoryMap.merge(category, amount, BigDecimal::add);
                }
            }
        } catch (Exception e) {
            // Return empty map if error
        }

        return categoryMap;
    }

    // === HELPER METHODS ===
    public List<Expense> getCurrentMonthExpenses(User user) {
        LocalDate now = LocalDate.now();
        LocalDate start = LocalDate.of(now.getYear(), now.getMonth(), 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return expenseRepository.findByUserAndDateBetween(user, start, end);
    }

    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id).orElse(null);
    }
}