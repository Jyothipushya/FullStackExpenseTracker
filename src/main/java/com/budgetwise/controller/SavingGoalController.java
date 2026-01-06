/*package com.budgetwise.controller;

import com.budgetwise.entity.SavingGoal;
import com.budgetwise.service.SavingGoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/goals")
public class SavingGoalController {

    @Autowired
    private SavingGoalService savingGoalService;

    // Show Combined Budget & Goals Page
    @GetMapping("/budget-and-goals")
    public String showBudgetAndGoals(Model model) {
        Long userId = 1L;
        List<SavingGoal> goals = savingGoalService.getActiveGoalsByUserId(userId);

        Map<String, Object> summary = new HashMap<>();
        double totalTarget = 0;
        double totalSaved = 0;
        double monthlyAllocation = 0;

        for (SavingGoal goal : goals) {
            totalTarget += goal.getTargetAmount() != null ? goal.getTargetAmount() : 0;
            totalSaved += goal.getSavedAmount() != null ? goal.getSavedAmount() : 0;
            if (goal.getMonthlyContribution() != null) {
                monthlyAllocation += goal.getMonthlyContribution();
            }
        }

        summary.put("totalTarget", totalTarget);
        summary.put("totalSaved", totalSaved);
        summary.put("monthlyAllocation", monthlyAllocation);
        summary.put("totalGoals", goals.size());
        summary.put("completedGoals", savingGoalService.getCompletedGoalsByUserId(userId).size());

        model.addAttribute("goalsSummary", summary);
        model.addAttribute("goals", goals);
        model.addAttribute("userName", "Jyothi Pushya");

        return "budget-and-goals";
    }

    // Show Goals Dashboard
    @GetMapping("/dashboard")
    public String showGoalsDashboard(Model model) {
        Long userId = 1L;

        List<SavingGoal> activeGoals = savingGoalService.getActiveGoalsByUserId(userId);
        List<SavingGoal> completedGoals = savingGoalService.getCompletedGoalsByUserId(userId);

        model.addAttribute("activeGoals", activeGoals);
        model.addAttribute("completedGoals", completedGoals);
        model.addAttribute("hasGoals", !activeGoals.isEmpty() || !completedGoals.isEmpty());

        Map<String, Object> summary = new HashMap<>();
        double totalTarget = 0;
        double totalSaved = 0;
        double monthlyAllocation = 0;

        for (SavingGoal goal : activeGoals) {
            totalTarget += goal.getTargetAmount() != null ? goal.getTargetAmount() : 0;
            totalSaved += goal.getSavedAmount() != null ? goal.getSavedAmount() : 0;
            if (goal.getMonthlyContribution() != null) {
                monthlyAllocation += goal.getMonthlyContribution();
            }
        }

        summary.put("totalTarget", totalTarget);
        summary.put("totalSaved", totalSaved);
        summary.put("monthlyAllocation", monthlyAllocation);
        summary.put("totalGoals", activeGoals.size() + completedGoals.size());
        summary.put("completedGoals", completedGoals.size());

        model.addAttribute("goalsSummary", summary);
        model.addAttribute("userName", "Jyothi Pushya");

        return "saving-goals";
    }

    // Show Create Goal Form
    @GetMapping("/create")
    public String showCreateGoalForm(Model model) {
        SavingGoal goal = new SavingGoal();
        goal.setTargetDate(LocalDate.now().plusYears(1));

        model.addAttribute("savingGoal", goal);

        List<String> categories = new ArrayList<>();
        categories.add("VACATION");
        categories.add("HOUSE");
        categories.add("CAR");
        categories.add("EMERGENCY");
        categories.add("EDUCATION");
        categories.add("GADGET");
        categories.add("OTHER");
        model.addAttribute("categories", categories);

        return "goal-setup";
    }

    // Save Goal - SIMPLE VERSION THAT WILL WORK
    @PostMapping("/save")
    public String saveGoal(@ModelAttribute SavingGoal savingGoal,
                           RedirectAttributes redirectAttributes,
                           HttpServletRequest request) {
        try {
            System.out.println("DEBUG: Starting to save goal: " + savingGoal.getName());

            // Debug: Check what date was received
            String rawDate = request.getParameter("targetDate");
            System.out.println("DEBUG: Raw date from form: " + rawDate);
            System.out.println("DEBUG: Parsed targetDate in savingGoal: " + savingGoal.getTargetDate());

            // Set default values
            savingGoal.setUserId(1L);
            savingGoal.setIcon(getIconForCategory(savingGoal.getCategory()));
            savingGoal.setStartDate(LocalDate.now());

            if (savingGoal.getSavedAmount() == null) {
                savingGoal.setSavedAmount(0.0);
            }

            if (savingGoal.getPriority() == null) {
                savingGoal.setPriority("MEDIUM");
            }

            if (savingGoal.getIsCompleted() == null) {
                savingGoal.setIsCompleted(false);
            }

            // Log all values before saving
            System.out.println("DEBUG: Final goal object before save:");
            System.out.println("  Name: " + savingGoal.getName());
            System.out.println("  Category: " + savingGoal.getCategory());
            System.out.println("  Target Amount: " + savingGoal.getTargetAmount());
            System.out.println("  Target Date: " + savingGoal.getTargetDate());
            System.out.println("  Priority: " + savingGoal.getPriority());

            // Save goal
            SavingGoal savedGoal = savingGoalService.saveGoal(savingGoal);
            System.out.println("DEBUG: Goal saved with ID: " + savedGoal.getId());

            redirectAttributes.addFlashAttribute("success",
                    "Goal '" + savingGoal.getName() + "' created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Error creating goal: " + e.getMessage());
        }

        return "redirect:/goals/dashboard";
    }

    // Add Funds to Goal
    @PostMapping("/add-funds")
    public String addFunds(@RequestParam Long goalId,
                           @RequestParam Double amount,
                           RedirectAttributes redirectAttributes) {
        try {
            savingGoalService.addFunds(goalId, amount);
            redirectAttributes.addFlashAttribute("success",
                    "Added ₹" + amount + " successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error: " + e.getMessage());
        }

        return "redirect:/goals/dashboard";
    }

    // Delete Goal
    @GetMapping("/delete/{id}")
    public String deleteGoal(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            savingGoalService.deleteGoal(id);
            redirectAttributes.addFlashAttribute("success", "Goal deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        return "redirect:/goals/dashboard";
    }

    // Helper method to get icon
    private String getIconForCategory(String category) {
        if (category == null) return "fa-bullseye";

        switch (category.toUpperCase()) {
            case "VACATION":
                return "fa-plane";
            case "HOUSE":
                return "fa-home";
            case "CAR":
                return "fa-car";
            case "EMERGENCY":
                return "fa-shield-alt";
            case "EDUCATION":
                return "fa-graduation-cap";
            case "GADGET":
                return "fa-laptop";
            default:
                return "fa-bullseye";
        }
    }
}*/
/*package com.budgetwise.controller;

import com.budgetwise.entity.SavingGoal;
import com.budgetwise.service.SavingGoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/goals")
public class SavingGoalController {

    @Autowired
    private SavingGoalService savingGoalService;

    // Show Create Goal Form
    @GetMapping("/create")
    public String showCreateGoalForm(Model model) {
        SavingGoal goal = new SavingGoal();
        goal.setTargetDate(LocalDate.now().plusYears(1));

        model.addAttribute("savingGoal", goal);

        List<String> categories = new ArrayList<>();
        categories.add("VACATION");
        categories.add("HOUSE");
        categories.add("CAR");
        categories.add("EMERGENCY");
        categories.add("EDUCATION");
        categories.add("GADGET");
        categories.add("OTHER");
        model.addAttribute("categories", categories);

        return "goal-setup";
    }

    // Save Goal - WORKING VERSION
    @PostMapping("/save")
    public String saveGoal(
            @RequestParam("name") String name,
            @RequestParam("category") String category,
            @RequestParam("targetAmount") Double targetAmount,
            @RequestParam("targetDate") String targetDateStr, // Get as String
            @RequestParam("priority") String priority,
            @RequestParam(value = "savedAmount", required = false) Double savedAmount,
            @RequestParam(value = "monthlyContribution", required = false) Double monthlyContribution,
            @RequestParam(value = "description", required = false) String description,
            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("=== DEBUG START ===");
            System.out.println("Name: " + name);
            System.out.println("Category: " + category);
            System.out.println("Target Amount: " + targetAmount);
            System.out.println("Target Date (raw): " + targetDateStr);
            System.out.println("Priority: " + priority);
            System.out.println("Saved Amount: " + savedAmount);
            System.out.println("Monthly Contribution: " + monthlyContribution);

            // Parse the date manually
            LocalDate targetDate = LocalDate.parse(targetDateStr);
            System.out.println("Target Date (parsed): " + targetDate);

            // Create new SavingGoal
            SavingGoal savingGoal = new SavingGoal();
            savingGoal.setName(name);
            savingGoal.setCategory(category);
            savingGoal.setTargetAmount(targetAmount);
            savingGoal.setTargetDate(targetDate);
            savingGoal.setPriority(priority);
            savingGoal.setSavedAmount(savedAmount != null ? savedAmount : 0.0);
            savingGoal.setMonthlyContribution(monthlyContribution);
            savingGoal.setUserId(1L);
            savingGoal.setIcon(getIconForCategory(category));
            savingGoal.setStartDate(LocalDate.now());
            savingGoal.setIsCompleted(false);

            System.out.println("=== BEFORE SAVE ===");
            System.out.println("Goal object created: " + savingGoal.getName());

            // Save goal
            SavingGoal savedGoal = savingGoalService.saveGoal(savingGoal);

            System.out.println("=== AFTER SAVE ===");
            System.out.println("Goal saved with ID: " + savedGoal.getId());

            redirectAttributes.addFlashAttribute("success",
                    "Goal '" + savingGoal.getName() + "' created successfully!");

        } catch (Exception e) {
            System.err.println("=== ERROR ===");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Error creating goal: " + e.getMessage());
        }

        return "redirect:/goals/dashboard";
    }

    // Dashboard - Simplified
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        Long userId = 1L;
        List<SavingGoal> goals = savingGoalService.getActiveGoalsByUserId(userId);
        model.addAttribute("goals", goals);
        model.addAttribute("userName", "Jyothi Pushya");
        return "saving-goals";
    }

    // Helper method to get icon
    private String getIconForCategory(String category) {
        if (category == null) return "fa-bullseye";

        switch (category.toUpperCase()) {
            case "VACATION": return "fa-plane";
            case "HOUSE": return "fa-home";
            case "CAR": return "fa-car";
            case "EMERGENCY": return "fa-shield-alt";
            case "EDUCATION": return "fa-graduation-cap";
            case "GADGET": return "fa-laptop";
            default: return "fa-bullseye";
        }
    }

    // Add Funds
    @PostMapping("/add-funds")
    public String addFunds(@RequestParam Long goalId,
                           @RequestParam Double amount,
                           RedirectAttributes redirectAttributes) {
        try {
            savingGoalService.addFunds(goalId, amount);
            redirectAttributes.addFlashAttribute("success", "Added ₹" + amount + " successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/goals/dashboard";
    }

    // Delete Goal
    @GetMapping("/delete/{id}")
    public String deleteGoal(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            savingGoalService.deleteGoal(id);
            redirectAttributes.addFlashAttribute("success", "Goal deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/goals/dashboard";
    }
}*/

