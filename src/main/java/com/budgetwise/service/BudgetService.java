package com.budgetwise.service;

import com.budgetwise.entity.Budget;
import com.budgetwise.entity.Expense;
import com.budgetwise.entity.User;
import com.budgetwise.repository.BudgetRepository;
import com.budgetwise.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    // ==================== CORE BUDGET METHODS ====================

    /**
     * Set or update budget for a category
     */
    public Budget setBudget(User user, String category, BigDecimal monthlyLimit) {
        if (monthlyLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Budget limit cannot be negative");
        }

        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);

        // CORRECT: Using findByUserAndCategoryAndCurrentMonth (matches repository)
        Optional<Budget> existingBudget = budgetRepository.findByUserAndCategoryAndCurrentMonth(
                user, category, currentMonth
        );

        Budget budget;
        if (existingBudget.isPresent()) {
            budget = existingBudget.get();
            budget.setMonthlyLimit(monthlyLimit);
            budget.setCurrentMonth(currentMonth); // Update month if needed
        } else {
            budget = new Budget(user, category, monthlyLimit);
            budget.setCurrentMonth(currentMonth);
        }

        return budgetRepository.save(budget);
    }

    /**
     * Get all budgets for a user (current month)
     */
    public List<Budget> getUserBudgets(User user) {
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        // CORRECT: Using findByUserAndMonth (matches repository)
        return budgetRepository.findByUserAndMonth(user, currentMonth);
    }

    /**
     * Get all budgets for a user (all time)
     */
    public List<Budget> getAllUserBudgets(User user) {
        return budgetRepository.findByUser(user);
    }

    /**
     * Delete a budget
     */
    public void deleteBudget(Long budgetId) {
        budgetRepository.deleteById(budgetId);
    }

    /**
     * Delete budget by user, category and month
     */
    public void deleteBudget(User user, String category, LocalDate month) {
        budgetRepository.deleteByUserAndCategoryAndCurrentMonth(user, category, month.withDayOfMonth(1));
    }

    // ==================== BUDGET PROGRESS CALCULATION ====================

    /**
     * Calculate budget progress for a specific budget
     */
    public Map<String, Object> calculateBudgetProgress(Budget budget) {
        Map<String, Object> progress = new HashMap<>();

        User user = budget.getUser();
        String category = budget.getCategory();
        LocalDate month = budget.getCurrentMonth();

        // FIXED: Using findByUserIdAndDateBetween instead of findByUserAndDateBetween
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(
                user.getId(),
                month.withDayOfMonth(1),
                month.withDayOfMonth(month.lengthOfMonth())
        );

        BigDecimal spentAmount = BigDecimal.ZERO;
        for (Expense expense : expenses) {
            if (expense.getCategory().equalsIgnoreCase(category) && !expense.isIncome()) {
                spentAmount = spentAmount.add(expense.getAmount());
            }
        }

        BigDecimal monthlyLimit = budget.getMonthlyLimit();
        BigDecimal remainingAmount = monthlyLimit.subtract(spentAmount);

        // Calculate percentage used
        int percentageUsed = 0;
        if (monthlyLimit.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percentage = spentAmount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(monthlyLimit, 2, RoundingMode.HALF_UP);
            percentageUsed = percentage.intValue();
        }

        // Determine status
        String status;
        if (monthlyLimit.compareTo(BigDecimal.ZERO) == 0) {
            status = "NOT_SET";
        } else if (spentAmount.compareTo(monthlyLimit) > 0) {
            status = "OVER_BUDGET";
        } else if (percentageUsed >= 80) {
            status = "WARNING";
        } else {
            status = "ON_TRACK";
        }

        // Populate progress map
        progress.put("budgetId", budget.getId());
        progress.put("category", category);
        progress.put("monthlyLimit", monthlyLimit);
        progress.put("spentAmount", spentAmount);
        progress.put("remainingAmount", remainingAmount);
        progress.put("percentageUsed", percentageUsed);
        progress.put("status", status);
        progress.put("statusClass", getStatusClass(status));
        progress.put("statusIcon", getStatusIcon(status));

        return progress;
    }

    /**
     * Get budget progress for all user budgets
     */
    public List<Map<String, Object>> getAllBudgetProgress(User user) {
        List<Budget> budgets = getUserBudgets(user);
        List<Map<String, Object>> allProgress = new ArrayList<>();

        for (Budget budget : budgets) {
            Map<String, Object> progress = calculateBudgetProgress(budget);
            allProgress.add(progress);
        }

        // Sort by percentage used (highest first)
        allProgress.sort((a, b) -> {
            Integer bPercent = (Integer) b.get("percentageUsed");
            Integer aPercent = (Integer) a.get("percentageUsed");
            return bPercent.compareTo(aPercent);
        });

        return allProgress;
    }

    /**
     * Get total budget summary for user
     */
    public Map<String, Object> getBudgetSummary(User user) {
        List<Map<String, Object>> allProgress = getAllBudgetProgress(user);

        BigDecimal totalBudget = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;
        int totalBudgets = allProgress.size();
        int overBudgetCount = 0;
        int warningCount = 0;
        int onTrackCount = 0;

        for (Map<String, Object> progress : allProgress) {
            totalBudget = totalBudget.add((BigDecimal) progress.get("monthlyLimit"));
            totalSpent = totalSpent.add((BigDecimal) progress.get("spentAmount"));

            String status = (String) progress.get("status");
            switch (status) {
                case "OVER_BUDGET":
                    overBudgetCount++;
                    break;
                case "WARNING":
                    warningCount++;
                    break;
                case "ON_TRACK":
                    onTrackCount++;
                    break;
            }
        }

        BigDecimal totalRemaining = totalBudget.subtract(totalSpent);
        int overallPercentage = 0;
        if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percentage = totalSpent
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalBudget, 2, RoundingMode.HALF_UP);
            overallPercentage = percentage.intValue();
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalBudget", totalBudget);
        summary.put("totalSpent", totalSpent);
        summary.put("totalRemaining", totalRemaining);
        summary.put("overallPercentage", overallPercentage);
        summary.put("totalBudgets", totalBudgets);
        summary.put("overBudgetCount", overBudgetCount);
        summary.put("warningCount", warningCount);
        summary.put("onTrackCount", onTrackCount);
        summary.put("status", getOverallStatus(overallPercentage));

        return summary;
    }

    // ==================== HELPER METHODS ====================

    private String getStatusClass(String status) {
        switch (status) {
            case "OVER_BUDGET":
                return "status-over";
            case "WARNING":
                return "status-warning";
            case "ON_TRACK":
                return "status-ontrack";
            default:
                return "status-notset";
        }
    }

    private String getStatusIcon(String status) {
        switch (status) {
            case "OVER_BUDGET":
                return "fa-exclamation-circle";
            case "WARNING":
                return "fa-exclamation-triangle";
            case "ON_TRACK":
                return "fa-check-circle";
            default:
                return "fa-info-circle";
        }
    }

    private String getOverallStatus(int percentage) {
        if (percentage > 100) return "OVER_BUDGET";
        if (percentage >= 80) return "WARNING";
        return "ON_TRACK";
    }

    // ==================== AI SUGGESTIONS ====================

    /**
     * Simple AI suggestions based on budget status
     */
    public List<String> getAISuggestions(User user) {
        List<String> suggestions = new ArrayList<>();
        List<Map<String, Object>> progress = getAllBudgetProgress(user);

        for (Map<String, Object> budget : progress) {
            String category = (String) budget.get("category");
            String status = (String) budget.get("status");
            BigDecimal spent = (BigDecimal) budget.get("spentAmount");
            BigDecimal limit = (BigDecimal) budget.get("monthlyLimit");
            BigDecimal remaining = (BigDecimal) budget.get("remainingAmount");
            int percentageUsed = (Integer) budget.get("percentageUsed");

            switch (status) {
                case "OVER_BUDGET":
                    BigDecimal overspent = spent.subtract(limit);
                    suggestions.add(String.format(
                            "❌ You've exceeded your %s budget by ₹%.2f. Consider reducing expenses in this category.",
                            category, overspent
                    ));
                    break;

                case "WARNING":
                    suggestions.add(String.format(
                            "⚠️ Your %s budget is almost used up (%d%%). Only ₹%.2f remaining.",
                            category, percentageUsed, remaining
                    ));
                    break;

                case "ON_TRACK":
                    if (percentageUsed < 50) {
                        suggestions.add(String.format(
                                "✅ Great job managing your %s budget! You still have ₹%.2f available.",
                                category, remaining
                        ));
                    }
                    break;
            }
        }

        // Add general suggestions
        if (suggestions.isEmpty()) {
            suggestions.add("📊 All your budgets are on track! Keep up the good financial management.");
        }

        suggestions.add("💡 Tip: Review your budgets weekly to stay on track.");

        return suggestions;
    }

    // ==================== ADDITIONAL HELPER METHODS ====================

    /**
     * Check if user has budget for specific category in current month
     */
    public boolean hasBudgetForCategory(User user, String category) {
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        return budgetRepository.existsByUserAndCategoryAndCurrentMonth(user, category, currentMonth);
    }

    /**
     * Get budget for specific category
     */
    public Optional<Budget> getBudgetForCategory(User user, String category) {
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        return budgetRepository.findByUserAndCategoryAndCurrentMonth(user, category, currentMonth);
    }

    /**
     * Get categories with budgets
     */
    public List<String> getBudgetCategories(User user) {
        return budgetRepository.findDistinctCategoriesByUser(user);
    }
    // In BudgetService.java, add this method:

// ==================== BUDGET RECOMMENDATIONS ====================

    /**
     * Get recommended budget amounts based on user's income
     */
    public Map<String, BigDecimal> getRecommendedBudgets(BigDecimal monthlyIncome) {
        Map<String, BigDecimal> recommendations = new HashMap<>();

        if (monthlyIncome == null || monthlyIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return recommendations; // Return empty map for invalid income
        }

        // Simple 50/30/20 rule: 50% needs, 30% wants, 20% savings
        BigDecimal needs = monthlyIncome.multiply(new BigDecimal("0.50"));
        BigDecimal wants = monthlyIncome.multiply(new BigDecimal("0.30"));
        BigDecimal savings = monthlyIncome.multiply(new BigDecimal("0.20"));

        // Distribute into categories
        recommendations.put("Food & Dining", needs.multiply(new BigDecimal("0.30"))); // 30% of needs
        recommendations.put("Rent/Mortgage", needs.multiply(new BigDecimal("0.40"))); // 40% of needs
        recommendations.put("Bills & Utilities", needs.multiply(new BigDecimal("0.20"))); // 20% of needs
        recommendations.put("Transportation", needs.multiply(new BigDecimal("0.10"))); // 10% of needs

        recommendations.put("Shopping", wants.multiply(new BigDecimal("0.40"))); // 40% of wants
        recommendations.put("Entertainment", wants.multiply(new BigDecimal("0.40"))); // 40% of wants
        recommendations.put("Personal Care", wants.multiply(new BigDecimal("0.20"))); // 20% of wants

        recommendations.put("Savings & Investments", savings);

        // Round to nearest 100
        for (Map.Entry<String, BigDecimal> entry : recommendations.entrySet()) {
            BigDecimal rounded = entry.getValue()
                    .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            recommendations.put(entry.getKey(), rounded);
        }

        return recommendations;
    }

    /**
     * Get recommended budgets based on user's recent income
     */
    public Map<String, BigDecimal> getRecommendedBudgetsForUser(User user) {
        // Get last month's income
        LocalDate now = LocalDate.now();
        LocalDate lastMonthStart = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = lastMonthStart.withDayOfMonth(lastMonthStart.lengthOfMonth());

        // Calculate average monthly income (last 3 months)
        BigDecimal totalIncome = BigDecimal.ZERO;
        int monthCount = 0;

        for (int i = 1; i <= 3; i++) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            List<Expense> monthlyExpenses = expenseRepository.findByUserIdAndDateBetween(
                    user.getId(), monthStart, monthEnd
            );

            BigDecimal monthlyIncome = monthlyExpenses.stream()
                    .filter(Expense::isIncome)
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (monthlyIncome.compareTo(BigDecimal.ZERO) > 0) {
                totalIncome = totalIncome.add(monthlyIncome);
                monthCount++;
            }
        }

        BigDecimal averageMonthlyIncome = monthCount > 0
                ? totalIncome.divide(BigDecimal.valueOf(monthCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return getRecommendedBudgets(averageMonthlyIncome);
    }
}