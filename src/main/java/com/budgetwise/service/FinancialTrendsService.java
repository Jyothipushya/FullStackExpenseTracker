package com.budgetwise.service;

import com.budgetwise.entity.Expense;
import com.budgetwise.entity.FinancialTrends;
import com.budgetwise.repository.ExpenseRepository;
import com.budgetwise.repository.FinancialTrendsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class FinancialTrendsService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private FinancialTrendsRepository financialTrendsRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // ========== 1. CATEGORY BREAKDOWN (REAL DATA) ==========
    /*public Map<String, Object> getCategoryBreakdown(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("\n=== DEBUG: getCategoryBreakdown ===");
            System.out.println("User ID: " + userId);
            System.out.println("Date Range: " + startDate + " to " + endDate);

            // Get expenses from database
            List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
            System.out.println("Total expenses fetched: " + expenses.size());

            Map<String, BigDecimal> categoryTotals = new HashMap<>();
            Map<String, Integer> categoryCounts = new HashMap<>();

            // Process each expense
            for (Expense expense : expenses) {
                if (!expense.isIncome()) {  // Only expenses, not income
                    String category = expense.getCategory() != null ? expense.getCategory() : "Uncategorized";
                    BigDecimal amount = expense.getAmount();

                    categoryTotals.merge(category, amount, BigDecimal::add);
                    categoryCounts.merge(category, 1, Integer::sum);

                    System.out.println("  Category: '" + category + "', Amount: ₹" + amount +
                            ", Description: " + expense.getDescription());
                }
            }

            System.out.println("\nCategory Totals Found: " + categoryTotals.size() + " categories");
            for (Map.Entry<String, BigDecimal> entry : categoryTotals.entrySet()) {
                System.out.println("  " + entry.getKey() + ": ₹" + entry.getValue() +
                        " (" + categoryCounts.get(entry.getKey()) + " transactions)");
            }

            // Prepare response data
            List<String> categories = new ArrayList<>(categoryTotals.keySet());
            List<BigDecimal> amounts = new ArrayList<>(categoryTotals.values());
            BigDecimal total = categoryTotals.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Colors for chart
            String[] colorPalette = {
                    "#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0",
                    "#9966FF", "#FF9F40", "#8AC926", "#1982C4",
                    "#6A0572", "#FF6B6B", "#4ECDC4", "#45B7D1"
            };

            List<String> colors = new ArrayList<>();
            for (int i = 0; i < categories.size(); i++) {
                colors.add(colorPalette[i % colorPalette.length]);
            }

            response.put("labels", categories);
            response.put("data", amounts);
            response.put("colors", colors);
            response.put("total", total);
            response.put("categoryCount", categories.size());
            response.put("success", true);
            response.put("hasData", !categories.isEmpty());

        } catch (Exception e) {
            System.err.println("ERROR in getCategoryBreakdown: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("hasData", false);
        }

        return response;
    }

    // ========== 2. MONTHLY EXPENSE TRENDS (REAL DATA) ==========
    public Map<String, Object> getMonthlyExpenseTrends(Long userId, int year) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("\n=== DEBUG: getMonthlyExpenseTrends ===");
            System.out.println("User ID: " + userId);
            System.out.println("Year: " + year);

            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);

            List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
            System.out.println("Total expenses for year: " + expenses.size());

            BigDecimal[] monthlyTotals = new BigDecimal[12];
            Arrays.fill(monthlyTotals, BigDecimal.ZERO);

            for (Expense expense : expenses) {
                if (!expense.isIncome()) {
                    int month = expense.getDate().getMonthValue() - 1;
                    monthlyTotals[month] = monthlyTotals[month].add(expense.getAmount());
                }
            }

            List<String> months = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
            List<BigDecimal> amounts = Arrays.asList(monthlyTotals);

            BigDecimal yearlyTotal = Arrays.stream(monthlyTotals)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            System.out.println("Yearly total: ₹" + yearlyTotal);

            response.put("labels", months);
            response.put("data", amounts);
            response.put("year", year);
            response.put("yearlyTotal", yearlyTotal);
            response.put("success", true);
            response.put("hasData", yearlyTotal.compareTo(BigDecimal.ZERO) > 0);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("hasData", false);
        }

        return response;
    }*/
    public Map<String, Object> getCategoryBreakdown(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Use simple method that exists
            List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

            System.out.println("\n=== DEBUG: CATEGORY ANALYSIS ===");
            System.out.println("User ID: " + userId);
            System.out.println("Date Range: " + startDate + " to " + endDate);
            System.out.println("Total expenses fetched: " + expenses.size());

            // Print ALL expenses for debugging
            for (Expense expense : expenses) {
                System.out.println("Expense: " + expense.getDescription() +
                        " | Amount: " + expense.getAmount() +
                        " | Category: '" + expense.getCategory() + "'" +
                        " | Date: " + expense.getDate() +
                        " | Is Income: " + expense.isIncome());
            }

            Map<String, BigDecimal> categoryTotals = new HashMap<>();
            for (Expense expense : expenses) {
                // REMOVE the income filter temporarily for debugging
                // if (!expense.isIncome()) {
                String category = expense.getCategory();
                BigDecimal amount = expense.getAmount();

                System.out.println("Processing: Category='" + category + "', Amount=" + amount +
                        ", IsIncome=" + expense.isIncome());

                categoryTotals.merge(category, amount, BigDecimal::add);
                // }
            }

            System.out.println("\nCategory Totals Found:");
            for (Map.Entry<String, BigDecimal> entry : categoryTotals.entrySet()) {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue());
            }

            // Convert to lists for response
            List<String> categories = new ArrayList<>(categoryTotals.keySet());
            List<BigDecimal> amounts = new ArrayList<>(categoryTotals.values());

            // ADD COLORS FOR CHART
            String[] colorPalette = {
                    "#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0",
                    "#9966FF", "#FF9F40", "#8AC926", "#1982C4",
                    "#6A0572", "#FF6B6B", "#4ECDC4", "#45B7D1"
            };

            List<String> colors = new ArrayList<>();
            for (int i = 0; i < categories.size(); i++) {
                colors.add(colorPalette[i % colorPalette.length]);
            }

            response.put("labels", categories);
            response.put("data", amounts);
            response.put("colors", colors);
            response.put("total", categoryTotals.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
            response.put("categoryCount", categories.size());
            response.put("success", true);

            System.out.println("\nSending to template:");
            System.out.println("  Categories: " + categories);
            System.out.println("  Amounts: " + amounts);
            System.out.println("  Total categories: " + categories.size());

        } catch (Exception e) {
            System.err.println("ERROR in getCategoryBreakdown: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    // 2. Simple Monthly Trends (manual calculation)
    public Map<String, Object> getMonthlyExpenseTrends(Long userId, int year) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get all expenses for the year
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);
            List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

            // Initialize monthly totals
            BigDecimal[] monthlyTotals = new BigDecimal[12];
            Arrays.fill(monthlyTotals, BigDecimal.ZERO);

            // Calculate totals per month
            for (Expense expense : expenses) {
                if (!expense.isIncome()) {
                    int month = expense.getDate().getMonthValue() - 1; // 0-indexed
                    monthlyTotals[month] = monthlyTotals[month].add(expense.getAmount());
                }
            }

            // Prepare response
            List<String> months = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
            List<BigDecimal> amounts = Arrays.asList(monthlyTotals);

            response.put("labels", months);
            response.put("data", amounts);
            response.put("year", year);
            response.put("success", true);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    // ========== 3. INCOME vs EXPENSE (REAL DATA) ==========
    public Map<String, Object> getIncomeVsExpense(Long userId, int months) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("\n=== DEBUG: getIncomeVsExpense ===");
            System.out.println("User ID: " + userId);
            System.out.println("Months: " + months);

            List<String> monthsList = new ArrayList<>();
            List<BigDecimal> incomeData = new ArrayList<>();
            List<BigDecimal> expenseData = new ArrayList<>();
            List<BigDecimal> savingsData = new ArrayList<>();
            List<Map<String, Object>> monthlyDetails = new ArrayList<>();

            LocalDate endDate = LocalDate.now();
            BigDecimal totalIncome = BigDecimal.ZERO;
            BigDecimal totalExpense = BigDecimal.ZERO;

            // Loop through each month
            for (int i = months - 1; i >= 0; i--) {
                LocalDate monthDate = endDate.minusMonths(i);

                LocalDate monthStart = monthDate.withDayOfMonth(1);
                LocalDate monthEnd = monthDate.withDayOfMonth(monthDate.lengthOfMonth());

                List<Expense> monthExpenses = expenseRepository.findByUserIdAndDateBetween(userId, monthStart, monthEnd);

                System.out.println("\nMonth: " + monthDate.getMonth() + " " + monthDate.getYear());
                System.out.println("Transactions: " + monthExpenses.size());

                BigDecimal monthIncome = BigDecimal.ZERO;
                BigDecimal monthExpense = BigDecimal.ZERO;

                for (Expense expense : monthExpenses) {
                    if (expense.isIncome()) {
                        monthIncome = monthIncome.add(expense.getAmount());
                    } else {
                        monthExpense = monthExpense.add(expense.getAmount());
                    }
                }

                BigDecimal monthSavings = monthIncome.subtract(monthExpense);
                BigDecimal savingsPercentage = BigDecimal.ZERO;
                if (monthIncome.compareTo(BigDecimal.ZERO) > 0) {
                    savingsPercentage = monthSavings
                            .divide(monthIncome, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));
                }

                // Format month label: "Jan '24"
                String monthLabel = monthDate.getMonth().toString().substring(0, 3) +
                        " '" + String.format("%02d", monthDate.getYear() % 100);

                monthsList.add(monthLabel);
                incomeData.add(monthIncome);
                expenseData.add(monthExpense);
                savingsData.add(monthSavings);

                totalIncome = totalIncome.add(monthIncome);
                totalExpense = totalExpense.add(monthExpense);

                // Monthly detail
                Map<String, Object> monthDetail = new HashMap<>();
                monthDetail.put("month", monthDate.getMonth().toString() + " " + monthDate.getYear());
                monthDetail.put("income", monthIncome);
                monthDetail.put("expenses", monthExpense);
                monthDetail.put("savings", monthSavings);
                monthDetail.put("percentage", savingsPercentage.setScale(1, RoundingMode.HALF_UP));
                monthDetail.put("status", monthSavings.compareTo(BigDecimal.ZERO) >= 0 ? "Good" : "Attention");
                monthlyDetails.add(monthDetail);

                System.out.println("  Income: ₹" + monthIncome + ", Expenses: ₹" + monthExpense +
                        ", Savings: ₹" + monthSavings);
            }

            BigDecimal netSavings = totalIncome.subtract(totalExpense);
            BigDecimal overallSavingsRate = BigDecimal.ZERO;
            if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
                overallSavingsRate = netSavings
                        .divide(totalIncome, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }

            System.out.println("\n=== SUMMARY ===");
            System.out.println("Total Income: ₹" + totalIncome);
            System.out.println("Total Expenses: ₹" + totalExpense);
            System.out.println("Net Savings: ₹" + netSavings);

            // Build response
            response.put("labels", monthsList);
            response.put("income", incomeData);
            response.put("expenses", expenseData);
            response.put("savings", savingsData);
            response.put("totalIncome", totalIncome);
            response.put("totalExpenses", totalExpense);
            response.put("netSavings", netSavings);
            response.put("overallSavingsRate", overallSavingsRate);
            response.put("monthlyData", monthlyDetails);
            response.put("selectedMonths", months);
            response.put("success", true);
            response.put("hasData", totalIncome.compareTo(BigDecimal.ZERO) > 0 || totalExpense.compareTo(BigDecimal.ZERO) > 0);

        } catch (Exception e) {
            System.err.println("ERROR in getIncomeVsExpense: " + e.getMessage());
            e.printStackTrace();

            // Fallback structure
            response.put("labels", new ArrayList<>());
            response.put("income", new ArrayList<>());
            response.put("expenses", new ArrayList<>());
            response.put("savings", new ArrayList<>());
            response.put("totalIncome", BigDecimal.ZERO);
            response.put("totalExpenses", BigDecimal.ZERO);
            response.put("netSavings", BigDecimal.ZERO);
            response.put("overallSavingsRate", BigDecimal.ZERO);
            response.put("monthlyData", new ArrayList<>());
            response.put("selectedMonths", months);
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("hasData", false);
        }

        return response;
    }

    // ========== 4. FINANCIAL HEALTH SCORE (REAL DATA) ==========
    public Map<String, Object> getFinancialHealthScore(Long userId) {
        Map<String, Object> health = new HashMap<>();

        try {
            System.out.println("\n=== DEBUG: getFinancialHealthScore ===");
            System.out.println("User ID: " + userId);

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusMonths(3);

            List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
            System.out.println("Transactions for health score: " + expenses.size());

            BigDecimal totalIncome = BigDecimal.ZERO;
            BigDecimal totalExpense = BigDecimal.ZERO;

            for (Expense expense : expenses) {
                if (expense.isIncome()) {
                    totalIncome = totalIncome.add(expense.getAmount());
                } else {
                    totalExpense = totalExpense.add(expense.getAmount());
                }
            }

            // Calculate savings rate
            BigDecimal savingsRate = BigDecimal.ZERO;
            if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
                savingsRate = totalIncome.subtract(totalExpense)
                        .divide(totalIncome, 2, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }

            System.out.println("Total Income: ₹" + totalIncome);
            System.out.println("Total Expenses: ₹" + totalExpense);
            System.out.println("Savings Rate: " + savingsRate + "%");

            // Calculate overall score
            int overallScore = calculateOverallScore(savingsRate);
            String grade = getGrade(overallScore);
            String status = getStatus(overallScore);

            // Create metrics
            List<Map<String, Object>> metrics = new ArrayList<>();

            // 1. Savings Rate Metric
            int savingsScore = calculateSavingsScore(savingsRate);
            metrics.add(Map.of(
                    "name", "Savings Rate",
                    "score", savingsScore,
                    "description", getSavingsDescription(savingsRate)
            ));

            // 2. Debt-to-Income (placeholder - would need debt data)
            int debtScore = calculateDebtScore(userId);
            metrics.add(Map.of(
                    "name", "Debt-to-Income",
                    "score", debtScore,
                    "description", getDebtDescription(debtScore)
            ));

            // 3. Emergency Fund (placeholder)
            int emergencyScore = calculateEmergencyFundScore(userId);
            metrics.add(Map.of(
                    "name", "Emergency Fund",
                    "score", emergencyScore,
                    "description", getEmergencyFundDescription(emergencyScore)
            ));

            // 4. Spending Control
            int spendingScore = calculateSpendingScore(totalIncome, totalExpense);
            metrics.add(Map.of(
                    "name", "Spending Control",
                    "score", spendingScore,
                    "description", getSpendingDescription(totalIncome, totalExpense)
            ));

            // 5. Investment Ratio (placeholder)
            int investmentScore = 40;
            metrics.add(Map.of(
                    "name", "Investment Ratio",
                    "score", investmentScore,
                    "description", "Consider increasing investment contributions"
            ));

            // 6. Credit Health (placeholder)
            int creditScore = 75;
            metrics.add(Map.of(
                    "name", "Credit Health",
                    "score", creditScore,
                    "description", "Good credit management habits"
            ));

            // Generate recommendations
            List<String> recommendations = generateRecommendations(overallScore, metrics);

            // Build response
            health.put("overallScore", overallScore);
            health.put("grade", grade);
            health.put("status", status);
            health.put("metrics", metrics);
            health.put("recommendations", recommendations);
            health.put("totalIncome", totalIncome);
            health.put("totalExpense", totalExpense);
            health.put("savingsRate", savingsRate);
            health.put("message", getHealthMessage(overallScore));
            health.put("lastUpdated", LocalDate.now().toString());
            health.put("success", true);

        } catch (Exception e) {
            System.err.println("ERROR in getFinancialHealthScore: " + e.getMessage());
            e.printStackTrace();
            health.put("success", false);
            health.put("error", e.getMessage());
            health.put("overallScore", 0);
            health.put("grade", "N/A");
            health.put("status", "Error");
            health.put("metrics", new ArrayList<>());
            health.put("recommendations", Arrays.asList("Could not calculate financial health due to data issues"));
        }

        return health;
    }

    // ========== 5. YEAR COMPARISON (REAL DATA) ==========
    public Map<String, Object> getYearComparison(Long userId, int currentYear) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("\n=== DEBUG: getYearComparison ===");
            System.out.println("User ID: " + userId);
            System.out.println("Current Year: " + currentYear);

            int previousYear = currentYear - 1;

            // Get monthly data for both years
            List<BigDecimal> currentYearData = getMonthlyDataForYear(userId, currentYear);
            List<BigDecimal> previousYearData = getMonthlyDataForYear(userId, previousYear);

            // Calculate totals
            BigDecimal currentTotal = currentYearData.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal previousTotal = previousYearData.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate percentage change
            BigDecimal totalChange = BigDecimal.ZERO;
            if (previousTotal.compareTo(BigDecimal.ZERO) > 0) {
                totalChange = currentTotal.subtract(previousTotal)
                        .divide(previousTotal, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }

            System.out.println("Current Year Total: ₹" + currentTotal);
            System.out.println("Previous Year Total: ₹" + previousTotal);
            System.out.println("Change: " + totalChange.setScale(1, RoundingMode.HALF_UP) + "%");

            // Build response
            response.put("labels", Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"));
            response.put("currentYear", currentYearData);
            response.put("previousYear", previousYearData);
            response.put("currentYearTotal", currentTotal);
            response.put("previousYearTotal", previousTotal);
            response.put("totalChange", totalChange);
            response.put("currentYearValue", currentYear);
            response.put("previousYearValue", previousYear);
            response.put("success", true);
            response.put("hasData", currentTotal.compareTo(BigDecimal.ZERO) > 0 || previousTotal.compareTo(BigDecimal.ZERO) > 0);

        } catch (Exception e) {
            System.err.println("ERROR in getYearComparison: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("hasData", false);
        }

        return response;
    }

    // ========== HELPER METHODS ==========

    private List<BigDecimal> getMonthlyDataForYear(Long userId, int year) {
        List<BigDecimal> monthlyData = new ArrayList<>(Collections.nCopies(12, BigDecimal.ZERO));

        try {
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);

            List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

            for (Expense expense : expenses) {
                if (!expense.isIncome()) {
                    int month = expense.getDate().getMonthValue() - 1;
                    BigDecimal current = monthlyData.get(month);
                    monthlyData.set(month, current.add(expense.getAmount()));
                }
            }

        } catch (Exception e) {
            System.err.println("Error getting monthly data for year " + year + ": " + e.getMessage());
        }

        return monthlyData;
    }

    private int calculateOverallScore(BigDecimal savingsRate) {
        int baseScore = savingsRate.intValue();
        int score = Math.min(100, Math.max(0, baseScore + 40));
        return score;
    }

    private int calculateSavingsScore(BigDecimal savingsRate) {
        if (savingsRate.compareTo(new BigDecimal("30")) >= 0) return 95;
        if (savingsRate.compareTo(new BigDecimal("20")) >= 0) return 85;
        if (savingsRate.compareTo(new BigDecimal("10")) >= 0) return 70;
        if (savingsRate.compareTo(new BigDecimal("5")) >= 0) return 60;
        if (savingsRate.compareTo(BigDecimal.ZERO) > 0) return 40;
        return 20;
    }

    private int calculateDebtScore(Long userId) {
        // Placeholder - would need actual debt calculation
        return 75;
    }

    private int calculateEmergencyFundScore(Long userId) {
        // Placeholder - would need actual emergency fund calculation
        return 60;
    }

    private int calculateSpendingScore(BigDecimal income, BigDecimal expense) {
        if (income.compareTo(BigDecimal.ZERO) == 0) return 50;

        BigDecimal spendingRatio = expense.divide(income, 2, RoundingMode.HALF_UP);

        if (spendingRatio.compareTo(new BigDecimal("0.60")) < 0) return 90;
        if (spendingRatio.compareTo(new BigDecimal("0.80")) < 0) return 75;
        if (spendingRatio.compareTo(new BigDecimal("0.95")) < 0) return 60;
        return 40;
    }

    private String getGrade(int score) {
        if (score >= 90) return "A+";
        if (score >= 80) return "A";
        if (score >= 70) return "B";
        if (score >= 60) return "C";
        if (score >= 50) return "D";
        return "F";
    }

    private String getStatus(int score) {
        if (score >= 90) return "Outstanding";
        if (score >= 80) return "Excellent";
        if (score >= 70) return "Good";
        if (score >= 60) return "Fair";
        if (score >= 50) return "Needs Improvement";
        return "Poor";
    }

    private String getSavingsDescription(BigDecimal savingsRate) {
        if (savingsRate.compareTo(new BigDecimal("30")) >= 0) return "Excellent savings habit (>30%)";
        if (savingsRate.compareTo(new BigDecimal("20")) >= 0) return "Great savings rate (20-30%)";
        if (savingsRate.compareTo(new BigDecimal("10")) >= 0) return "Good savings rate (10-20%)";
        if (savingsRate.compareTo(new BigDecimal("5")) >= 0) return "Fair savings rate (5-10%)";
        if (savingsRate.compareTo(BigDecimal.ZERO) > 0) return "Low savings rate (<5%)";
        return "No savings detected";
    }

    private String getDebtDescription(int score) {
        if (score >= 80) return "Low debt burden";
        if (score >= 60) return "Manageable debt level";
        if (score >= 40) return "High debt - consider reduction";
        return "Critical debt level - seek advice";
    }

    private String getEmergencyFundDescription(int score) {
        if (score >= 80) return "6+ months of expenses covered";
        if (score >= 60) return "3-6 months covered";
        if (score >= 40) return "1-3 months covered";
        return "Less than 1 month covered";
    }

    private String getSpendingDescription(BigDecimal income, BigDecimal expense) {
        if (income.compareTo(BigDecimal.ZERO) == 0) return "No income data";

        BigDecimal spendingRatio = expense.divide(income, 2, RoundingMode.HALF_UP);
        BigDecimal percentage = spendingRatio.multiply(new BigDecimal("100"));
        return String.format("Spending %s%% of income", percentage.intValue());
    }

    private String getHealthMessage(int score) {
        if (score >= 90) return "Outstanding financial health! You're doing amazing.";
        if (score >= 80) return "Excellent financial habits. Keep up the good work!";
        if (score >= 70) return "Good financial health with room for improvement.";
        if (score >= 60) return "Fair. Focus on implementing recommendations.";
        if (score >= 50) return "Needs attention. Prioritize key improvements.";
        return "Critical. Take immediate action to improve finances.";
    }

    private List<String> generateRecommendations(int overallScore, List<Map<String, Object>> metrics) {
        List<String> recommendations = new ArrayList<>();

        recommendations.add("Review and update your monthly budget");
        recommendations.add("Set up automatic savings transfers");

        if (overallScore < 70) {
            recommendations.add("Build emergency fund to cover 3-6 months of expenses");
            recommendations.add("Review discretionary spending for potential cuts");
        }

        if (overallScore >= 80) {
            recommendations.add("Consider increasing investment contributions");
            recommendations.add("Explore additional income streams");
        }

        // Ensure minimum recommendations
        while (recommendations.size() < 4) {
            recommendations.add("Regularly review financial goals and progress");
        }

        return recommendations.subList(0, Math.min(recommendations.size(), 6));
    }

    // ========== OTHER METHODS (STUBS) ==========
    public Map<String, Object> getSavingGoalsProgress(Long userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Saving goals feature coming soon");
        response.put("success", true);
        return response;
    }

    public Map<String, Object> getSpendingByDayOfWeek(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Spending by day feature coming soon");
        response.put("success", true);
        return response;
    }

    private void saveTrendToCache(Long userId, String trendType, Map<String, Object> data) {
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            FinancialTrends trend = new FinancialTrends(userId, LocalDate.now(), trendType, jsonData);
            financialTrendsRepository.save(trend);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}