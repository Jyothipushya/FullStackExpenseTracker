package com.budgetwise.service;

import com.budgetwise.entity.*;
import com.budgetwise.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class DataExportService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private SavingGoalRepository savingGoalRepository;

    @Autowired
    private DataExportRepository dataExportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private FinancialTrendsRepository financialTrendsRepository;

    // ============================================
    // EXPORT EXPENSES TO EXCEL (FIXED VERSION)
    // ============================================
    public byte[] exportExpensesToExcel(Long userId, LocalDate startDate, LocalDate endDate, String category) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            // Get expenses with null checks
            List<Expense> expenses;
            if (category != null && !category.isEmpty()) {
                expenses = expenseRepository.findByUserIdAndCategoryAndDateBetween(userId, category, startDate, endDate);
            } else {
                expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
            }

            if (expenses == null) {
                expenses = new ArrayList<>();
            }

            // Sort by date descending
            expenses.sort((e1, e2) -> {
                if (e1.getDate() == null) return 1;
                if (e2.getDate() == null) return -1;
                return e2.getDate().compareTo(e1.getDate());
            });

            // Create Excel workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Expenses Report");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Date", "Description", "Category", "Amount", "Type"};

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            int rowNum = 1;
            BigDecimal totalIncome = BigDecimal.ZERO;
            BigDecimal totalExpense = BigDecimal.ZERO;

            for (Expense expense : expenses) {
                Row row = sheet.createRow(rowNum++);

                // Date
                row.createCell(0).setCellValue(expense.getDate() != null ? expense.getDate().toString() : "");

                // Description
                row.createCell(1).setCellValue(expense.getDescription() != null ? expense.getDescription() : "");

                // Category
                row.createCell(2).setCellValue(expense.getCategory() != null ? expense.getCategory() : "");

                // Amount with null check
                BigDecimal amount = expense.getAmount() != null ? expense.getAmount() : BigDecimal.ZERO;
                Cell amountCell = row.createCell(3);
                amountCell.setCellValue(amount.doubleValue());

                // Type
                String type = expense.isIncome() ? "Income" : "Expense";
                row.createCell(4).setCellValue(type);

                // Color coding for amounts
                CellStyle amountStyle = workbook.createCellStyle();
                Font amountFont = workbook.createFont();
                if (expense.isIncome()) {
                    amountFont.setColor(IndexedColors.GREEN.getIndex());
                    amountStyle.setFont(amountFont);
                    totalIncome = totalIncome.add(amount);
                } else {
                    amountFont.setColor(IndexedColors.RED.getIndex());
                    amountStyle.setFont(amountFont);
                    totalExpense = totalExpense.add(amount);
                }
                amountCell.setCellStyle(amountStyle);
            }

            // Add summary section only if there are expenses
            if (!expenses.isEmpty()) {
                Row summaryRow1 = sheet.createRow(rowNum + 1);
                summaryRow1.createCell(0).setCellValue("SUMMARY");

                CellStyle summaryStyle = workbook.createCellStyle();
                Font summaryFont = workbook.createFont();
                summaryFont.setBold(true);
                summaryStyle.setFont(summaryFont);
                summaryRow1.getCell(0).setCellStyle(summaryStyle);

                Row summaryRow2 = sheet.createRow(rowNum + 2);
                summaryRow2.createCell(0).setCellValue("Total Income:");
                summaryRow2.createCell(1).setCellValue(totalIncome.doubleValue());

                Row summaryRow3 = sheet.createRow(rowNum + 3);
                summaryRow3.createCell(0).setCellValue("Total Expenses:");
                summaryRow3.createCell(1).setCellValue(totalExpense.doubleValue());

                Row summaryRow4 = sheet.createRow(rowNum + 4);
                summaryRow4.createCell(0).setCellValue("Net Balance:");
                summaryRow4.createCell(1).setCellValue(totalIncome.subtract(totalExpense).doubleValue());
            } else {
                // Add message for empty data
                Row emptyRow = sheet.createRow(1);
                emptyRow.createCell(0).setCellValue("No expense data found for the selected period");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            workbook.write(outputStream);
            workbook.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            // Return simple error data instead of throwing exception
            try {
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Error");
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("Error generating Excel file: " + e.getMessage());
                workbook.write(outputStream);
                workbook.close();
                return outputStream.toByteArray();
            } catch (Exception ex) {
                return ("Error: " + e.getMessage()).getBytes();
            }
        }
    }

    // ============================================
    // EXPORT SAVING GOALS TO EXCEL (FIXED VERSION)
    // ============================================
    public byte[] exportSavingGoalsToExcel(Long userId) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            List<SavingGoal> goals = savingGoalRepository.findByUserId(userId);

            if (goals == null) {
                goals = new ArrayList<>();
            }

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Saving Goals");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Goal Name", "Category", "Target Amount", "Saved Amount",
                    "Remaining", "Progress %", "Target Date", "Status"};

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            int rowNum = 1;
            BigDecimal totalTarget = BigDecimal.ZERO;
            BigDecimal totalSaved = BigDecimal.ZERO;

            for (SavingGoal goal : goals) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(goal.getName() != null ? goal.getName() : "");
                row.createCell(1).setCellValue(goal.getCategory() != null ? goal.getCategory() : "");

                double target = goal.getTargetAmount() != null ? goal.getTargetAmount() : 0.0;
                double saved = goal.getSavedAmount() != null ? goal.getSavedAmount() : 0.0;
                double remaining = target - saved;
                double progress = target > 0 ? (saved / target) * 100 : 0;
                String status = Boolean.TRUE.equals(goal.getIsCompleted()) ? "Completed" : "In Progress";

                row.createCell(2).setCellValue(target);
                row.createCell(3).setCellValue(saved);
                row.createCell(4).setCellValue(remaining);
                row.createCell(5).setCellValue(progress);
                row.createCell(6).setCellValue(goal.getTargetDate() != null ? goal.getTargetDate().toString() : "");
                row.createCell(7).setCellValue(status);

                totalTarget = totalTarget.add(BigDecimal.valueOf(target));
                totalSaved = totalSaved.add(BigDecimal.valueOf(saved));
            }

            // Add summary if there are goals
            if (!goals.isEmpty()) {
                Row summaryRow1 = sheet.createRow(rowNum + 1);
                summaryRow1.createCell(0).setCellValue("SUMMARY");

                CellStyle summaryStyle = workbook.createCellStyle();
                Font summaryFont = workbook.createFont();
                summaryFont.setBold(true);
                summaryStyle.setFont(summaryFont);
                summaryRow1.getCell(0).setCellStyle(summaryStyle);

                Row summaryRow2 = sheet.createRow(rowNum + 2);
                summaryRow2.createCell(0).setCellValue("Total Target:");
                summaryRow2.createCell(1).setCellValue(totalTarget.doubleValue());

                Row summaryRow3 = sheet.createRow(rowNum + 3);
                summaryRow3.createCell(0).setCellValue("Total Saved:");
                summaryRow3.createCell(1).setCellValue(totalSaved.doubleValue());

                Row summaryRow4 = sheet.createRow(rowNum + 4);
                summaryRow4.createCell(0).setCellValue("Remaining:");
                summaryRow4.createCell(1).setCellValue(totalTarget.subtract(totalSaved).doubleValue());

                Row summaryRow5 = sheet.createRow(rowNum + 5);
                summaryRow5.createCell(0).setCellValue("Overall Progress:");
                double overallProgress = totalTarget.doubleValue() > 0 ?
                        (totalSaved.doubleValue() / totalTarget.doubleValue()) * 100 : 0;
                summaryRow5.createCell(1).setCellValue(overallProgress);
            } else {
                Row emptyRow = sheet.createRow(1);
                emptyRow.createCell(0).setCellValue("No saving goals found");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            workbook.write(outputStream);
            workbook.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            // Return simple error data
            try {
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Error");
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("Error generating Excel file: " + e.getMessage());
                workbook.write(outputStream);
                workbook.close();
                return outputStream.toByteArray();
            } catch (Exception ex) {
                return ("Error: " + e.getMessage()).getBytes();
            }
        }
    }

    // ============================================
    // EXPORT FINANCIAL TRENDS TO EXCEL (FIXED VERSION)
    // ============================================
    public byte[] exportFinancialTrendsToExcel(Long userId, int year) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            Workbook workbook = new XSSFWorkbook();

            // Create summary sheet
            Sheet summarySheet = workbook.createSheet("Financial Summary");
            int rowNum = 0;

            // Add report title
            Row titleRow = summarySheet.createRow(rowNum++);
            titleRow.createCell(0).setCellValue("BudgetWise Financial Trends Report - " + year);

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleRow.getCell(0).setCellStyle(titleStyle);

            // Try to get data with null checks
            List<Object[]> monthlyTrends = null;
            try {
                monthlyTrends = expenseRepository.getMonthlyExpenseTrend(userId, year);
            } catch (Exception e) {
                monthlyTrends = new ArrayList<>();
            }

            // Add basic information
            summarySheet.createRow(rowNum++).createCell(0).setCellValue("Report Generated: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // Write to byte array
            workbook.write(outputStream);
            workbook.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            // Return simple error data
            try {
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Error");
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("Error generating trends Excel file: " + e.getMessage());
                workbook.write(outputStream);
                workbook.close();
                return outputStream.toByteArray();
            } catch (Exception ex) {
                return ("Error: " + e.getMessage()).getBytes();
            }
        }
    }

    // ============================================
    // EXPORT EXPENSES TO CSV (FIXED VERSION)
    // ============================================
    public String exportExpensesToCsv(Long userId, LocalDate startDate, LocalDate endDate) {
        try {
            List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

            if (expenses == null) {
                expenses = new ArrayList<>();
            }

            StringBuilder csv = new StringBuilder();
            csv.append("Date,Description,Category,Amount,Type,Is Income\n");

            for (Expense expense : expenses) {
                BigDecimal amount = expense.getAmount() != null ? expense.getAmount() : BigDecimal.ZERO;

                csv.append(expense.getDate() != null ? expense.getDate() : "").append(",")
                        .append(escapeCsv(expense.getDescription() != null ? expense.getDescription() : "")).append(",")
                        .append(escapeCsv(expense.getCategory() != null ? expense.getCategory() : "")).append(",")
                        .append(amount.setScale(2)).append(",")
                        .append(expense.isIncome() ? "Income" : "Expense").append(",")
                        .append(expense.isIncome() ? "Yes" : "No")
                        .append("\n");
            }

            // If no expenses, return header only
            if (expenses.isEmpty()) {
                csv.append("No expenses found for the selected period\n");
            }

            return csv.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Date,Description,Category,Amount,Type,Is Income\nError: " + e.getMessage() + "\n";
        }
    }

    // ============================================
    // EXPORT SAVING GOALS TO CSV (FIXED VERSION)
    // ============================================
    public String exportSavingGoalsToCsv(Long userId) {
        try {
            List<SavingGoal> goals = savingGoalRepository.findByUserId(userId);

            if (goals == null) {
                goals = new ArrayList<>();
            }

            StringBuilder csv = new StringBuilder();
            csv.append("Goal Name,Category,Target Amount,Saved Amount,Remaining,Progress %,Target Date,Start Date,Priority,Monthly Contribution,Status\n");

            for (SavingGoal goal : goals) {
                double target = goal.getTargetAmount() != null ? goal.getTargetAmount() : 0.0;
                double saved = goal.getSavedAmount() != null ? goal.getSavedAmount() : 0.0;
                double remaining = target - saved;
                double progress = target > 0 ? (saved / target) * 100 : 0;
                String status = Boolean.TRUE.equals(goal.getIsCompleted()) ? "Completed" : "In Progress";
                String priority = goal.getPriority() != null ? goal.getPriority() : "MEDIUM";
                double monthly = goal.getMonthlyContribution() != null ? goal.getMonthlyContribution() : 0.0;

                csv.append(escapeCsv(goal.getName() != null ? goal.getName() : "")).append(",")
                        .append(escapeCsv(goal.getCategory() != null ? goal.getCategory() : "")).append(",")
                        .append(String.format("%.2f", target)).append(",")
                        .append(String.format("%.2f", saved)).append(",")
                        .append(String.format("%.2f", remaining)).append(",")
                        .append(String.format("%.1f", progress)).append(",")
                        .append(goal.getTargetDate() != null ? goal.getTargetDate().toString() : "").append(",")
                        .append(goal.getStartDate() != null ? goal.getStartDate().toString() : "").append(",")
                        .append(priority).append(",")
                        .append(String.format("%.2f", monthly)).append(",")
                        .append(status)
                        .append("\n");
            }

            // If no goals, return header only
            if (goals.isEmpty()) {
                csv.append("No saving goals found\n");
            }

            return csv.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Goal Name,Category,Target Amount,Saved Amount,Remaining,Progress %,Target Date,Start Date,Priority,Monthly Contribution,Status\nError: " + e.getMessage() + "\n";
        }
    }

    // ============================================
    // EXPORT BUDGETS TO CSV (FIXED VERSION)
    // ============================================
    public String exportBudgetsToCsv(Long userId) {
        try {
            // Get user first
            User user = userRepository.findById(userId).orElse(null);

            List<Budget> budgets = new ArrayList<>();
            if (user != null) {
                budgets = budgetRepository.findByUser(user);
            }

            StringBuilder csv = new StringBuilder();
            csv.append("Category,Monthly Limit,Current Month,Created Date,Updated Date\n");

            for (Budget budget : budgets) {
                csv.append(escapeCsv(budget.getCategory() != null ? budget.getCategory() : "")).append(",")
                        .append(budget.getMonthlyLimit() != null ? budget.getMonthlyLimit().setScale(2) : "0.00").append(",")
                        .append(budget.getCurrentMonth() != null ? budget.getCurrentMonth().toString() : "").append(",")
                        .append(budget.getCreatedAt() != null ?
                                budget.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE) : "").append(",")
                        .append(budget.getUpdatedAt() != null ?
                                budget.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE) : "")
                        .append("\n");
            }

            // If no budgets, return header only
            if (budgets.isEmpty()) {
                csv.append("No budgets found\n");
            }

            return csv.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Category,Monthly Limit,Current Month,Created Date,Updated Date\nError: " + e.getMessage() + "\n";
        }
    }

    // ============================================
    // EXPORT EXPENSES TO HTML (FIXED VERSION)
    // ============================================
    public String exportExpensesToHtml(Long userId, LocalDate startDate, LocalDate endDate, String category) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            String displayName = user != null ? user.getFullName() : "User";
            String userEmail = user != null ? user.getEmail() : "";

            // Get expenses
            List<Expense> expenses;
            if (category != null && !category.isEmpty()) {
                expenses = expenseRepository.findByUserIdAndCategoryAndDateBetween(userId, category, startDate, endDate);
            } else {
                expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
            }

            if (expenses == null) {
                expenses = new ArrayList<>();
            }

            // Generate HTML
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n")
                    .append("<html>\n")
                    .append("<head>\n")
                    .append("    <meta charset=\"UTF-8\">\n")
                    .append("    <title>BudgetWise - Expenses Report</title>\n")
                    .append("    <style>\n")
                    .append("        body { font-family: Arial, sans-serif; margin: 20px; }\n")
                    .append("        table { border-collapse: collapse; width: 100%; margin-top: 20px; }\n")
                    .append("        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n")
                    .append("        th { background-color: #4CAF50; color: white; }\n")
                    .append("        .total-row { font-weight: bold; }\n")
                    .append("    </style>\n")
                    .append("</head>\n")
                    .append("<body>\n")
                    .append("    <h2>BudgetWise - Expenses Report</h2>\n")
                    .append("    <p><strong>User:</strong> ").append(escapeHtml(displayName)).append("</p>\n")
                    .append("    <p><strong>Email:</strong> ").append(escapeHtml(userEmail)).append("</p>\n")
                    .append("    <p><strong>Period:</strong> ").append(startDate).append(" to ").append(endDate).append("</p>\n");

            if (category != null && !category.isEmpty()) {
                html.append("    <p><strong>Category:</strong> ").append(escapeHtml(category)).append("</p>\n");
            }

            html.append("    <p><strong>Generated:</strong> ").append(LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>\n");

            // Expenses Table
            html.append("    <h3>Transaction Details</h3>\n")
                    .append("    <table>\n")
                    .append("        <tr>\n")
                    .append("            <th>Date</th>\n")
                    .append("            <th>Description</th>\n")
                    .append("            <th>Category</th>\n")
                    .append("            <th>Amount</th>\n")
                    .append("            <th>Type</th>\n")
                    .append("        </tr>\n");

            if (expenses.isEmpty()) {
                html.append("        <tr>\n")
                        .append("            <td colspan=\"5\" style=\"text-align: center;\">\n")
                        .append("                No transactions found for the selected period\n")
                        .append("            </td>\n")
                        .append("        </tr>\n");
            } else {
                BigDecimal totalIncome = BigDecimal.ZERO;
                BigDecimal totalExpense = BigDecimal.ZERO;

                for (Expense expense : expenses) {
                    String typeText = expense.isIncome() ? "Income" : "Expense";
                    BigDecimal amount = expense.getAmount() != null ? expense.getAmount() : BigDecimal.ZERO;

                    if (expense.isIncome()) {
                        totalIncome = totalIncome.add(amount);
                    } else {
                        totalExpense = totalExpense.add(amount);
                    }

                    html.append("        <tr>\n")
                            .append("            <td>").append(expense.getDate() != null ? expense.getDate() : "").append("</td>\n")
                            .append("            <td>").append(escapeHtml(expense.getDescription() != null ? expense.getDescription() : "")).append("</td>\n")
                            .append("            <td>").append(escapeHtml(expense.getCategory() != null ? expense.getCategory() : "")).append("</td>\n")
                            .append("            <td>").append(amount.setScale(2)).append("</td>\n")
                            .append("            <td>").append(typeText).append("</td>\n")
                            .append("        </tr>\n");
                }

                // Add totals
                html.append("        <tr class=\"total-row\">\n")
                        .append("            <td colspan=\"3\"><strong>Total Income:</strong></td>\n")
                        .append("            <td colspan=\"2\">").append(totalIncome.setScale(2)).append("</td>\n")
                        .append("        </tr>\n")
                        .append("        <tr class=\"total-row\">\n")
                        .append("            <td colspan=\"3\"><strong>Total Expenses:</strong></td>\n")
                        .append("            <td colspan=\"2\">").append(totalExpense.setScale(2)).append("</td>\n")
                        .append("        </tr>\n")
                        .append("        <tr class=\"total-row\">\n")
                        .append("            <td colspan=\"3\"><strong>Net Balance:</strong></td>\n")
                        .append("            <td colspan=\"2\">").append(totalIncome.subtract(totalExpense).setScale(2)).append("</td>\n")
                        .append("        </tr>\n");
            }

            html.append("    </table>\n")
                    .append("</body>\n")
                    .append("</html>");

            return html.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "<html><body><h1>Error generating report</h1><p>" + escapeHtml(e.getMessage()) + "</p></body></html>";
        }
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // ============================================
    // HISTORY MANAGEMENT METHODS
    // ============================================

    public DataExportHistory saveExportHistory(DataExportHistory exportHistory) {
        try {
            return dataExportRepository.save(exportHistory);
        } catch (Exception e) {
            e.printStackTrace();
            return exportHistory;
        }
    }

    public List<DataExportHistory> getExportHistory(Long userId) {
        try {
            return dataExportRepository.findByUserIdOrderByExportDateDesc(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void deleteExportHistory(Long id) {
        try {
            dataExportRepository.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error deleting export history");
        }
    }

    public String generateFileName(String exportType, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        return String.format("budgetwise_%s_%s.%s", exportType.toLowerCase(), timestamp, format.toLowerCase());
    }
}