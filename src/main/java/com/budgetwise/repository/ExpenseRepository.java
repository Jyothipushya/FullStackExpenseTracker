/*package com.budgetwise.repository;

import com.budgetwise.entity.Expense;
import com.budgetwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // SIMPLE METHODS THAT WORK:
    List<Expense> findByUser(User user);

    List<Expense> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);

    List<Expense> findByUserAndIsIncome(User user, boolean isIncome);
    // 1. Monthly expense trends
    @Query("SELECT MONTH(e.date) as month, SUM(e.amount) as total " +
            "FROM Expense e WHERE e.userId = ?1 AND YEAR(e.date) = ?2 " +
            "GROUP BY MONTH(e.date) ORDER BY month")
    List<Object[]> getMonthlyExpenseTrend(Long userId, int year);

    // 2. Category breakdown
    @Query("SELECT e.category, SUM(e.amount) as total FROM Expense e " +
            "WHERE e.userId = ?1 AND e.date BETWEEN ?2 AND ?3 " +
            "GROUP BY e.category ORDER BY total DESC")
    List<Object[]> getCategoryBreakdown(Long userId, LocalDate startDate, LocalDate endDate);

    // 3. Spending by day of week
    @Query("SELECT DAYOFWEEK(e.date) as day, SUM(e.amount) as total FROM Expense e " +
            "WHERE e.userId = ?1 AND e.date BETWEEN ?2 AND ?3 " +
            "GROUP BY DAYOFWEEK(e.date) ORDER BY day")
    List<Object[]> getSpendingByDayOfWeek(Long userId, LocalDate startDate, LocalDate endDate);

    // 4. Payment method analysis
    @Query("SELECT e.paymentMethod, SUM(e.amount) as total FROM Expense e " +
            "WHERE e.userId = ?1 AND e.date BETWEEN ?2 AND ?3 " +
            "GROUP BY e.paymentMethod ORDER BY total DESC")
    List<Object[]> getPaymentMethodAnalysis(Long userId, LocalDate startDate, LocalDate endDate);

    // 5. Yearly trends
    @Query("SELECT YEAR(e.date) as year, MONTH(e.date) as month, SUM(e.amount) as total " +
            "FROM Expense e WHERE e.userId = ?1 AND e.date BETWEEN ?2 AND ?3 " +
            "GROUP BY YEAR(e.date), MONTH(e.date) ORDER BY year, month")
    List<Object[]> getYearlyTrend(Long userId, LocalDate startDate, LocalDate endDate);

    // 6. Total expenses for a period
    @Query("SELECT SUM(e.amount) FROM Expense e " +
            "WHERE e.userId = ?1 AND e.date BETWEEN ?2 AND ?3")
    Double getTotalExpensesForPeriod(Long userId, LocalDate startDate, LocalDate endDate);

    // 7. Top categories with limit
    @Query("SELECT e.category, SUM(e.amount) as total FROM Expense e " +
            "WHERE e.userId = ?1 AND e.date BETWEEN ?2 AND ?3 " +
            "GROUP BY e.category ORDER BY total DESC")
    List<Object[]> getTopCategories(Long userId, LocalDate startDate, LocalDate endDate, org.springframework.data.domain.Pageable pageable);

}*/
/*package com.budgetwise.repository;

import com.budgetwise.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.math.BigDecimal;  // ADD THIS IMPORT

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Your existing methods...
    List<Expense> findByUserId(Long userId);
    List<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.userId = :userId " +
            "AND e.date BETWEEN :startDate AND :endDate " +
            "AND e.isIncome = false")
    BigDecimal getTotalExpensesForPeriod(@Param("userId") Long userId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    // ADD THIS METHOD for total income:
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.userId = :userId " +
            "AND e.date BETWEEN :startDate AND :endDate " +
            "AND e.isIncome = true")
    BigDecimal getTotalIncomeForPeriod(@Param("userId") Long userId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);


    // ADD THESE NEW METHODS for trends module:

    @Query(value = "SELECT EXTRACT(MONTH FROM e.date) as month, SUM(e.amount) as total " +
            "FROM Expense e WHERE e.userId = ?1 AND EXTRACT(YEAR FROM e.date) = ?2 " +
            "GROUP BY EXTRACT(MONTH FROM e.date) ORDER BY month")
    List<Object[]> getMonthlyExpenseTrend(Long userId, int year);

    @Query("SELECT e.category, SUM(e.amount) as total FROM Expense e " +
            "WHERE e.userId = ?1 AND e.date BETWEEN ?2 AND ?3 " +
            "GROUP BY e.category ORDER BY total DESC")
    List<Object[]> getCategoryBreakdown(Long userId, LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT EXTRACT(DOW FROM e.date) as day, SUM(e.amount) as total " +
            "FROM Expense e WHERE e.userId = ?1 AND e.date BETWEEN ?2 AND ?3 " +
            "GROUP BY EXTRACT(DOW FROM e.date) ORDER BY day")
    List<Object[]> getSpendingByDayOfWeek(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT e.paymentMethod, SUM(e.amount) as total FROM Expense e " +
            "WHERE e.userId = ?1 AND e.date BETWEEN ?2 AND ?3 " +
            "GROUP BY e.paymentMethod ORDER BY total DESC")
    List<Object[]> getPaymentMethodAnalysis(Long userId, LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT EXTRACT(YEAR FROM e.date) as year, EXTRACT(MONTH FROM e.date) as month, " +
            "SUM(e.amount) as total FROM Expense e " +
            "WHERE e.userId = ?1 AND e.date BETWEEN ?2 AND ?3 " +
            "GROUP BY EXTRACT(YEAR FROM e.date), EXTRACT(MONTH FROM e.date) ORDER BY year, month")
    List<Object[]> getYearlyTrend(Long userId, LocalDate startDate, LocalDate endDate);
}*/
/*package com.budgetwise.repository;

import com.budgetwise.entity.Expense;
import com.budgetwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // SIMPLE METHODS THAT WORK:
    List<Expense> findByUser(User user);

    List<Expense> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);

    List<Expense> findByUserAndIsIncome(User user, boolean isIncome);

    // ADDED: Methods using userId
    List<Expense> findByUserId(Long userId);

    List<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    // 1. Monthly expense trends - FIXED FOR POSTGRESQL
    @Query(value = "SELECT EXTRACT(MONTH FROM e.date) as month, SUM(e.amount) as total " +
            "FROM expenses e WHERE e.user_id = :userId AND EXTRACT(YEAR FROM e.date) = :year " +
            "GROUP BY EXTRACT(MONTH FROM e.date) ORDER BY month", nativeQuery = true)
    List<Object[]> getMonthlyExpenseTrend(@Param("userId") Long userId, @Param("year") int year);

    // 2. Category breakdown
    @Query("SELECT e.category, SUM(e.amount) as total FROM Expense e " +
            "WHERE e.userId = :userId AND e.date BETWEEN :startDate AND :endDate " +
            "GROUP BY e.category ORDER BY total DESC")
    List<Object[]> getCategoryBreakdown(@Param("userId") Long userId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    // 3. Spending by day of week - FIXED FOR POSTGRESQL
    @Query(value = "SELECT EXTRACT(DOW FROM e.date) as day, SUM(e.amount) as total " +
            "FROM expenses e WHERE e.user_id = :userId AND e.date BETWEEN :startDate AND :endDate " +
            "GROUP BY EXTRACT(DOW FROM e.date) ORDER BY day", nativeQuery = true)
    List<Object[]> getSpendingByDayOfWeek(@Param("userId") Long userId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    // 4. Payment method analysis
    @Query("SELECT e.paymentMethod, SUM(e.amount) as total FROM Expense e " +
            "WHERE e.userId = :userId AND e.date BETWEEN :startDate AND :endDate " +
            "GROUP BY e.paymentMethod ORDER BY total DESC")
    List<Object[]> getPaymentMethodAnalysis(@Param("userId") Long userId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    // 5. Yearly trends - FIXED FOR POSTGRESQL
    @Query(value = "SELECT EXTRACT(YEAR FROM e.date) as year, EXTRACT(MONTH FROM e.date) as month, " +
            "SUM(e.amount) as total FROM expenses e " +
            "WHERE e.user_id = :userId AND e.date BETWEEN :startDate AND :endDate " +
            "GROUP BY EXTRACT(YEAR FROM e.date), EXTRACT(MONTH FROM e.date) ORDER BY year, month",
            nativeQuery = true)
    List<Object[]> getYearlyTrend(@Param("userId") Long userId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    // 6. Total expenses for a period - FIXED RETURN TYPE
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.userId = :userId AND e.date BETWEEN :startDate AND :endDate AND e.isIncome = false")
    BigDecimal getTotalExpensesForPeriod(@Param("userId") Long userId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    // 7. Total income for a period
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.userId = :userId AND e.date BETWEEN :startDate AND :endDate AND e.isIncome = true")
    BigDecimal getTotalIncomeForPeriod(@Param("userId") Long userId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    // 8. Top categories
    @Query("SELECT e.category, SUM(e.amount) as total FROM Expense e " +
            "WHERE e.userId = :userId AND e.date BETWEEN :startDate AND :endDate " +
            "GROUP BY e.category ORDER BY total DESC")
    List<Object[]> getTopCategories(@Param("userId") Long userId,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);
}*/
/*package com.budgetwise.repository;

import com.budgetwise.entity.Expense;
import com.budgetwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // SIMPLE METHODS THAT WORK:
    List<Expense> findByUser(User user);

    List<Expense> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);

    List<Expense> findByUserAndIsIncome(User user, boolean isIncome);

    // FIXED: Methods using userId - Use user.id in queries
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId")
    List<Expense> findByUserId(@Param("userId") Long userId);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate")
    List<Expense> findByUserIdAndDateBetween(@Param("userId") Long userId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    // 1. Monthly expense trends - FIXED
    @Query("SELECT FUNCTION('MONTH', e.date), SUM(e.amount) FROM Expense e " +
            "WHERE e.user.id = :userId AND FUNCTION('YEAR', e.date) = :year " +
            "GROUP BY FUNCTION('MONTH', e.date) ORDER BY FUNCTION('MONTH', e.date)")
    List<Object[]> getMonthlyExpenseTrend(@Param("userId") Long userId, @Param("year") int year);

    // 2. Category breakdown - FIXED
    @Query("SELECT e.category, SUM(e.amount) FROM Expense e " +
            "WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate " +
            "GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> getCategoryBreakdown(@Param("userId") Long userId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    // 3. Spending by day of week - FIXED
    @Query("SELECT FUNCTION('DAYOFWEEK', e.date), SUM(e.amount) FROM Expense e " +
            "WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('DAYOFWEEK', e.date) ORDER BY FUNCTION('DAYOFWEEK', e.date)")
    List<Object[]> getSpendingByDayOfWeek(@Param("userId") Long userId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    // 4. Payment method analysis - FIXED (if you have paymentMethod field)
    @Query("SELECT e.paymentMethod, SUM(e.amount) FROM Expense e " +
            "WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate " +
            "GROUP BY e.paymentMethod ORDER BY SUM(e.amount) DESC")
    List<Object[]> getPaymentMethodAnalysis(@Param("userId") Long userId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    // 5. Yearly trends - FIXED
    @Query("SELECT FUNCTION('YEAR', e.date), FUNCTION('MONTH', e.date), SUM(e.amount) FROM Expense e " +
            "WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('YEAR', e.date), FUNCTION('MONTH', e.date) " +
            "ORDER BY FUNCTION('YEAR', e.date), FUNCTION('MONTH', e.date)")
    List<Object[]> getYearlyTrend(@Param("userId") Long userId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    // 6. Total expenses for a period - FIXED
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate AND e.isIncome = false")
    BigDecimal getTotalExpensesForPeriod(@Param("userId") Long userId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    // 7. Total income for a period - FIXED
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate AND e.isIncome = true")
    BigDecimal getTotalIncomeForPeriod(@Param("userId") Long userId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);
}*/
package com.budgetwise.repository;

