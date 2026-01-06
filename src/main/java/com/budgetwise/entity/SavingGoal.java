package com.budgetwise.entity;

import javax.persistence.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "saving_goals")
public class SavingGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String category;
    private Double targetAmount;
    private Double savedAmount = 0.0;
    private Double monthlyContribution;
    private String priority = "MEDIUM";
    private LocalDate targetDate;
    private LocalDate startDate = LocalDate.now();
    private Boolean isCompleted = false;
    private String icon = "fa-bullseye";

    // Remove User relationship for now to simplify
    // @ManyToOne
    // private User user;
    private Long userId;  // Simple foreign key

    // Getters and Setters (generate these in IDE)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(Double targetAmount) { this.targetAmount = targetAmount; }

    public Double getSavedAmount() { return savedAmount; }
    public void setSavedAmount(Double savedAmount) { this.savedAmount = savedAmount; }

    public Double getMonthlyContribution() { return monthlyContribution; }
    public void setMonthlyContribution(Double monthlyContribution) { this.monthlyContribution = monthlyContribution; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    // Helper methods
    public double getProgressPercentage() {
        if (targetAmount == null || targetAmount == 0) return 0;
        return (savedAmount / targetAmount) * 100;
    }
}
/*package com.budgetwise.entity;

import javax.persistence.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;  // Add this import

@Entity
@Table(name = "saving_goals")
public class SavingGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String category;
    private Double targetAmount;
    private Double savedAmount = 0.0;
    private Double monthlyContribution;
    private String priority = "MEDIUM";

    @DateTimeFormat(pattern = "yyyy-MM-dd")  // ADD THIS LINE
    private LocalDate targetDate;

    private LocalDate startDate = LocalDate.now();
    private Boolean isCompleted = false;
    private String icon = "fa-bullseye";
    private Long userId;

    // ... rest of your getters and setters ...
}*/