/*package com.budgetwise.controller;

import com.budgetwise.entity.SavingGoal;
import com.budgetwise.service.SavingGoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/goals")
public class SavingGoalController {

    @Autowired
    private SavingGoalService savingGoalService;

    // Show Create Goal Form
    @GetMapping("/create")
    public String showCreateGoalForm(Model model) {
        SavingGoal goal = new SavingGoal();
        goal.setTargetDate(LocalDate.now().plusYears(1));

        model.addAttribute("savingGoal", goal);

        List<String> categories = new ArrayList<>();
        categories.add("VACATION");
        categories.add("HOUSE");
        categories.add("CAR");
        categories.add("EMERGENCY");
        categories.add("EDUCATION");
        categories.add("GADGET");
        categories.add("OTHER");
        model.addAttribute("categories", categories);

        return "goal-setup";
    }

    // Save Goal - WORKING VERSION
    @PostMapping("/save")
    public String saveGoal(
            @RequestParam("name") String name,
            @RequestParam("category") String category,
            @RequestParam("targetAmount") Double targetAmount,
            @RequestParam("targetDate") String targetDateStr,
            @RequestParam("priority") String priority,
            @RequestParam(value = "savedAmount", required = false) Double savedAmount,
            @RequestParam(value = "monthlyContribution", required = false) Double monthlyContribution,
            @RequestParam(value = "description", required = false) String description,
            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("=== DEBUG: Received Form Data ===");
            System.out.println("Name: " + name);
            System.out.println("Category: " + category);
            System.out.println("Target Amount: " + targetAmount);
            System.out.println("Target Date (raw): " + targetDateStr);
            System.out.println("Priority: " + priority);
            System.out.println("Saved Amount: " + savedAmount);
            System.out.println("Monthly Contribution: " + monthlyContribution);

            // Parse the date manually
            LocalDate targetDate = LocalDate.parse(targetDateStr);
            System.out.println("Target Date (parsed): " + targetDate);

            // Create new SavingGoal
            SavingGoal savingGoal = new SavingGoal();
            savingGoal.setName(name);
            savingGoal.setCategory(category);
            savingGoal.setTargetAmount(targetAmount);
            savingGoal.setTargetDate(targetDate);
            savingGoal.setPriority(priority);
            savingGoal.setSavedAmount(savedAmount != null ? savedAmount : 0.0);
            savingGoal.setMonthlyContribution(monthlyContribution);
            savingGoal.setUserId(1L);
            savingGoal.setIcon(getIconForCategory(category));
            savingGoal.setStartDate(LocalDate.now());
            savingGoal.setIsCompleted(false);

            // Save goal
            SavingGoal savedGoal = savingGoalService.saveGoal(savingGoal);
            System.out.println("=== SUCCESS: Goal saved with ID: " + savedGoal.getId());

            redirectAttributes.addFlashAttribute("success",
                    "Goal '" + savingGoal.getName() + "' created successfully!");

        } catch (Exception e) {
            System.err.println("=== ERROR ===");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Error creating goal: " + e.getMessage());
        }

        return "redirect:/goals/dashboard";
    }

    // Dashboard
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        Long userId = 1L;
        List<SavingGoal> goals = savingGoalService.getActiveGoalsByUserId(userId);
        model.addAttribute("goals", goals);
        model.addAttribute("userName", "Jyothi Pushya");
        return "saving-goals";
    }

    // Helper method to get icon
    private String getIconForCategory(String category) {
        if (category == null) return "fa-bullseye";

        switch (category.toUpperCase()) {
            case "VACATION": return "fa-plane";
            case "HOUSE": return "fa-home";
            case "CAR": return "fa-car";
            case "EMERGENCY": return "fa-shield-alt";
            case "EDUCATION": return "fa-graduation-cap";
            case "GADGET": return "fa-laptop";
            default: return "fa-bullseye";
        }
    }

    // Add Funds
    @PostMapping("/add-funds")
    public String addFunds(@RequestParam Long goalId,
                           @RequestParam Double amount,
                           RedirectAttributes redirectAttributes) {
        try {
            savingGoalService.addFunds(goalId, amount);
            redirectAttributes.addFlashAttribute("success", "Added ₹" + amount + " successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/goals/dashboard";
    }

    // Delete Goal
    @GetMapping("/delete/{id}")
    public String deleteGoal(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            savingGoalService.deleteGoal(id);
            redirectAttributes.addFlashAttribute("success", "Goal deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/goals/dashboard";
    }
}*/
/*package com.budgetwise.controller;

import com.budgetwise.entity.SavingGoal;
import com.budgetwise.service.SavingGoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/goals")
public class SavingGoalController {

    @Autowired
    private SavingGoalService savingGoalService;

    // Show Goals Dashboard
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        Long userId = 1L;

        // Get goals from database
        List<SavingGoal> activeGoals = savingGoalService.getActiveGoalsByUserId(userId);
        List<SavingGoal> completedGoals = savingGoalService.getCompletedGoalsByUserId(userId);

        // Calculate summary
        Map<String, Object> summary = new HashMap<>();
        double totalTarget = 0;
        double totalSaved = 0;
        double monthlyAllocation = 0;

        for (SavingGoal goal : activeGoals) {
            totalTarget += goal.getTargetAmount() != null ? goal.getTargetAmount() : 0;
            totalSaved += goal.getSavedAmount() != null ? goal.getSavedAmount() : 0;
            if (goal.getMonthlyContribution() != null) {
                monthlyAllocation += goal.getMonthlyContribution();
            }
        }

        summary.put("totalTarget", totalTarget);
        summary.put("totalSaved", totalSaved);
        summary.put("monthlyAllocation", monthlyAllocation);
        summary.put("totalGoals", activeGoals.size() + completedGoals.size());
        summary.put("completedGoals", completedGoals.size());

        // Add data to model
        model.addAttribute("activeGoals", activeGoals);
        model.addAttribute("completedGoals", completedGoals);
        model.addAttribute("goalsSummary", summary);
        model.addAttribute("userName", "Jyothi Pushya");
        model.addAttribute("hasGoals", !activeGoals.isEmpty() || !completedGoals.isEmpty());

        return "saving-goals";
    }

    // Show Create Goal Form
    @GetMapping("/create")
    public String showCreateGoalForm(Model model) {
        // Create empty goal for form
        SavingGoal goal = new SavingGoal();
        goal.setTargetDate(LocalDate.now().plusYears(1));

        // Add categories for dropdown
        List<String> categories = Arrays.asList(
                "VACATION", "HOUSE", "CAR", "EMERGENCY",
                "EDUCATION", "GADGET", "OTHER"
        );

        model.addAttribute("savingGoal", goal);
        model.addAttribute("categories", categories);

        return "goal-setup";
    }

    // Save Goal - FIXED VERSION
    @PostMapping("/save")
    public String saveGoal(
            @RequestParam("name") String name,
            @RequestParam("category") String category,
            @RequestParam("targetAmount") Double targetAmount,
            @RequestParam("targetDate") String targetDateStr,
            @RequestParam("priority") String priority,
            @RequestParam(value = "savedAmount", required = false) Double savedAmount,
            @RequestParam(value = "monthlyContribution", required = false) Double monthlyContribution,
            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("DEBUG: Saving goal - " + name);
            System.out.println("DEBUG: Date received - " + targetDateStr);

            // Parse date manually
            LocalDate targetDate = LocalDate.parse(targetDateStr);

            // Create new goal
            SavingGoal savingGoal = new SavingGoal();
            savingGoal.setName(name);
            savingGoal.setCategory(category);
            savingGoal.setTargetAmount(targetAmount);
            savingGoal.setTargetDate(targetDate);
            savingGoal.setPriority(priority);
            savingGoal.setSavedAmount(savedAmount != null ? savedAmount : 0.0);
            savingGoal.setMonthlyContribution(monthlyContribution);
            savingGoal.setUserId(1L);
            savingGoal.setIcon(getIconForCategory(category));
            savingGoal.setStartDate(LocalDate.now());
            savingGoal.setIsCompleted(false);

            // Save to database
            savingGoalService.saveGoal(savingGoal);

            System.out.println("DEBUG: Goal saved successfully!");

            redirectAttributes.addFlashAttribute("success",
                    "Goal '" + name + "' created successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Error: " + e.getMessage());
        }

        return "redirect:/goals/dashboard";
    }

    // Add Funds to Goal
    @PostMapping("/add-funds")
    public String addFunds(@RequestParam Long goalId,
                           @RequestParam Double amount,
                           RedirectAttributes redirectAttributes) {
        try {
            savingGoalService.addFunds(goalId, amount);
            redirectAttributes.addFlashAttribute("success",
                    "Added ₹" + amount + " successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error: " + e.getMessage());
        }
        return "redirect:/goals/dashboard";
    }

    // Delete Goal
    @GetMapping("/delete/{id}")
    public String deleteGoal(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            savingGoalService.deleteGoal(id);
            redirectAttributes.addFlashAttribute("success", "Goal deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error: " + e.getMessage());
        }
        return "redirect:/goals/dashboard";
    }

    // Helper method to get icon
    private String getIconForCategory(String category) {
        if (category == null) return "fa-bullseye";

        switch (category.toUpperCase()) {
            case "VACATION": return "fa-plane";
            case "HOUSE": return "fa-home";
            case "CAR": return "fa-car";
            case "EMERGENCY": return "fa-shield-alt";
            case "EDUCATION": return "fa-graduation-cap";
            case "GADGET": return "fa-laptop";
            default: return "fa-bullseye";
        }
    }
}*/
package com.budgetwise.controller;

