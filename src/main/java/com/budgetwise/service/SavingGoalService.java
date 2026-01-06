/*package com.budgetwise.service;

import com.budgetwise.entity.SavingGoal;
import com.budgetwise.repository.SavingGoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class SavingGoalService {

    @Autowired
    private SavingGoalRepository savingGoalRepository;

    // Save or update a goal
    public SavingGoal saveGoal(SavingGoal goal) {
        // Calculate monthly contribution if not set
        if (goal.getMonthlyContribution() == null || goal.getMonthlyContribution() == 0) {
            calculateMonthlyContribution(goal);
        }
        return savingGoalRepository.save(goal);
    }

    // Get active goals (not completed) for user
    public List<SavingGoal> getActiveGoalsByUserId(Long userId) {
        List<SavingGoal> allGoals = savingGoalRepository.findByUserId(userId);
        List<SavingGoal> activeGoals = new ArrayList<>();

        for (SavingGoal goal : allGoals) {
            if (goal.getIsCompleted() == null || !goal.getIsCompleted()) {
                activeGoals.add(goal);
            }
        }
        return activeGoals;
    }

    // Get completed goals for user
    public List<SavingGoal> getCompletedGoalsByUserId(Long userId) {
        List<SavingGoal> allGoals = savingGoalRepository.findByUserId(userId);
        List<SavingGoal> completedGoals = new ArrayList<>();

        for (SavingGoal goal : allGoals) {
            if (goal.getIsCompleted() != null && goal.getIsCompleted()) {
                completedGoals.add(goal);
            }
        }
        return completedGoals;
    }

    // Get all goals for user
    public List<SavingGoal> getAllGoalsByUserId(Long userId) {
        return savingGoalRepository.findByUserId(userId);
    }

    // Add funds to a goal
    public void addFunds(Long goalId, Double amount) {
        SavingGoal goal = savingGoalRepository.findById(goalId).orElse(null);
        if (goal != null) {
            goal.setSavedAmount(goal.getSavedAmount() + amount);

            // Check if goal is completed
            if (goal.getSavedAmount() >= goal.getTargetAmount()) {
                goal.setIsCompleted(true);
            }

            savingGoalRepository.save(goal);
        }
    }

    // Delete a goal
    public void deleteGoal(Long goalId) {
        savingGoalRepository.deleteById(goalId);
    }

    // Get goal by ID
    public SavingGoal getGoalById(Long goalId) {
        return savingGoalRepository.findById(goalId).orElse(null);
    }

    // Mark goal as completed
    public void markAsCompleted(Long goalId) {
        SavingGoal goal = savingGoalRepository.findById(goalId).orElse(null);
        if (goal != null) {
            goal.setIsCompleted(true);
            savingGoalRepository.save(goal);
        }
    }

    // Calculate months remaining
    public int getMonthsRemaining(SavingGoal goal) {
        if (goal.getTargetDate() == null || goal.getIsCompleted()) {
            return 0;
        }

        LocalDate now = LocalDate.now();
        if (goal.getTargetDate().isBefore(now)) {
            return 0;
        }

        long months = ChronoUnit.MONTHS.between(now, goal.getTargetDate());
        return Math.max(0, (int) months);
    }

    // Helper method to calculate monthly contribution
    private void calculateMonthlyContribution(SavingGoal goal) {
        if (goal.getTargetDate() != null) {
            int monthsRemaining = getMonthsRemaining(goal);
            if (monthsRemaining > 0) {
                double remainingAmount = goal.getTargetAmount() - goal.getSavedAmount();
                double monthly = remainingAmount / monthsRemaining;
                goal.setMonthlyContribution(monthly);
            }
        }
    }
}*/
/*package com.budgetwise.repository;

import com.budgetwise.entity.SavingGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingGoalRepository extends JpaRepository<SavingGoal, Long> {

    // Find all goals by user ID
    List<SavingGoal> findByUserId(Long userId);

    // Find active (not completed) goals by user ID
    List<SavingGoal> findByUserIdAndIsCompletedFalse(Long userId);

    // Find completed goals by user ID
    List<SavingGoal> findByUserIdAndIsCompletedTrue(Long userId);

    // Find goals by user ID and category
    List<SavingGoal> findByUserIdAndCategory(Long userId, String category);

    // Find goals by priority
    List<SavingGoal> findByUserIdAndPriority(Long userId, String priority);
}*/
package com.budgetwise.service;

