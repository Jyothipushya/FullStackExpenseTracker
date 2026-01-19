package com.budgetwise.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // ============================================
    // ADD THIS SECTION (Expenses Relationship)
    // ============================================
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expenses = new ArrayList<>();
    // ============================================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Add getter for full name (optional but useful)
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
/*package com.budgetwise.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // ============================================
    // Optional field for username (if needed)
    // Add IF NOT EXISTS in database
    // ============================================
    @Column(name = "username", unique = true)
    private String username;
    // ============================================

    // ============================================
    // ADD THIS SECTION (Expenses Relationship)
    // ============================================
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expenses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SavingGoal> savingGoals = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DataExportHistory> exportHistory = new ArrayList<>();
    // ============================================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Add getter for full name (optional but useful)
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // ============================================
    // ADD THIS METHOD for display name in exports
    // Won't break existing code
    // ============================================
    public String getDisplayName() {
        if (username != null && !username.trim().isEmpty()) {
            return username;
        }
        return getFullName();
    }

    // ============================================
    // ADD THIS METHOD for backward compatibility
    // Returns username or generates from email
    // ============================================
    public String getUsernameSafe() {
        if (username != null && !username.trim().isEmpty()) {
            return username;
        }
        // Generate from email if username not set
        if (email != null && email.contains("@")) {
            return email.split("@")[0];
        }
        return "user_" + id;
    }
}*/