import com.budgetwise.entity.SavingGoal;
import com.budgetwise.repository.SavingGoalRepository;
import com.budgetwise.service.SavingGoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/goals")
public class SavingGoalController {

    @Autowired
    private SavingGoalService savingGoalService;

    @Autowired
    private SavingGoalRepository savingGoalRepository;  // Add this

    // Show Goals Dashboard
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        Long userId = 1L;

        // DEBUG: Print database status
        System.out.println("=== DASHBOARD DEBUG ===");
        List<SavingGoal> allGoals = savingGoalRepository.findByUserId(userId);
        System.out.println("Total goals in DB: " + allGoals.size());
        for (SavingGoal goal : allGoals) {
            System.out.println("Goal: " + goal.getName() +
                    ", isCompleted: " + goal.getIsCompleted() +
                    ", userId: " + goal.getUserId());
        }

        // Get goals from database
        List<SavingGoal> activeGoals = savingGoalService.getActiveGoalsByUserId(userId);
        List<SavingGoal> completedGoals = savingGoalService.getCompletedGoalsByUserId(userId);

        System.out.println("Active goals count: " + activeGoals.size());
        System.out.println("Completed goals count: " + completedGoals.size());
        System.out.println("=== END DEBUG ===");

        // Calculate summary
        Map<String, Object> summary = new HashMap<>();
        double totalTarget = 0;
        double totalSaved = 0;
        double monthlyAllocation = 0;

