/*package com.budgetwise.controller;

import com.budgetwise.entity.User;
import com.budgetwise.service.ExpenseService;
import com.budgetwise.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Controller
 public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExpenseService expenseService;

    @GetMapping("/dashBoard")
    public String showDashBoard(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userService.findByEmail(userDetails.getUsername());

        // GET REAL DATA FROM EXPENSE SERVICE
        BigDecimal monthlyIncome = expenseService.getCurrentMonthTotalIncome(user);
        BigDecimal monthlyExpenses = expenseService.getCurrentMonthTotalExpenses(user);
        BigDecimal monthlySavings = monthlyIncome.subtract(monthlyExpenses);

        // Calculate budget utilization
        int budgetUtilization = 0;
        if (monthlyIncome.compareTo(BigDecimal.ZERO) > 0) {
            budgetUtilization = monthlyExpenses
                    .divide(monthlyIncome, 2, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100))
                    .intValue();
        }

        // Get category data for chart
        Map<String, BigDecimal> categoryTotals = expenseService.getCategoryTotals(user);

        model.addAttribute("user", user);
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userEmail", user.getEmail());
        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("monthlyIncome", monthlyIncome);
        model.addAttribute("monthlyExpenses", monthlyExpenses);
        model.addAttribute("monthlySavings", monthlySavings);
        model.addAttribute("budgetUtilization", budgetUtilization);
        model.addAttribute("categoryTotals", categoryTotals);

        return "dashboard";
    }
}*/


/*package com.budgetwise.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String showDashBoard(Model model) {
        System.out.println("🎯 DASHBOARD PAGE ACCESSED");

        // Add test data
        model.addAttribute("userName", "Test User");
        model.addAttribute("firstName", "Test");
        model.addAttribute("userEmail", "test@test.com");
        model.addAttribute("monthlyIncome", 5000.00);
        model.addAttribute("monthlyExpenses", 3200.00);
        model.addAttribute("monthlySavings", 1800.00);
        model.addAttribute("budgetUtilization", 64);

        System.out.println("✅ Rendering dashboard with test data");
        return "dashboard";
    }
}*/
package com.budgetwise.controller;

import com.budgetwise.entity.User;
import com.budgetwise.service.UserService;
import com.budgetwise.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.Map;

@Controller
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExpenseService expenseService;

    @GetMapping("/dashboard")
    public String showDashBoard(Model model) {
        System.out.println("=== DASHBOARD LOADING ===");

        // 1. Get currently logged-in user email from Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        System.out.println("🔐 Logged in as: " + userEmail);

        // 2. If not logged in, redirect to login
        if (userEmail.equals("anonymousUser")) {
            return "redirect:/login";
        }

        // 3. Fetch user from database using your existing UserService
        User currentUser = userService.findByEmail(userEmail);

        if (currentUser == null) {
            System.out.println("❌ User not found: " + userEmail);
            return "redirect:/logout";
        }

        System.out.println("✅ Welcome, " + currentUser.getFullName() + "!");

        // 4. ADD ACTUAL USER DATA (NOT HARDCODED)
        model.addAttribute("userName", currentUser.getFullName());    // Actual name
        model.addAttribute("firstName", currentUser.getFirstName());  // Actual first name
        model.addAttribute("userEmail", currentUser.getEmail());      // Actual email

        // 5. Get real financial data using your existing ExpenseService
        BigDecimal monthlyIncome = expenseService.getCurrentMonthTotalIncome(currentUser);
        BigDecimal monthlyExpenses = expenseService.getCurrentMonthTotalExpenses(currentUser);
        BigDecimal monthlySavings = monthlyIncome.subtract(monthlyExpenses);

        // Calculate budget utilization
        int budgetUtilization = 0;
        if (monthlyIncome.compareTo(BigDecimal.ZERO) > 0) {
            budgetUtilization = (int) ((monthlyExpenses.doubleValue() / monthlyIncome.doubleValue()) * 100);
        }

        // 6. ADD ACTUAL FINANCIAL DATA
        model.addAttribute("monthlyIncome", String.format("%.2f", monthlyIncome));
        model.addAttribute("monthlyExpenses", String.format("%.2f", monthlyExpenses));
        model.addAttribute("monthlySavings", String.format("%.2f", monthlySavings));
        model.addAttribute("budgetUtilization", budgetUtilization);

        // 7. Get category totals for chart
        Map<String, BigDecimal> categoryTotals = expenseService.getCategoryTotals(currentUser);
        model.addAttribute("categoryTotals", categoryTotals);

        System.out.println("📊 Your data loaded successfully!");
        return "dashboard";
    }
}