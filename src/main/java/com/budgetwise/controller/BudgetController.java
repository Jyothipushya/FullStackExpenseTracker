
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
        try {
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            User user = userService.findByEmail(email);

            if (user == null) {
                return "redirect:/login";
            }

            // Get budget progress data
            List<Map<String, Object>> budgetProgress = budgetService.getAllBudgetProgress(user);
            Map<String, Object> budgetSummary = budgetService.getBudgetSummary(user);
            List<String> aiSuggestions = budgetService.getAISuggestions(user);

            // Add to model
            model.addAttribute("userName", user.getFullName());
            model.addAttribute("budgetProgress", budgetProgress);
            model.addAttribute("budgetSummary", budgetSummary);
            model.addAttribute("aiSuggestions", aiSuggestions);
            model.addAttribute("hasBudgets", !budgetProgress.isEmpty());

            System.out.println("✅ Budget Dashboard loaded for: " + user.getEmail());
            System.out.println("📊 Budgets found: " + budgetProgress.size());

        } catch (Exception e) {
            System.out.println("❌ Error loading budget dashboard: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to load budget data");
        }

        return "budgets";
    }

    /**
     * Show budget setup page
     */
    @GetMapping("/setup")
    public String showBudgetSetupPage(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            User user = userService.findByEmail(email);

            if (user == null) {
                return "redirect:/login";
            }

            // Get existing budgets for pre-filling form
            List<Map<String, Object>> existingBudgets = budgetService.getAllBudgetProgress(user);

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

        } catch (Exception e) {
            model.addAttribute("error", "Failed to load setup page");
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
}