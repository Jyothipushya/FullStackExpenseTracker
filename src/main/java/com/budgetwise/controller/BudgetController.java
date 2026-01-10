package com.budgetwise.controller;

import com.budgetwise.entity.User;
import com.budgetwise.service.BudgetService;
import com.budgetwise.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.math.RoundingMode; // ADD THIS IMPORT

@Controller
@RequestMapping("/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private UserService userService;

    /**
     * Show budget dashboard
     */
    @GetMapping
    public String showBudgetDashboard(Model model) {
        System.out.println("=== DEBUG: Budget Dashboard START ===");

        try {
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            System.out.println("DEBUG: User email: " + email);

            User user = userService.findByEmail(email);
            System.out.println("DEBUG: User found: " + (user != null));

            if (user == null) {
                return "redirect:/login";
            }

            // Get budget progress data - SIMPLIFIED FOR TESTING
            List<Map<String, Object>> budgetProgress = new ArrayList<>();
            Map<String, Object> budgetSummary = new HashMap<>();
            List<String> aiSuggestions = new ArrayList<>();

            try {
                budgetProgress = budgetService.getAllBudgetProgress(user);
                budgetSummary = budgetService.getBudgetSummary(user);
                aiSuggestions = budgetService.getAISuggestions(user);
            } catch (Exception e) {
                System.out.println("DEBUG: Service error, using empty data: " + e.getMessage());
                // Use empty data if service fails
                budgetSummary.put("totalBudget", 0);
                budgetSummary.put("totalSpent", 0);
                budgetSummary.put("totalRemaining", 0);
                budgetSummary.put("overallPercentage", 0);
            }

            // Add to model
            model.addAttribute("userName", user.getFullName());
            model.addAttribute("budgetProgress", budgetProgress);
            model.addAttribute("budgetSummary", budgetSummary);
            model.addAttribute("aiSuggestions", aiSuggestions);
            model.addAttribute("hasBudgets", !budgetProgress.isEmpty());

            System.out.println("✅ Budget Dashboard loaded for: " + user.getEmail());
            System.out.println("📊 Budgets found: " + budgetProgress.size());

            System.out.println("=== DEBUG: Budget Dashboard SUCCESS ===");

        } catch (Exception e) {
            System.out.println("❌ Error loading budget dashboard: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to load budget data: " + e.getMessage());
        }

        return "budgets";
    }

    /**
     * Show budget setup page - FIXED VERSION
     */
    @GetMapping("/setup")
    public String showBudgetSetupPage(Model model) {
        System.out.println("=== DEBUG: Budget Setup Page START ===");

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            System.out.println("DEBUG: User email: " + email);

            User user = userService.findByEmail(email);
            System.out.println("DEBUG: User found: " + (user != null));

            if (user == null) {
                System.out.println("DEBUG: User is null, redirecting to login");
                return "redirect:/login";
            }

            // Get existing budgets for pre-filling form - SIMPLIFIED
            List<Map<String, Object>> existingBudgets = new ArrayList<>();
            try {
                existingBudgets = budgetService.getAllBudgetProgress(user);
            } catch (Exception e) {
                System.out.println("DEBUG: Could not load existing budgets: " + e.getMessage());
            }

            // Common budget categories
            String[] commonCategories = {
                    "Food & Dining", "Shopping", "Transportation",
                    "Entertainment", "Bills & Utilities", "Rent/Mortgage",
                    "Healthcare", "Education", "Personal Care",
                    "Savings & Investments", "Others"
            };

            model.addAttribute("userName", user.getFullName());
            model.addAttribute("existingBudgets", existingBudgets);
            model.addAttribute("commonCategories", commonCategories);
            model.addAttribute("hasExistingBudgets", !existingBudgets.isEmpty());

            System.out.println("DEBUG: Model attributes added successfully");
            System.out.println("=== DEBUG: Budget Setup Page SUCCESS ===");

        } catch (Exception e) {
            System.out.println("=== DEBUG: Budget Setup Page ERROR ===");
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to load setup page: " + e.getMessage());
            return "error"; // Make sure you have error.html template
        }

        return "budget-setup";
    }

    /**
     * Save/update budget
     */
    @PostMapping("/save")
    public String saveBudget(@RequestParam String category,
                             @RequestParam BigDecimal amount,
                             RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            User user = userService.findByEmail(email);

            if (user == null) {
                return "redirect:/login";
            }

            // Save budget
            budgetService.setBudget(user, category, amount);

            redirectAttributes.addFlashAttribute("success",
                    "Budget for " + category + " set to ₹" + amount + " successfully!");

            System.out.println("✅ Budget saved: " + category + " - ₹" + amount + " for " + user.getEmail());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Failed to save budget: " + e.getMessage());
        }

        return "redirect:/budgets";
    }

    /**
     * Delete budget
     */
    @GetMapping("/delete/{budgetId}")
    public String deleteBudget(@PathVariable Long budgetId,
                               RedirectAttributes redirectAttributes) {
        try {
            budgetService.deleteBudget(budgetId);
            redirectAttributes.addFlashAttribute("success", "Budget deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete budget");
        }
        return "redirect:/budgets";
    }

    /**
     * Get AI recommended budgets based on income
     */
    @GetMapping("/recommendations")
    @ResponseBody
    public Map<String, BigDecimal> getBudgetRecommendations(@RequestParam BigDecimal monthlyIncome) {
        return budgetService.getRecommendedBudgets(monthlyIncome);
    }

    /**
     * Quick budget setup with AI recommendations
     */
    @PostMapping("/quick-setup")
    public String quickBudgetSetup(@RequestParam BigDecimal monthlyIncome,
                                   RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            User user = userService.findByEmail(email);

            if (user == null) {
                return "redirect:/login";
            }

            // Get AI recommendations
            Map<String, BigDecimal> recommendations = budgetService.getRecommendedBudgets(monthlyIncome);

            // Save all recommended budgets
            for (Map.Entry<String, BigDecimal> entry : recommendations.entrySet()) {
                budgetService.setBudget(user, entry.getKey(), entry.getValue());
            }

            redirectAttributes.addFlashAttribute("success",
                    "AI-generated budgets set successfully based on your income of ₹" + monthlyIncome + "!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Failed to setup budgets: " + e.getMessage());
        }

        return "redirect:/budgets";
    }

    /**
     * Show Budget & Saving Goals Combined Dashboard
     */
    @GetMapping("/budget-and-goals")
    public String showBudgetAndGoals(Model model) {
        System.out.println("=== DEBUG: Budget & Goals Dashboard START ===");

        try {
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            System.out.println("DEBUG: User email: " + email);

            User user = userService.findByEmail(email);
            System.out.println("DEBUG: User found: " + (user != null));

            if (user == null) {
                return "redirect:/login";
            }

            // Get budget data - SIMPLIFIED
            List<Map<String, Object>> budgetProgress = new ArrayList<>();
            Map<String, Object> budgetSummary = new HashMap<>();

            try {
                budgetProgress = budgetService.getAllBudgetProgress(user);
                budgetSummary = budgetService.getBudgetSummary(user);
            } catch (Exception e) {
                System.out.println("DEBUG: Could not load budget data: " + e.getMessage());
                budgetSummary.put("totalBudget", 0);
                budgetSummary.put("totalSpent", 0);
                budgetSummary.put("totalRemaining", 0);
                budgetSummary.put("overallPercentage", 0);
            }

            // Add to model
            model.addAttribute("userName", user.getFullName());
            model.addAttribute("budgetProgress", budgetProgress);
            model.addAttribute("budgetSummary", budgetSummary);
            model.addAttribute("hasBudgets", !budgetProgress.isEmpty());

            // Create sample goals data for testing
            List<Map<String, Object>> goals = createSampleGoals();
            Map<String, Object> goalsSummary = calculateGoalsSummary(goals);

            model.addAttribute("goals", goals);
            model.addAttribute("goalsSummary", goalsSummary);
            model.addAttribute("income", 50000.00); // Default for testing

            // Add combined AI suggestions
            List<String> budgetSuggestions = new ArrayList<>();
            try {
                budgetSuggestions = budgetService.getAISuggestions(user);
            } catch (Exception e) {
                System.out.println("DEBUG: Could not load AI suggestions: " + e.getMessage());
            }

            List<String> goalSuggestions = generateGoalSuggestions(goals);
            List<String> allSuggestions = new ArrayList<>();
            allSuggestions.addAll(budgetSuggestions);
            allSuggestions.addAll(goalSuggestions);
            model.addAttribute("aiSuggestions", allSuggestions);

            System.out.println("✅ Budget & Goals Dashboard loaded for: " + user.getEmail());
            System.out.println("📊 Budgets: " + budgetProgress.size() + ", Goals: " + goals.size());

            System.out.println("=== DEBUG: Budget & Goals Dashboard SUCCESS ===");

        } catch (Exception e) {
            System.out.println("❌ Error loading budget & goals: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to load budget & goals data: " + e.getMessage());
        }

        return "budget-and-goals";
    }

    /**
     * Show Create Goal Form - FIXED VERSION
     */
    @GetMapping("/create-goal")
    public String showCreateGoalForm(Model model) {
        System.out.println("=== DEBUG: Create Goal Form START ===");

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            System.out.println("DEBUG: User email: " + email);

            User user = userService.findByEmail(email);
            System.out.println("DEBUG: User found: " + (user != null));

            if (user == null) {
                System.out.println("DEBUG: User is null, redirecting to login");
                return "redirect:/login";
            }

            model.addAttribute("userName", user.getFullName());

            // Add categories for dropdown
            List<String> categories = new ArrayList<>();
            categories.add("VACATION");
            categories.add("HOUSE");
            categories.add("CAR");
            categories.add("EMERGENCY");
            categories.add("EDUCATION");
            categories.add("GADGET");
            categories.add("OTHER");
            model.addAttribute("categories", categories);
            model.addAttribute("savingGoal", new HashMap<String, Object>());


            System.out.println("✅ Create Goal Form loaded");
            System.out.println("=== DEBUG: Create Goal Form SUCCESS ===");

        } catch (Exception e) {
            System.out.println("=== DEBUG: Create Goal Form ERROR ===");
            System.out.println("❌ Error loading create goal form: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to load form: " + e.getMessage());
            return "error";
        }

        return "goal-setup";
    }

    /**
     * Helper method to create sample goals for testing
     */
    private List<Map<String, Object>> createSampleGoals() {
        List<Map<String, Object>> goals = new ArrayList<>();

        // Goal 1: Emergency Fund
        Map<String, Object> goal1 = new HashMap<>();
        goal1.put("id", 1L);
        goal1.put("name", "Emergency Fund");
        goal1.put("category", "EMERGENCY");
        goal1.put("icon", "🛡️");
        goal1.put("targetAmount", 300000.00);
        goal1.put("savedAmount", 210000.00);
        goal1.put("monthlyContribution", 10000.00);
        goal1.put("priority", "HIGH");
        goal1.put("progressPercentage", 70.0);
        goal1.put("monthsRemaining", 9);
        goals.add(goal1);

        // Goal 2: Europe Vacation
        Map<String, Object> goal2 = new HashMap<>();
        goal2.put("id", 2L);
        goal2.put("name", "Europe Vacation");
        goal2.put("category", "VACATION");
        goal2.put("icon", "✈️");
        goal2.put("targetAmount", 500000.00);
        goal2.put("savedAmount", 200000.00);
        goal2.put("monthlyContribution", 15000.00);
        goal2.put("priority", "MEDIUM");
        goal2.put("progressPercentage", 40.0);
        goal2.put("monthsRemaining", 20);
        goals.add(goal2);

        // Goal 3: New Car
        Map<String, Object> goal3 = new HashMap<>();
        goal3.put("id", 3L);
        goal3.put("name", "New Car");
        goal3.put("category", "CAR");
        goal3.put("icon", "🚗");
        goal3.put("targetAmount", 800000.00);
        goal3.put("savedAmount", 120000.00);
        goal3.put("monthlyContribution", 20000.00);
        goal3.put("priority", "MEDIUM");
        goal3.put("progressPercentage", 15.0);
        goal3.put("monthsRemaining", 34);
        goals.add(goal3);

        return goals;
    }

    /**
     * Helper method to calculate goals summary
     */
    private Map<String, Object> calculateGoalsSummary(List<Map<String, Object>> goals) {
        Map<String, Object> summary = new HashMap<>();

        double totalTarget = 0;
        double totalSaved = 0;
        double monthlyAllocation = 0;
        int completedGoals = 0;

        for (Map<String, Object> goal : goals) {
            totalTarget += (Double) goal.get("targetAmount");
            totalSaved += (Double) goal.get("savedAmount");
            monthlyAllocation += (Double) goal.get("monthlyContribution");

            Double progress = (Double) goal.get("progressPercentage");
            if (progress != null && progress >= 100) {
                completedGoals++;
            }
        }

        double completionRate = totalTarget > 0 ? (totalSaved / totalTarget) * 100 : 0;

        summary.put("totalTarget", totalTarget);
        summary.put("totalSaved", totalSaved);
        summary.put("monthlyAllocation", monthlyAllocation);
        summary.put("completionRate", Math.round(completionRate));
        summary.put("totalGoals", goals.size());
        summary.put("completedGoals", completedGoals);

        return summary;
    }

    /**
     * Generate goal suggestions
     */
    private List<String> generateGoalSuggestions(List<Map<String, Object>> goals) {
        List<String> suggestions = new ArrayList<>();

        if (goals.isEmpty()) {
            suggestions.add("Start by creating an emergency fund - aim for 3-6 months of expenses");
            suggestions.add("Consider setting up a vacation fund for motivation");
            return suggestions;
        }

        // Check for emergency fund
        boolean hasEmergencyFund = false;
        for (Map<String, Object> goal : goals) {
            if ("EMERGENCY".equals(goal.get("category"))) {
                hasEmergencyFund = true;
                Double progress = (Double) goal.get("progressPercentage");
                if (progress != null && progress < 50) {
                    suggestions.add("Your emergency fund is at " + progress.intValue() +
                            "%. Consider increasing monthly contributions.");
                }
            }
        }

        if (!hasEmergencyFund) {
            suggestions.add("Consider adding an emergency fund for financial security");
        }

        // Calculate total monthly allocation
        double totalMonthly = goals.stream()
                .mapToDouble(g -> (Double) g.get("monthlyContribution"))
                .sum();

        if (totalMonthly > 0) {
            suggestions.add("You're allocating ₹" + totalMonthly + " monthly towards your goals");
        }

        return suggestions;

    }

    // Add this method to your BudgetController class
    @ModelAttribute("savingGoal")
    public Map<String, Object> savingGoal() {
        Map<String, Object> goal = new HashMap<>();
        goal.put("name", "");
        goal.put("category", "");
        goal.put("targetAmount", 0.0);
        goal.put("savedAmount", 0.0);
        goal.put("targetDate", "");
        goal.put("priority", "MEDIUM");
        goal.put("monthlyContribution", 0.0);
        goal.put("description", "");
        return goal;
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
}