        for (SavingGoal goal : activeGoals) {
            totalTarget += goal.getTargetAmount() != null ? goal.getTargetAmount() : 0;
            totalSaved += goal.getSavedAmount() != null ? goal.getSavedAmount() : 0;
            if (goal.getMonthlyContribution() != null) {
                monthlyAllocation += goal.getMonthlyContribution();
            }
        }

        summary.put("totalTarget", totalTarget);
        summary.put("totalSaved", totalSaved);
        summary.put("monthlyAllocation", monthlyAllocation);
        summary.put("totalGoals", activeGoals.size() + completedGoals.size());
        summary.put("completedGoals", completedGoals.size());

        // Add data to model
        model.addAttribute("activeGoals", activeGoals);
        model.addAttribute("completedGoals", completedGoals);
        model.addAttribute("goalsSummary", summary);
        model.addAttribute("userName", "Jyothi Pushya");
        model.addAttribute("hasGoals", !activeGoals.isEmpty() || !completedGoals.isEmpty());

        return "saving-goals";
    }

    // Show Create Goal Form
    @GetMapping("/create")
    public String showCreateGoalForm(Model model) {
        // Create empty goal for form
        SavingGoal goal = new SavingGoal();
        goal.setTargetDate(LocalDate.now().plusYears(1));

        // Add categories for dropdown
        List<String> categories = Arrays.asList(
                "VACATION", "HOUSE", "CAR", "EMERGENCY",
                "EDUCATION", "GADGET", "OTHER"
        );

        model.addAttribute("savingGoal", goal);
        model.addAttribute("categories", categories);

        return "goal-setup";
    }

    // Save Goal - FIXED VERSION
    @PostMapping("/save")
    public String saveGoal(
            @RequestParam("name") String name,
            @RequestParam("category") String category,
            @RequestParam("targetAmount") Double targetAmount,
            @RequestParam("targetDate") String targetDateStr,
            @RequestParam("priority") String priority,
            @RequestParam(value = "savedAmount", required = false) Double savedAmount,
            @RequestParam(value = "monthlyContribution", required = false) Double monthlyContribution,
            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("=== SAVING NEW GOAL ===");
            System.out.println("Name: " + name);
            System.out.println("Category: " + category);
            System.out.println("Target Amount: " + targetAmount);
            System.out.println("Target Date (raw): " + targetDateStr);
            System.out.println("Priority: " + priority);
            System.out.println("Saved Amount: " + savedAmount);
            System.out.println("Monthly Contribution: " + monthlyContribution);

            // Parse date manually
            LocalDate targetDate = LocalDate.parse(targetDateStr);
            System.out.println("Target Date (parsed): " + targetDate);

            // Create new goal
            SavingGoal savingGoal = new SavingGoal();
            savingGoal.setName(name);
            savingGoal.setCategory(category);
            savingGoal.setTargetAmount(targetAmount);
            savingGoal.setTargetDate(targetDate);
            savingGoal.setPriority(priority);
            savingGoal.setSavedAmount(savedAmount != null ? savedAmount : 0.0);
            savingGoal.setMonthlyContribution(monthlyContribution);
            savingGoal.setUserId(1L);
            savingGoal.setIcon(getIconForCategory(category));
            savingGoal.setStartDate(LocalDate.now());

            // ⭐ CRITICAL: Set isCompleted to false
            savingGoal.setIsCompleted(false);

            System.out.println("Goal created. isCompleted: " + savingGoal.getIsCompleted());

            // Save to database
            SavingGoal savedGoal = savingGoalService.saveGoal(savingGoal);
            System.out.println("Goal saved with ID: " + savedGoal.getId());
            System.out.println("Saved goal isCompleted: " + savedGoal.getIsCompleted());
            System.out.println("=== GOAL SAVED SUCCESSFULLY ===");

            redirectAttributes.addFlashAttribute("success",
                    "Goal '" + name + "' created successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Error: " + e.getMessage());
        }

        return "redirect:/goals/dashboard";
    }

    // ⭐ ADD THIS DEBUG ENDPOINT
    @GetMapping("/debug-db")
    @ResponseBody
    public String debugDatabase() {
        Long userId = 1L;

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Database Debug</title></head><body>");
        sb.append("<h1>Database Debug - User ID: ").append(userId).append("</h1>");

        try {
            // 1. Check all goals in repository
            List<SavingGoal> allGoals = savingGoalRepository.findByUserId(userId);
            sb.append("<h2>All Goals in Database (").append(allGoals.size()).append(")</h2>");

            if (allGoals.isEmpty()) {
                sb.append("<p style='color: red;'>NO GOALS FOUND IN DATABASE!</p>");
            } else {
                for (SavingGoal goal : allGoals) {
                    sb.append("<div style='border:1px solid #ccc; padding:10px; margin:10px;'>")
                            .append("<strong>ID:</strong> ").append(goal.getId()).append("<br>")
                            .append("<strong>Name:</strong> ").append(goal.getName()).append("<br>")
                            .append("<strong>User ID:</strong> ").append(goal.getUserId()).append("<br>")
                            .append("<strong>isCompleted:</strong> ").append(goal.getIsCompleted()).append("<br>")
                            .append("<strong>Target Amount:</strong> ").append(goal.getTargetAmount()).append("<br>")
                            .append("<strong>Saved Amount:</strong> ").append(goal.getSavedAmount()).append("<br>")
                            .append("<strong>Target Date:</strong> ").append(goal.getTargetDate()).append("<br>")
                            .append("</div>");
                }
            }

            // 2. Check repository methods
            sb.append("<h2>Repository Queries</h2>");

            List<SavingGoal> activeRepo = savingGoalRepository.findByUserIdAndIsCompletedFalse(userId);
            sb.append("<p>Active Goals (isCompleted=false): ").append(activeRepo.size()).append("</p>");

            List<SavingGoal> completedRepo = savingGoalRepository.findByUserIdAndIsCompletedTrue(userId);
            sb.append("<p>Completed Goals (isCompleted=true): ").append(completedRepo.size()).append("</p>");

            // 3. Check service methods
            sb.append("<h2>Service Methods</h2>");

            List<SavingGoal> activeService = savingGoalService.getActiveGoalsByUserId(userId);
            sb.append("<p>Active Goals (Service): ").append(activeService.size()).append("</p>");

            List<SavingGoal> completedService = savingGoalService.getCompletedGoalsByUserId(userId);
            sb.append("<p>Completed Goals (Service): ").append(completedService.size()).append("</p>");

            // 4. Last 5 transactions
            sb.append("<h2>Last 5 Console Messages</h2>")
                    .append("<p><em>Check your console/terminal for these messages</em></p>");

        } catch (Exception e) {
            sb.append("<div style='color: red; padding: 20px; background: #fee;'>")
                    .append("<h3>ERROR:</h3>")
                    .append("<pre>").append(e.getMessage()).append("</pre>")
                    .append("</div>");
            e.printStackTrace();
        }

        sb.append("</body></html>");
        return sb.toString();
    }

    // Add Funds to Goal
    @PostMapping("/add-funds")
    public String addFunds(@RequestParam Long goalId,
                           @RequestParam Double amount,
                           RedirectAttributes redirectAttributes) {
        try {
            savingGoalService.addFunds(goalId, amount);
            redirectAttributes.addFlashAttribute("success",
                    "Added ₹" + amount + " successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error: " + e.getMessage());
        }
        return "redirect:/goals/dashboard";
    }

    // Delete Goal
    @GetMapping("/delete/{id}")
    public String deleteGoal(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            savingGoalService.deleteGoal(id);
            redirectAttributes.addFlashAttribute("success", "Goal deleted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error: " + e.getMessage());
        }
        return "redirect:/goals/dashboard";
    }

    // Helper method to get icon
    private String getIconForCategory(String category) {
        if (category == null) return "fa-bullseye";

        switch (category.toUpperCase()) {
            case "VACATION": return "fa-plane";
            case "HOUSE": return "fa-home";
            case "CAR": return "fa-car";
            case "EMERGENCY": return "fa-shield-alt";
            case "EDUCATION": return "fa-graduation-cap";
            case "GADGET": return "fa-laptop";
            default: return "fa-bullseye";
        }
    }
}