import com.budgetwise.entity.SavingGoal;
import com.budgetwise.repository.SavingGoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class SavingGoalService {

    @Autowired
    private SavingGoalRepository savingGoalRepository;

    // Save or update a goal - FIXED VERSION
    public SavingGoal saveGoal(SavingGoal goal) {
        // Ensure isCompleted is not null
        if (goal.getIsCompleted() == null) {
            goal.setIsCompleted(false);
        }

        // Ensure savedAmount is not null
        if (goal.getSavedAmount() == null) {
            goal.setSavedAmount(0.0);
        }

        // Ensure priority is not null
        if (goal.getPriority() == null) {
            goal.setPriority("MEDIUM");
        }

        // Calculate monthly contribution if not set
        if (goal.getMonthlyContribution() == null) {
            calculateMonthlyContribution(goal);
        }

        return savingGoalRepository.save(goal);
    }

    // Get active goals (not completed) for user - FIXED
    public List<SavingGoal> getActiveGoalsByUserId(Long userId) {
        return savingGoalRepository.findByUserIdAndIsCompletedFalse(userId);
    }

    // Get completed goals for user - FIXED
    public List<SavingGoal> getCompletedGoalsByUserId(Long userId) {
        return savingGoalRepository.findByUserIdAndIsCompletedTrue(userId);
    }

    // Get all goals for user
    public List<SavingGoal> getAllGoalsByUserId(Long userId) {
        return savingGoalRepository.findByUserId(userId);
    }

    // Add funds to a goal
    public void addFunds(Long goalId, Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        SavingGoal goal = savingGoalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found with ID: " + goalId));

        Double currentSaved = goal.getSavedAmount() != null ? goal.getSavedAmount() : 0.0;
        Double newAmount = currentSaved + amount;
        goal.setSavedAmount(newAmount);

        // Check if goal is completed
        if (goal.getTargetAmount() != null && newAmount >= goal.getTargetAmount()) {
            goal.setIsCompleted(true);
        }

        savingGoalRepository.save(goal);
    }

    // Delete a goal
    public void deleteGoal(Long goalId) {
        if (!savingGoalRepository.existsById(goalId)) {
            throw new RuntimeException("Goal not found with ID: " + goalId);
        }
        savingGoalRepository.deleteById(goalId);
    }

    // Get goal by ID
    public SavingGoal getGoalById(Long goalId) {
        return savingGoalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found with ID: " + goalId));
    }

    // Mark goal as completed
    public void markAsCompleted(Long goalId) {
        SavingGoal goal = getGoalById(goalId);
        goal.setIsCompleted(true);
        savingGoalRepository.save(goal);
    }

    // Mark goal as active (not completed)
    public void markAsActive(Long goalId) {
        SavingGoal goal = getGoalById(goalId);
        goal.setIsCompleted(false);
        savingGoalRepository.save(goal);
    }

    // Calculate months remaining
    public int getMonthsRemaining(SavingGoal goal) {
        if (goal == null || goal.getTargetDate() == null ||
                goal.getIsCompleted() != null && goal.getIsCompleted()) {
            return 0;
        }

        LocalDate now = LocalDate.now();
        if (goal.getTargetDate().isBefore(now)) {
            return 0;
        }

        long months = ChronoUnit.MONTHS.between(now, goal.getTargetDate());
        return Math.max(0, (int) months);
    }

    // Helper method to calculate monthly contribution
    private void calculateMonthlyContribution(SavingGoal goal) {
        if (goal.getTargetDate() != null && goal.getTargetAmount() != null) {
            int monthsRemaining = getMonthsRemaining(goal);

            if (monthsRemaining > 0) {
                Double saved = goal.getSavedAmount() != null ? goal.getSavedAmount() : 0.0;
                double remainingAmount = goal.getTargetAmount() - saved;

                if (remainingAmount > 0) {
                    double monthly = Math.ceil(remainingAmount / monthsRemaining);
                    goal.setMonthlyContribution(monthly);
                } else {
                    goal.setMonthlyContribution(0.0);
                }
            } else {
                goal.setMonthlyContribution(0.0);
            }
        } else {
            goal.setMonthlyContribution(0.0);
        }
    }

    // Get goals by category for user
    public List<SavingGoal> getGoalsByCategory(Long userId, String category) {
        return savingGoalRepository.findByUserIdAndCategory(userId, category);
    }

    // Get goals by priority for user
    public List<SavingGoal> getGoalsByPriority(Long userId, String priority) {
        return savingGoalRepository.findByUserIdAndPriority(userId, priority);
    }

    // Calculate total saved amount for user
    public Double getTotalSavedByUserId(Long userId) {
        List<SavingGoal> goals = getAllGoalsByUserId(userId);
        return goals.stream()
                .mapToDouble(g -> g.getSavedAmount() != null ? g.getSavedAmount() : 0.0)
                .sum();
    }

    // Calculate total target amount for user
    public Double getTotalTargetByUserId(Long userId) {
        List<SavingGoal> goals = getAllGoalsByUserId(userId);
        return goals.stream()
                .mapToDouble(g -> g.getTargetAmount() != null ? g.getTargetAmount() : 0.0)
                .sum();
    }
}