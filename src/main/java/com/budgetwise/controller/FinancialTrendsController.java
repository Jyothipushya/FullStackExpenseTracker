package com.budgetwise.controller;

import com.budgetwise.service.FinancialTrendsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/trends")
public class FinancialTrendsController {

    @Autowired
    private FinancialTrendsService trendsService;

    // Main trends dashboard
    @GetMapping("/dashboard")
    public String showTrendsDashboard(Model model) {
        Long userId = 1L; // TODO: Get from session/authentication

        // Get current month/year for defaults
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());

        // Load trend data from service
        Map<String, Object> monthlyTrends = trendsService.getMonthlyExpenseTrends(userId, currentYear);
        Map<String, Object> categoryBreakdown = trendsService.getCategoryBreakdown(userId, monthStart, monthEnd);
        Map<String, Object> incomeVsExpense = trendsService.getIncomeVsExpense(userId, 6);
        Map<String, Object> healthScore = trendsService.getFinancialHealthScore(userId);

        // Add mock goals data for template
        Map<String, Object> goalsProgress = new HashMap<>();
        goalsProgress.put("goals", Arrays.asList("Emergency Fund", "Vacation", "New Car", "Home Down Payment"));
        goalsProgress.put("current", Arrays.asList(50000.0, 20000.0, 15000.0, 30000.0));
        goalsProgress.put("target", Arrays.asList(100000.0, 50000.0, 80000.0, 200000.0));
        goalsProgress.put("percentages", Arrays.asList(50.0, 40.0, 18.75, 15.0));
        goalsProgress.put("status", Arrays.asList("Halfway", "Started", "Just Started", "Just Started"));

        // Convert data to JSON for JavaScript charts
        ObjectMapper mapper = new ObjectMapper();
        try {
            model.addAttribute("monthlyTrendsJson", mapper.writeValueAsString(monthlyTrends));
            model.addAttribute("categoryBreakdownJson", mapper.writeValueAsString(categoryBreakdown));
            model.addAttribute("incomeVsExpenseJson", mapper.writeValueAsString(incomeVsExpense));
        } catch (Exception e) {
            // Fallback if JSON conversion fails
            model.addAttribute("monthlyTrendsJson", "{}");
            model.addAttribute("categoryBreakdownJson", "{}");
            model.addAttribute("incomeVsExpenseJson", "{}");
        }

        // Add all attributes to model
        model.addAttribute("userId", userId);
        model.addAttribute("monthlyTrends", monthlyTrends);
        model.addAttribute("categoryBreakdown", categoryBreakdown);
        model.addAttribute("incomeVsExpense", incomeVsExpense);
        model.addAttribute("healthScore", healthScore);
        model.addAttribute("goalsProgress", goalsProgress);
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("userName", "Jyothi Pushya");

