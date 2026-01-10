/*package com.budgetwise.service;

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

    // === CORE METHODS ===
    public Expense addExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    public List<Expense> getUserExpenses(Long userId) {
        // FIXED: Using correct method name
        return expenseRepository.findByUserId(userId);
    }

    public List<Expense> getUserExpensesBetweenDates(Long userId, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }

    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

    // === DASHBOARD DATA METHODS ===
    public BigDecimal getCurrentMonthTotalIncome(Long userId) {
        try {
            LocalDate now = LocalDate.now();
            LocalDate start = LocalDate.of(now.getYear(), now.getMonth(), 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, start, end);

            return expenses.stream()
                    .filter(Expense::isIncome)
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getCurrentMonthTotalExpenses(Long userId) {
        try {
            LocalDate now = LocalDate.now();
            LocalDate start = LocalDate.of(now.getYear(), now.getMonth(), 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, start, end);

            return expenses.stream()
                    .filter(expense -> !expense.isIncome())
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public Map<String, BigDecimal> getCategoryTotals(Long userId) {
        Map<String, BigDecimal> categoryMap = new HashMap<>();

        try {
            LocalDate now = LocalDate.now();
            LocalDate start = LocalDate.of(now.getYear(), now.getMonth(), 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, start, end);

            for (Expense expense : expenses) {
                if (!expense.isIncome()) {
                    String category = expense.getCategory();
                    BigDecimal amount = expense.getAmount();
                    categoryMap.merge(category, amount, BigDecimal::add);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log error
        }

        return categoryMap;
    }

    // === TREND ANALYSIS METHODS ===
    public Map<String, Object> getMonthlyTrends(Long userId, int year) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Object[]> data = expenseRepository.getMonthlyExpenseTrend(userId, year);
            result.put("monthlyTrends", data);
            result.put("status", "success");
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getCategoryBreakdown(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Object[]> data = expenseRepository.getCategoryBreakdown(userId, startDate, endDate);
            result.put("categoryBreakdown", data);
            result.put("status", "success");
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }
        return result;
    }

    // === HELPER METHODS ===
    public List<Expense> getCurrentMonthExpenses(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate start = LocalDate.of(now.getYear(), now.getMonth(), 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return expenseRepository.findByUserIdAndDateBetween(userId, start, end);
    }

    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id).orElse(null);
    }

    //public BigDecimal getTotalExpensesForPeriod(Long userId, LocalDate startDate, LocalDate endDate) {
      //  try {
        //    BigDecimal total = expenseRepository.getTotalExpensesForPeriod(userId, startDate, endDate);
          //  return BigDecimal.valueOf(total);
        //} catch (Exception e) {
          //  return BigDecimal.ZERO;
        //}
    //}
    public BigDecimal getTotalExpensesForPeriod(Long userId, LocalDate startDate, LocalDate endDate) {
        try {
            // Change Double to BigDecimal
            BigDecimal total = expenseRepository.getTotalExpensesForPeriod(userId, startDate, endDate);
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    // === NEW: Financial Trend Methods ===
    public Map<String, BigDecimal> getCurrentMonthFinancialSummary(Long userId) {
        Map<String, BigDecimal> summary = new HashMap<>();

        BigDecimal totalIncome = getCurrentMonthTotalIncome(userId);
        BigDecimal totalExpenses = getCurrentMonthTotalExpenses(userId);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        summary.put("totalIncome", totalIncome);
        summary.put("totalExpenses", totalExpenses);
        summary.put("netSavings", netSavings);

        return summary;
    }
}*/
/*package com.budgetwise.service;

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

    // === CORE METHODS ===
    public Expense addExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    public List<Expense> getUserExpenses(Long userId) {
        return expenseRepository.findByUserId(userId);
    }

    public List<Expense> getUserExpenses(User user) {
        if (user == null || user.getId() == null) {
            return List.of();
        }
        return getUserExpenses(user.getId());
    }

    public List<Expense> getUserExpensesBetweenDates(Long userId, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }

    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

    // === DASHBOARD DATA METHODS ===
    public BigDecimal getCurrentMonthTotalIncome(Long userId) {
        try {
            LocalDate now = LocalDate.now();
            LocalDate start = LocalDate.of(now.getYear(), now.getMonth(), 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, start, end);

            return expenses.stream()
                    .filter(Expense::isIncome)
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getCurrentMonthTotalIncome(User user) {
        if (user == null || user.getId() == null) {
            return BigDecimal.ZERO;
        }
        return getCurrentMonthTotalIncome(user.getId());
    }

    public BigDecimal getCurrentMonthTotalExpenses(Long userId) {
        try {
            LocalDate now = LocalDate.now();
            LocalDate start = LocalDate.of(now.getYear(), now.getMonth(), 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, start, end);

            return expenses.stream()
                    .filter(expense -> !expense.isIncome())
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getCurrentMonthTotalExpenses(User user) {
        if (user == null || user.getId() == null) {
            return BigDecimal.ZERO;
        }
        return getCurrentMonthTotalExpenses(user.getId());
    }

    public Map<String, BigDecimal> getCategoryTotals(Long userId) {
        Map<String, BigDecimal> categoryMap = new HashMap<>();

        try {
            LocalDate now = LocalDate.now();
            LocalDate start = LocalDate.of(now.getYear(), now.getMonth(), 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, start, end);

            for (Expense expense : expenses) {
                if (!expense.isIncome()) {
                    String category = expense.getCategory();
                    BigDecimal amount = expense.getAmount();
                    categoryMap.merge(category, amount, BigDecimal::add);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return categoryMap;
    }

    public Map<String, BigDecimal> getCategoryTotals(User user) {
        if (user == null || user.getId() == null) {
            return new HashMap<>();
        }
        return getCategoryTotals(user.getId());
    }

    // === HELPER METHODS ===
    public List<Expense> getCurrentMonthExpenses(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate start = LocalDate.of(now.getYear(), now.getMonth(), 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return expenseRepository.findByUserIdAndDateBetween(userId, start, end);
    }

    public List<Expense> getCurrentMonthExpenses(User user) {
        if (user == null || user.getId() == null) {
            return List.of();
        }
        return getCurrentMonthExpenses(user.getId());
    }

    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id).orElse(null);
    }

    // ... rest of your existing methods ...
}*/
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

    // === CORE METHODS ===
    public Expense addExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    // Methods that accept User objects
    public List<Expense> getUserExpenses(User user) {
        if (user == null || user.getId() == null) return List.of();
        return expenseRepository.findByUserId(user.getId());
    }

    public List<Expense> getCurrentMonthExpenses(User user) {
        if (user == null || user.getId() == null) return List.of();
        return getCurrentMonthExpenses(user.getId());
    }

    public BigDecimal getCurrentMonthTotalExpenses(User user) {
        if (user == null || user.getId() == null) return BigDecimal.ZERO;
        return getCurrentMonthTotalExpenses(user.getId());
    }

    public BigDecimal getCurrentMonthTotalIncome(User user) {
        if (user == null || user.getId() == null) return BigDecimal.ZERO;
        return getCurrentMonthTotalIncome(user.getId());
    }

    public Map<String, BigDecimal> getCategoryTotals(User user) {
        if (user == null || user.getId() == null) return new HashMap<>();
        return getCategoryTotals(user.getId());
    }

    // Methods that accept Long userId
    public List<Expense> getUserExpenses(Long userId) {
        return expenseRepository.findByUserId(userId);
    }

    public List<Expense> getUserExpensesBetweenDates(Long userId, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }

    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

    // === DASHBOARD DATA METHODS ===
    public BigDecimal getCurrentMonthTotalIncome(Long userId) {
        try {
            LocalDate now = LocalDate.now();
            LocalDate start = LocalDate.of(now.getYear(), now.getMonth(), 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, start, end);

            return expenses.stream()
                    .filter(Expense::isIncome)
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getCurrentMonthTotalExpenses(Long userId) {
        try {
            LocalDate now = LocalDate.now();
            LocalDate start = LocalDate.of(now.getYear(), now.getMonth(), 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, start, end);

            return expenses.stream()
                    .filter(expense -> !expense.isIncome())
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public Map<String, BigDecimal> getCategoryTotals(Long userId) {
        Map<String, BigDecimal> categoryMap = new HashMap<>();

        try {
            LocalDate now = LocalDate.now();
            LocalDate start = LocalDate.of(now.getYear(), now.getMonth(), 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, start, end);

            for (Expense expense : expenses) {
                if (!expense.isIncome()) {
                    String category = expense.getCategory();
                    BigDecimal amount = expense.getAmount();
                    categoryMap.merge(category, amount, BigDecimal::add);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return categoryMap;
    }

    // === TREND ANALYSIS METHODS ===
    public Map<String, Object> getMonthlyTrends(Long userId, int year) {
        Map<String, Object> result = new HashMap<>();
        try {
            // FIXED: Repository method now has @Param annotations but call is the same
            List<Object[]> data = expenseRepository.getMonthlyExpenseTrend(userId, year);
            result.put("monthlyTrends", data);
            result.put("status", "success");
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getCategoryBreakdown(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        try {
            // FIXED: Repository method now has @Param annotations but call is the same
            List<Object[]> data = expenseRepository.getCategoryBreakdown(userId, startDate, endDate);
            result.put("categoryBreakdown", data);
            result.put("status", "success");
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }
        return result;
    }

    // === HELPER METHODS ===
    public List<Expense> getCurrentMonthExpenses(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate start = LocalDate.of(now.getYear(), now.getMonth(), 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return expenseRepository.findByUserIdAndDateBetween(userId, start, end);
    }

    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id).orElse(null);
    }

    // FIXED: Repository now returns BigDecimal
    public BigDecimal getTotalExpensesForPeriod(Long userId, LocalDate startDate, LocalDate endDate) {
        try {
            BigDecimal total = expenseRepository.getTotalExpensesForPeriod(userId, startDate, endDate);
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    // FIXED: Added method for total income
    public BigDecimal getTotalIncomeForPeriod(Long userId, LocalDate startDate, LocalDate endDate) {
        try {
            BigDecimal total = expenseRepository.getTotalIncomeForPeriod(userId, startDate, endDate);
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    // === NEW: Financial Trend Methods ===
    public Map<String, BigDecimal> getCurrentMonthFinancialSummary(Long userId) {
        Map<String, BigDecimal> summary = new HashMap<>();

        BigDecimal totalIncome = getCurrentMonthTotalIncome(userId);
        BigDecimal totalExpenses = getCurrentMonthTotalExpenses(userId);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        summary.put("totalIncome", totalIncome);
        summary.put("totalExpenses", totalExpenses);
        summary.put("netSavings", netSavings);

        return summary;
    }
}