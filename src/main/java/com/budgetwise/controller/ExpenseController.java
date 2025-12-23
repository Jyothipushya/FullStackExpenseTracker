package com.budgetwise.controller;

import com.budgetwise.entity.Expense;
import com.budgetwise.entity.User;
import com.budgetwise.service.ExpenseService;
import com.budgetwise.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String showExpenses(@AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User user = userService.findByEmail(userDetails.getUsername());

        // Get current month expenses
        List<Expense> expenses = expenseService.getCurrentMonthExpenses(user);

        // Calculate totals
        BigDecimal totalExpenses = expenseService.getCurrentMonthTotalExpenses(user);
        BigDecimal totalIncome = expenseService.getCurrentMonthTotalIncome(user);
        BigDecimal balance = totalIncome.subtract(totalExpenses);

        // Get category totals for chart
        Map<String, BigDecimal> categoryTotals = expenseService.getCategoryTotals(user);

        model.addAttribute("expenses", expenses);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("balance", balance);
        model.addAttribute("categoryTotals", categoryTotals);
        model.addAttribute("user", user);
        model.addAttribute("today", LocalDate.now());

        return "expenses";
    }

    @PostMapping("/add")
    public String addExpense(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam String description,
                             @RequestParam BigDecimal amount,
                             @RequestParam String category,
                             @RequestParam String date,
                             @RequestParam(defaultValue = "false") boolean isIncome,
                             RedirectAttributes redirectAttributes) {

        System.out.println("=== ADDING EXPENSE ===");
        System.out.println("Description: " + description);
        System.out.println("Amount: " + amount);
        System.out.println("Category: " + category);
        System.out.println("Date: " + date);
        System.out.println("Is Income: " + isIncome);

        try {
            User user = userService.findByEmail(userDetails.getUsername());
            System.out.println("User: " + user.getEmail());

            Expense expense = new Expense();
            expense.setDescription(description);
            expense.setAmount(amount);
            expense.setCategory(category);
            expense.setDate(LocalDate.parse(date));
            expense.setIncome(isIncome);
            expense.setUser(user);

            Expense savedExpense = expenseService.addExpense(expense);
            System.out.println("✅ Expense saved with ID: " + savedExpense.getId());

            redirectAttributes.addFlashAttribute("success",
                    (isIncome ? "Income" : "Expense") + " added successfully!");

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to add: " + e.getMessage());
        }

        return "redirect:/expenses";
    }

    @PostMapping("/quick-add")
    public String quickAddExpense(@AuthenticationPrincipal UserDetails userDetails,
                                  @RequestParam String description,
                                  @RequestParam BigDecimal amount,
                                  @RequestParam String category,
                                  @RequestParam(required = false) String date,
                                  RedirectAttributes redirectAttributes) {

        System.out.println("=== QUICK ADDING EXPENSE ===");
        System.out.println("Description: " + description);
        System.out.println("Amount: " + amount);
        System.out.println("Category: " + category);
        System.out.println("Date: " + date);

        try {
            User user = userService.findByEmail(userDetails.getUsername());

            Expense expense = new Expense();
            expense.setDescription(description);
            expense.setAmount(amount);
            expense.setCategory(category);

            if (date != null && !date.isEmpty()) {
                expense.setDate(LocalDate.parse(date));
            } else {
                expense.setDate(LocalDate.now());
            }

            expense.setIncome(false);
            expense.setUser(user);

            Expense savedExpense = expenseService.addExpense(expense);
            System.out.println("✅ Expense saved with ID: " + savedExpense.getId());

            redirectAttributes.addFlashAttribute("success", "Expense added successfully!");

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to add expense: " + e.getMessage());
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/delete/{id}")
    public String deleteExpense(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable Long id,
                                RedirectAttributes redirectAttributes) {

        System.out.println("=== DELETING EXPENSE ID: " + id + " ===");

        try {
            expenseService.deleteExpense(id);
            System.out.println("✅ Expense deleted successfully!");
            redirectAttributes.addFlashAttribute("success", "Transaction deleted successfully!");
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to delete transaction: " + e.getMessage());
        }

        return "redirect:/expenses";
    }

    @GetMapping("/api/chart-data")
    @ResponseBody
    public Map<String, BigDecimal> getChartData(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        return expenseService.getCategoryTotals(user);
    }

    @GetMapping("/api/stats")
    @ResponseBody
    public Map<String, BigDecimal> getStats(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());

        BigDecimal monthlyIncome = expenseService.getCurrentMonthTotalIncome(user);
        BigDecimal monthlyExpenses = expenseService.getCurrentMonthTotalExpenses(user);
        BigDecimal monthlySavings = monthlyIncome.subtract(monthlyExpenses);

        return Map.of(
                "monthlyIncome", monthlyIncome,
                "monthlyExpenses", monthlyExpenses,
                "monthlySavings", monthlySavings
        );
    }
}
/*package com.budgetwise.controller;

import com.budgetwise.entity.Expense;
import com.budgetwise.entity.User;
import com.budgetwise.service.ExpenseService;
import com.budgetwise.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private UserService userService;

    // Get recent transactions (last 10)
    @GetMapping("/recent")
    public List<Map<String, Object>> getRecentTransactions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);

        if (user == null) {
            return List.of();
        }

        List<Expense> expenses = expenseService.getUserExpenses(user);

        // Get last 10 expenses, sorted by date (newest first)
        return expenses.stream()
                .sorted((e1, e2) -> e2.getDate().compareTo(e1.getDate()))
                .limit(10)
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    // Add new expense
    @PostMapping("/add")
    public Map<String, Object> addExpense(@RequestBody Map<String, Object> expenseData) {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            User user = userService.findByEmail(email);

            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }

            Expense expense = new Expense();
            expense.setDescription((String) expenseData.get("description"));
            expense.setAmount(new BigDecimal(expenseData.get("amount").toString()));
            expense.setCategory((String) expenseData.get("category"));
            expense.setDate(LocalDate.parse((String) expenseData.get("date")));
            expense.setIncome((Boolean) expenseData.get("income"));
            expense.setUser(user);

            Expense savedExpense = expenseService.addExpense(expense);

            response.put("success", true);
            response.put("message", "Expense added successfully");
            response.put("expenseId", savedExpense.getId());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    // Helper method to convert Expense to Map
    private Map<String, Object> convertToMap(Expense expense) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", expense.getId());
        map.put("description", expense.getDescription());
        map.put("amount", expense.getAmount());
        map.put("category", expense.getCategory());
        map.put("date", expense.getDate().toString());
        map.put("income", expense.isIncome());
        map.put("createdAt", expense.getCreatedAt());
        return map;
    }
}*/