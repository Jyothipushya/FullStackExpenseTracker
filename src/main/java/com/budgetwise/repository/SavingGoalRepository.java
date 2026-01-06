/*package com.budgetwise.repository;

import com.budgetwise.entity.SavingGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingGoalRepository extends JpaRepository<SavingGoal, Long> {

    // Find goals by user ID
    List<SavingGoal> findByUserId(Long userId);

    // Find active (not completed) goals
    List<SavingGoal> findByUserIdAndIsCompletedFalse(Long userId);

    // Find completed goals
    List<SavingGoal> findByUserIdAndIsCompletedTrue(Long userId);
}*/
package com.budgetwise.repository;

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
}