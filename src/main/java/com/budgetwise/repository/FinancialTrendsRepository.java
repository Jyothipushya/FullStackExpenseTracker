package com.budgetwise.repository;

import com.budgetwise.entity.FinancialTrends;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinancialTrendsRepository extends JpaRepository<FinancialTrends, Long> {
    //List<SavingGoal> findByUserId(Long userId);

    List<FinancialTrends> findByUserIdAndTrendType(Long userId, String trendType);

    List<FinancialTrends> findByUserIdAndAnalysisDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT ft FROM FinancialTrends ft WHERE ft.userId = ?1 AND ft.trendType = ?2 ORDER BY ft.analysisDate DESC")
    List<FinancialTrends> findLatestByUserAndType(Long userId, String trendType);
}