import com.budgetwise.entity.Expense;
import com.budgetwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // SIMPLE METHODS THAT WORK:
    List<Expense> findByUser(User user);
    List<Expense> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);
    List<Expense> findByUserAndIsIncome(User user, boolean isIncome);

    // Methods using userId
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId")
    List<Expense> findByUserId(@Param("userId") Long userId);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate")
    List<Expense> findByUserIdAndDateBetween(@Param("userId") Long userId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    // 1. Monthly expense trends - NATIVE QUERY FOR POSTGRESQL
    @Query(value = "SELECT EXTRACT(MONTH FROM date) AS month, SUM(amount) AS total " +
            "FROM expenses WHERE user_id = :userId AND EXTRACT(YEAR FROM date) = :year " +
            "GROUP BY EXTRACT(MONTH FROM date) ORDER BY month", nativeQuery = true)
    List<Object[]> getMonthlyExpenseTrend(@Param("userId") Long userId, @Param("year") int year);

    // 2. Category breakdown
    @Query("SELECT e.category, SUM(e.amount) FROM Expense e " +
            "WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate " +
            "GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> getCategoryBreakdown(@Param("userId") Long userId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    // 3. Spending by day of week - NATIVE QUERY FOR POSTGRESQL
    @Query(value = "SELECT EXTRACT(DOW FROM date) AS day, SUM(amount) AS total " +
            "FROM expenses WHERE user_id = :userId AND date BETWEEN :startDate AND :endDate " +
            "GROUP BY EXTRACT(DOW FROM date) ORDER BY day", nativeQuery = true)
    List<Object[]> getSpendingByDayOfWeek(@Param("userId") Long userId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    // 4. Payment method analysis (if you have paymentMethod field)
    //@Query("SELECT e.paymentMethod, SUM(e.amount) FROM Expense e " +
      //      "WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate " +
        //    "GROUP BY e.paymentMethod ORDER BY SUM(e.amount) DESC")
    //List<Object[]> getPaymentMethodAnalysis(@Param("userId") Long userId,
      //                                      @Param("startDate") LocalDate startDate,
        //                                    @Param("endDate") LocalDate endDate);

    // 5. Yearly trends - NATIVE QUERY FOR POSTGRESQL
    @Query(value = "SELECT EXTRACT(YEAR FROM date) AS year, EXTRACT(MONTH FROM date) AS month, " +
            "SUM(amount) AS total FROM expenses " +
            "WHERE user_id = :userId AND date BETWEEN :startDate AND :endDate " +
            "GROUP BY EXTRACT(YEAR FROM date), EXTRACT(MONTH FROM date) " +
            "ORDER BY year, month", nativeQuery = true)
    List<Object[]> getYearlyTrend(@Param("userId") Long userId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    // 6. Total expenses for a period
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate AND e.isIncome = false")
    BigDecimal getTotalExpensesForPeriod(@Param("userId") Long userId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    // 7. Total income for a period
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate AND e.isIncome = true")
    BigDecimal getTotalIncomeForPeriod(@Param("userId") Long userId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);
}