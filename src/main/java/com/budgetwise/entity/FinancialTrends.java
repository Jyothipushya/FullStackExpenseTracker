package com.budgetwise.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "financial_trends")
public class FinancialTrends {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private LocalDate analysisDate;
    private String trendType; // MONTHLY_EXPENSE, CATEGORY_BREAKDOWN, etc.

    @Column(columnDefinition = "TEXT")
    private String trendData; // JSON data

    public FinancialTrends() {}

    public FinancialTrends(Long userId, LocalDate analysisDate, String trendType, String trendData) {
        this.userId = userId;
        this.analysisDate = analysisDate;
        this.trendType = trendType;
        this.trendData = trendData;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getAnalysisDate() { return analysisDate; }
    public void setAnalysisDate(LocalDate analysisDate) { this.analysisDate = analysisDate; }

    public String getTrendType() { return trendType; }
    public void setTrendType(String trendType) { this.trendType = trendType; }

    public String getTrendData() { return trendData; }
    public void setTrendData(String trendData) { this.trendData = trendData; }
}