        return "financial-trends";
    }

    // REST API endpoints for AJAX calls

    @GetMapping("/api/monthly-trends")
    @ResponseBody
    public Map<String, Object> getMonthlyTrendsApi(@RequestParam int year) {
        Long userId = 1L; // TODO: Get from session/authentication
        return trendsService.getMonthlyExpenseTrends(userId, year);
    }

    @GetMapping("/api/category-breakdown")
    @ResponseBody
    public Map<String, Object> getCategoryBreakdownApi(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = 1L; // TODO: Get from session/authentication
        return trendsService.getCategoryBreakdown(userId, startDate, endDate);
    }

    @GetMapping("/api/income-vs-expense")
    @ResponseBody
    public Map<String, Object> getIncomeVsExpenseApi(@RequestParam int months) {
        Long userId = 1L; // TODO: Get from session/authentication
        return trendsService.getIncomeVsExpense(userId, months);
    }

    @GetMapping("/api/financial-health")
    @ResponseBody
    public Map<String, Object> getFinancialHealthApi() {
        Long userId = 1L; // TODO: Get from session/authentication
        return trendsService.getFinancialHealthScore(userId);
    }

    // Additional analysis pages

    /*@GetMapping("/category-analysis")
    public String showCategoryAnalysis(Model model) {
        Long userId = 1L; // TODO: Get from session/authentication
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());

        Map<String, Object> categoryData = trendsService.getCategoryBreakdown(userId, monthStart, monthEnd);

        // Convert to JSON for JavaScript
        ObjectMapper mapper = new ObjectMapper();
        try {
            model.addAttribute("categoryDataJson", mapper.writeValueAsString(categoryData));
        } catch (Exception e) {
            model.addAttribute("categoryDataJson", "{}");
        }

        model.addAttribute("categoryData", categoryData);
        model.addAttribute("startDate", monthStart);
        model.addAttribute("endDate", monthEnd);
        model.addAttribute("userName", "Jyothi Pushya");

        return "category-analysis";
    }*/
   /* @GetMapping("/category-analysis")
    public String showCategoryAnalysis(Model model) {
        Long userId = 1L; // TODO: Get from session/authentication
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());

        // Get raw category data from service
        Map<String, Object> rawCategoryData = trendsService.getCategoryBreakdown(userId, monthStart, monthEnd);

        // Transform to template-friendly format
        Map<String, Object> categoryData = new HashMap<>();

        if (rawCategoryData != null && rawCategoryData.containsKey("categories")) {
            // If service returns categories list
            List<Map<String, Object>> categories = (List<Map<String, Object>>) rawCategoryData.get("categories");

            List<String> labels = new ArrayList<>();
            List<Double> data = new ArrayList<>();
            List<Double> percentages = new ArrayList<>();
            List<String> colors = new ArrayList<>();

            double total = categories.stream()
                    .mapToDouble(cat -> (Double) cat.getOrDefault("amount", 0.0))
                    .sum();

            for (Map<String, Object> category : categories) {
                labels.add((String) category.getOrDefault("name", "Unknown"));
                double amount = (Double) category.getOrDefault("amount", 0.0);
                data.add(amount);

                double percentage = total > 0 ? (amount / total) * 100 : 0;
                percentages.add(Math.round(percentage * 100.0) / 100.0); // Round to 2 decimals

                // Generate or get color
                String color = (String) category.getOrDefault("color",
                        String.format("#%06x", new Random().nextInt(0xffffff + 1)));
                colors.add(color);
            }

            categoryData.put("labels", labels);
            categoryData.put("data", data);
            categoryData.put("percentages", percentages);
            categoryData.put("colors", colors);
        } else {
            // Fallback mock data for testing
            categoryData.put("labels", Arrays.asList("Food", "Transport", "Shopping", "Entertainment", "Bills"));
            categoryData.put("data", Arrays.asList(15000.0, 8000.0, 12000.0, 5000.0, 10000.0));
            categoryData.put("percentages", Arrays.asList(30.0, 16.0, 24.0, 10.0, 20.0));
            categoryData.put("colors", Arrays.asList("#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF"));
        }

        // Convert to JSON for JavaScript
        ObjectMapper mapper = new ObjectMapper();
        try {
            model.addAttribute("categoryDataJson", mapper.writeValueAsString(categoryData));
        } catch (Exception e) {
            model.addAttribute("categoryDataJson", "{}");
        }

        model.addAttribute("categoryData", categoryData);
        model.addAttribute("startDate", monthStart);
        model.addAttribute("endDate", monthEnd);
        model.addAttribute("userName", "Jyothi Pushya");

        // Debug: Check what data is being passed
        System.out.println("Category Data: " + categoryData);

        return "category-analysis";
    }*/
    /*@GetMapping("/category-analysis")
    public String showCategoryAnalysis(Model model) {
        Long userId = 1L;

        // GUARANTEED DATA STRUCTURE - Always works
        Map<String, Object> categoryData = new HashMap<>();

        // Sample data - always present
        List<String> labels = Arrays.asList("Food & Dining", "Transportation", "Shopping", "Entertainment", "Bills", "Healthcare");
        List<Double> data = Arrays.asList(15000.0, 8500.0, 12000.0, 5000.0, 10000.0, 4500.0);
        List<Double> percentages = Arrays.asList(27.3, 15.5, 21.8, 9.1, 18.2, 8.2);
        List<String> colors = Arrays.asList("#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF", "#FF9F40");

        categoryData.put("labels", labels);
        categoryData.put("data", data);
        categoryData.put("percentages", percentages);
        categoryData.put("colors", colors);

        // Add to model
        model.addAttribute("categoryData", categoryData);

        // Convert to JSON - CRITICAL for JavaScript
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(categoryData);
            model.addAttribute("categoryDataJson", json);
            System.out.println("JSON Data: " + json); // Debug print
        } catch (Exception e) {
            model.addAttribute("categoryDataJson", "{}");
            System.out.println("JSON Error: " + e.getMessage());
        }

        model.addAttribute("userName", "Jyothi Pushya");

        return "category-analysis";
    }*/
    @GetMapping("/category-analysis")
    public String showCategoryAnalysis(
            @RequestParam(defaultValue = "30") int days,
            Model model) {

        Long userId = 1L; // TODO: Get from session/authentication

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        System.out.println("=== CATEGORY ANALYSIS (REAL DATA) ===");
        System.out.println("User ID: " + userId);
        System.out.println("Period: " + startDate + " to " + endDate);
        System.out.println("Days: " + days);

        // CALL THE REAL SERVICE METHOD
        Map<String, Object> serviceResponse = trendsService.getCategoryBreakdown(userId, startDate, endDate);

        boolean success = (Boolean) serviceResponse.getOrDefault("success", false);

        Map<String, Object> categoryData;

        if (success) {
            // Extract data from service response
            List<String> labels = (List<String>) serviceResponse.get("labels");
            List<Object> rawData = (List<Object>) serviceResponse.get("data");
            List<String> colors = (List<String>) serviceResponse.get("colors");
            Object totalObj = serviceResponse.get("total");

            System.out.println("Service returned success!");
            System.out.println("Found " + labels.size() + " categories");
            System.out.println("Labels: " + labels);

            // Convert data to Double (handle BigDecimal/Integer)
            List<Double> data = new ArrayList<>();
            List<Double> percentages = new ArrayList<>();

            double total = 0;
            if (totalObj instanceof java.math.BigDecimal) {
                total = ((java.math.BigDecimal) totalObj).doubleValue();
            } else if (totalObj instanceof Double) {
                total = (Double) totalObj;
            }

            for (Object amountObj : rawData) {
                double amount = 0;
                if (amountObj instanceof java.math.BigDecimal) {
                    amount = ((java.math.BigDecimal) amountObj).doubleValue();
                } else if (amountObj instanceof Double) {
                    amount = (Double) amountObj;
                } else if (amountObj instanceof Integer) {
                    amount = ((Integer) amountObj).doubleValue();
                }
                data.add(amount);

                // Calculate percentage
                double percentage = total > 0 ? (amount / total) * 100 : 0;
                percentages.add(Math.round(percentage * 100.0) / 100.0);
            }

            // Build the category data map
            categoryData = new HashMap<>();
            categoryData.put("labels", labels);
            categoryData.put("data", data);
            categoryData.put("percentages", percentages);
            categoryData.put("colors", colors);
            categoryData.put("totalSpent", total);
            categoryData.put("categoryCount", labels.size());
            categoryData.put("hasData", !labels.isEmpty());

            System.out.println("Total spent: " + total);

        } else {
            // Service failed, use empty data
            System.out.println("Service failed: " + serviceResponse.get("error"));

            categoryData = new HashMap<>();
            categoryData.put("labels", new ArrayList<>());
            categoryData.put("data", new ArrayList<>());
            categoryData.put("percentages", new ArrayList<>());
            categoryData.put("colors", new ArrayList<>());
            categoryData.put("totalSpent", 0.0);
            categoryData.put("categoryCount", 0);
            categoryData.put("hasData", false);
        }

        // Convert to JSON for JavaScript
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(categoryData);
            model.addAttribute("categoryDataJson", json);
            System.out.println("JSON created successfully");
        } catch (Exception e) {
            model.addAttribute("categoryDataJson", "{}");
            System.err.println("JSON Error: " + e.getMessage());
        }

        // Add to model
        model.addAttribute("categoryData", categoryData);
        model.addAttribute("days", days);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("hasData", categoryData.get("hasData"));
        model.addAttribute("userName", "Jyothi Pushya");

        return "category-analysis";
    }

    /*@GetMapping("/income-expense-analysis")
    public String showIncomeExpenseAnalysis(
            @RequestParam(defaultValue = "6") int months,
            Model model) {

        Long userId = 1L; // TODO: Get from session/authentication

        System.out.println("=== INCOME vs EXPENSE (REAL DATA) ===");
        System.out.println("Months: " + months);

        // CALL THE REAL SERVICE METHOD
        Map<String, Object> serviceResponse = trendsService.getIncomeVsExpense(userId, months);

        boolean success = (Boolean) serviceResponse.getOrDefault("success", false);

        Map<String, Object> incomeExpenseData;

        if (success) {
            // Extract data from service response
            List<String> labels = (List<String>) serviceResponse.get("labels");
            List<Object> incomeRaw = (List<Object>) serviceResponse.get("income");
            List<Object> expensesRaw = (List<Object>) serviceResponse.get("expenses");

            System.out.println("Service returned success!");
            System.out.println("Months: " + labels);

            // Convert data to Double
            List<Double> income = new ArrayList<>();
            List<Double> expenses = new ArrayList<>();
            List<Double> savings = new ArrayList<>();

            double totalIncome = 0;
            double totalExpenses = 0;

            for (int i = 0; i < incomeRaw.size(); i++) {
                // Convert income
                double incomeVal = 0;
                Object incomeObj = incomeRaw.get(i);
                if (incomeObj instanceof java.math.BigDecimal) {
                    incomeVal = ((java.math.BigDecimal) incomeObj).doubleValue();
                } else if (incomeObj instanceof Double) {
                    incomeVal = (Double) incomeObj;
                } else if (incomeObj instanceof Integer) {
                    incomeVal = ((Integer) incomeObj).doubleValue();
                }
                income.add(incomeVal);
                totalIncome += incomeVal;

                // Convert expenses
                double expenseVal = 0;
                Object expenseObj = expensesRaw.get(i);
                if (expenseObj instanceof java.math.BigDecimal) {
                    expenseVal = ((java.math.BigDecimal) expenseObj).doubleValue();
                } else if (expenseObj instanceof Double) {
                    expenseVal = (Double) expenseObj;
                } else if (expenseObj instanceof Integer) {
                    expenseVal = ((Integer) expenseObj).doubleValue();
                }
                expenses.add(expenseVal);
                totalExpenses += expenseVal;

                // Calculate savings
                double saving = incomeVal - expenseVal;
                savings.add(saving);
            }

            // Create detailed monthly data
            List<Map<String, Object>> monthlyData = new ArrayList<>();
            for (int i = 0; i < labels.size(); i++) {
                Map<String, Object> month = new HashMap<>();
                month.put("month", labels.get(i));
                month.put("income", income.get(i));
                month.put("expenses", expenses.get(i));
                month.put("savings", savings.get(i));
                month.put("percentage", income.get(i) > 0 ?
                        Math.round((savings.get(i) / income.get(i)) * 100) : 0);
                monthlyData.add(month);
            }

            // Build the response
            incomeExpenseData = new HashMap<>();
            incomeExpenseData.put("labels", labels);
            incomeExpenseData.put("income", income);
            incomeExpenseData.put("expenses", expenses);
            incomeExpenseData.put("savings", savings);
            incomeExpenseData.put("totalIncome", totalIncome);
            incomeExpenseData.put("totalExpenses", totalExpenses);
            incomeExpenseData.put("netSavings", totalIncome - totalExpenses);
            incomeExpenseData.put("monthlyData", monthlyData);
            incomeExpenseData.put("selectedMonths", months);

            System.out.println("Total Income: " + totalIncome);
            System.out.println("Total Expenses: " + totalExpenses);

        } else {
            // Service failed, use mock data as fallback
            System.out.println("Service failed, using fallback data");

            incomeExpenseData = new HashMap<>();
            incomeExpenseData.put("totalIncome", 438000.0);
            incomeExpenseData.put("totalExpenses", 318000.0);
            incomeExpenseData.put("netSavings", 120000.0);
            incomeExpenseData.put("labels", Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun"));
            incomeExpenseData.put("income", Arrays.asList(65000.0, 72000.0, 68000.0, 75000.0, 80000.0, 78000.0));
            incomeExpenseData.put("expenses", Arrays.asList(45000.0, 52000.0, 48000.0, 55000.0, 60000.0, 58000.0));
            incomeExpenseData.put("savings", Arrays.asList(20000.0, 20000.0, 20000.0, 20000.0, 20000.0, 20000.0));
            incomeExpenseData.put("selectedMonths", months);
        }

        // Convert to JSON
        ObjectMapper mapper = new ObjectMapper();
        try {
            model.addAttribute("incomeExpenseJson", mapper.writeValueAsString(incomeExpenseData));
        } catch (Exception e) {
            model.addAttribute("incomeExpenseJson", "{}");
        }

        // Add to model
        model.addAttribute("incomeExpenseData", incomeExpenseData);
        model.addAttribute("userName", "Jyothi Pushya");

        return "income-expense-analysis";
    }*/
    @GetMapping("/income-expense-analysis")
    public String showIncomeExpenseAnalysis(
            @RequestParam(defaultValue = "6") int months,
            Model model) {

        Long userId = 1L;
        Map<String, Object> incomeExpenseData = trendsService.getIncomeVsExpense(userId, months);

        // Convert to JSON
        ObjectMapper mapper = new ObjectMapper();
        try {
            String incomeExpenseJson = mapper.writeValueAsString(incomeExpenseData);
            model.addAttribute("incomeExpenseJson", incomeExpenseJson);
        } catch (Exception e) {
            model.addAttribute("incomeExpenseJson", "{}");
        }

        model.addAttribute("incomeExpenseData", incomeExpenseData);
        model.addAttribute("userName", "Jyothi Pushya");

        return "income-expense-analysis";
    }

    /*@GetMapping("/financial-health")
    public String showFinancialHealth(Model model) {
        Long userId = 1L; // TODO: Get from session/authentication
        Map<String, Object> healthData = trendsService.getFinancialHealthScore(userId);

        // Convert to JSON for JavaScript
        ObjectMapper mapper = new ObjectMapper();
        try {
            model.addAttribute("healthDataJson", mapper.writeValueAsString(healthData));
        } catch (Exception e) {
            model.addAttribute("healthDataJson", "{}");
        }

        model.addAttribute("healthData", healthData);
        model.addAttribute("userName", "Jyothi Pushya");

        return "financial-health";
    }*/
    @GetMapping("/financial-health")
    public String showFinancialHealth(Model model) {
        Long userId = 1L; // TODO: Get from session/authentication

        // Get REAL data from service
        Map<String, Object> healthData = trendsService.getFinancialHealthScore(userId);

        // Log for debugging
        System.out.println("=== FINANCIAL HEALTH DATA ===");
        System.out.println("Overall Score: " + healthData.get("overallScore"));
        System.out.println("Metrics Count: " +
                (healthData.containsKey("metrics") ?
                        ((List<?>) healthData.get("metrics")).size() : 0));
        System.out.println("Recommendations Count: " +
                (healthData.containsKey("recommendations") ?
                        ((List<?>) healthData.get("recommendations")).size() : 0));

        // Convert to JSON for JavaScript - IMPORTANT!
        ObjectMapper mapper = new ObjectMapper();
        try {
            String healthDataJson = mapper.writeValueAsString(healthData);
            model.addAttribute("healthDataJson", healthDataJson);
            System.out.println("JSON Data: " + healthDataJson);
        } catch (Exception e) {
            model.addAttribute("healthDataJson", "{}");
            System.err.println("JSON Error: " + e.getMessage());
        }

        // Add the actual data object to model
        model.addAttribute("healthData", healthData);
        model.addAttribute("userName", "Jyothi Pushya");

        return "financial-health";
    }

    @GetMapping("/goals-analytics")
    public String showGoalsAnalytics(
            @RequestParam(defaultValue = "6") int months,
            Model model) {
        Long userId = 1L;

        // Analytics Data
        Map<String, Object> analyticsData = new HashMap<>();

        // Goal Data
        List<String> goals = Arrays.asList("Emergency Fund", "Vacation", "New Car", "Home Down Payment");
        List<Double> current = Arrays.asList(50000.0, 20000.0, 15000.0, 30000.0);
        List<Double> target = Arrays.asList(100000.0, 50000.0, 80000.0, 200000.0);
        List<Double> percentages = Arrays.asList(50.0, 40.0, 18.75, 15.0);

        // Calculations
        double totalSaved = current.stream().mapToDouble(Double::doubleValue).sum();
        double totalTarget = target.stream().mapToDouble(Double::doubleValue).sum();
        double overallProgress = totalTarget > 0 ? (totalSaved / totalTarget) * 100 : 0;

        // Monthly Trends (Simplified)
        List<String> monthsLabels = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun");
        List<Double> monthlySaved = Arrays.asList(10000.0, 15000.0, 20000.0, 25000.0, 30000.0, 35000.0);

        // AI Predictions
        List<Map<String, Object>> predictions = Arrays.asList(
                Map.of("goal", "Emergency Fund", "months", 2, "confidence", "High", "status", "Ahead"),
                Map.of("goal", "Vacation", "months", 5, "confidence", "Medium", "status", "On Track"),
                Map.of("goal", "New Car", "months", 18, "confidence", "Low", "status", "Behind"),
                Map.of("goal", "Home Down Payment", "months", 42, "confidence", "Medium", "status", "Behind")
        );

        // Recommendations
        List<String> recommendations = Arrays.asList(
                "Increase Emergency Fund monthly by ₹2,500 to finish 1 month early",
                "Consider reducing Vacation budget by 10% to accelerate New Car goal",
                "Automate ₹12,000 monthly to Home Down Payment",
                "Review and optimize discretionary spending"
        );

        // Build analytics data
        analyticsData.put("goals", goals);
        analyticsData.put("current", current);
        analyticsData.put("target", target);
        analyticsData.put("percentages", percentages);
        analyticsData.put("totalSaved", totalSaved);
        analyticsData.put("totalTarget", totalTarget);
        analyticsData.put("overallProgress", overallProgress);
        analyticsData.put("monthsLabels", monthsLabels);
        analyticsData.put("monthlySaved", monthlySaved);
        analyticsData.put("predictions", predictions);
        analyticsData.put("recommendations", recommendations);
        analyticsData.put("selectedMonths", months);

        // Add to model
        model.addAttribute("analyticsData", analyticsData);
        model.addAttribute("userName", "Jyothi Pushya");

        return "trends/goals-analytics";
    }

    /*@GetMapping("/year-comparison")
    public String showYearComparison(Model model) {
        Long userId = 1L; // TODO: Get from session/authentication

        // Mock data for year comparison
        Map<String, Object> yearData = new HashMap<>();
        yearData.put("labels", Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"));
        yearData.put("currentYear", Arrays.asList(1000, 1500, 1200, 1800, 2000, 1700, 1900, 1600, 1400, 1300, 1500, 1700));
        yearData.put("previousYear", Arrays.asList(900, 1400, 1100, 1600, 1900, 1500, 1800, 1400, 1300, 1200, 1400, 1600));

        model.addAttribute("yearData", yearData);
        model.addAttribute("userName", "Jyothi Pushya");

        return "year-comparison";
    }

    // Debug endpoint to check data
    @GetMapping("/debug")
    @ResponseBody
    public Map<String, Object> debugData() {
        Long userId = 1L;
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());

        Map<String, Object> debug = new HashMap<>();
        debug.put("categoryBreakdown", trendsService.getCategoryBreakdown(userId, monthStart, monthEnd));
        debug.put("monthlyTrends", trendsService.getMonthlyExpenseTrends(userId, now.getYear()));
        debug.put("incomeVsExpense", trendsService.getIncomeVsExpense(userId, 6));
        debug.put("healthScore", trendsService.getFinancialHealthScore(userId));

        return debug;
    }*/
    @GetMapping("/year-comparison")
    public String showYearComparison(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year,
            Model model) {

        Long userId = 1L; // TODO: Get from session/authentication

        // Get REAL data from service
        Map<String, Object> yearData = trendsService.getYearComparison(userId, year);

        // Convert to JSON for JavaScript
        ObjectMapper mapper = new ObjectMapper();
        try {
            String yearDataJson = mapper.writeValueAsString(yearData);
            model.addAttribute("yearDataJson", yearDataJson);
        } catch (Exception e) {
            model.addAttribute("yearDataJson", "{}");
        }

        model.addAttribute("yearData", yearData);
        model.addAttribute("selectedYear", year);
        model.addAttribute("userName", "Jyothi Pushya");

        return "year-comparison";
    }
}