package com.budgetwise.controller;

import com.budgetwise.entity.DataExportHistory;
import com.budgetwise.security.CustomUserDetails;
import com.budgetwise.service.DataExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/export")
public class DataExportController {

    private static final Logger logger = LoggerFactory.getLogger(DataExportController.class);

    @Autowired
    private DataExportService dataExportService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/dashboard")
    public String showExportDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            Long userId = userDetails.getUserId();
            String userName = userDetails.getFullName();
            String userEmail = userDetails.getUsername();

            // Get export history
            List<DataExportHistory> exportHistory = dataExportService.getExportHistory(userId);

            // Calculate statistics
            long totalExports = exportHistory.size();
            long completedExports = exportHistory.stream()
                    .filter(h -> h.getStatus() != null && h.getStatus().equals("COMPLETED"))
                    .count();
            long pendingExports = exportHistory.stream()
                    .filter(h -> h.getStatus() != null && h.getStatus().equals("PROCESSING"))
                    .count();

            long totalSize = exportHistory.stream()
                    .mapToLong(h -> h.getFileSize() != null ? h.getFileSize() : 0L)
                    .sum();
            double dataSizeMB = totalSize / (1024.0 * 1024.0);

            // Add statistics to model
            model.addAttribute("totalExports", totalExports);
            model.addAttribute("completedExports", completedExports);
            model.addAttribute("pendingExports", pendingExports);
            model.addAttribute("dataSize", String.format("%.2f", dataSizeMB));
            model.addAttribute("currentYear", LocalDate.now().getYear());

            // User info
            model.addAttribute("userName", userName);
            model.addAttribute("userEmail", userEmail);
            model.addAttribute("exportHistory", exportHistory);

            return "export-dashboard";

        } catch (Exception e) {
            logger.error("Error loading export dashboard", e);
            model.addAttribute("error", "Error loading export dashboard");
            return "export-dashboard";
        }
    }

    // Export expenses to Excel
    @GetMapping("/expenses/excel")
    public ResponseEntity<byte[]> exportExpensesToExcel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category) {

        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Long userId = userDetails.getUserId();

            // Set default dates if not provided
            if (startDate == null) {
                startDate = LocalDate.now().minusMonths(1);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }

            byte[] excelData = dataExportService.exportExpensesToExcel(userId, startDate, endDate, category);

            if (excelData == null || excelData.length == 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error generating Excel file".getBytes());
            }

            // Save to history
            DataExportHistory history = new DataExportHistory();
            history.setUserId(userId);
            history.setExportType("EXPENSES");
            history.setFormat("EXCEL");
            history.setFileName(dataExportService.generateFileName("expenses", "xlsx"));
            history.setFileSize((long) excelData.length);
            history.setStatus("COMPLETED");
            history.setDownloadUrl("/export/download/" + history.getFileName());
            history.setExpiryDate(LocalDateTime.now().plusDays(7));
            dataExportService.saveExportHistory(history);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(history.getFileName())
                    .build());
            headers.setContentLength(excelData.length);

            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error exporting expenses to Excel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error exporting data: " + e.getMessage()).getBytes());
        }
    }

    // Export saving goals to Excel
    @GetMapping("/goals/excel")
    public ResponseEntity<byte[]> exportSavingGoalsToExcel(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Long userId = userDetails.getUserId();

            byte[] excelData = dataExportService.exportSavingGoalsToExcel(userId);

            if (excelData == null || excelData.length == 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error generating Excel file".getBytes());
            }

            DataExportHistory history = new DataExportHistory();
            history.setUserId(userId);
            history.setExportType("GOALS");
            history.setFormat("EXCEL");
            history.setFileName(dataExportService.generateFileName("saving_goals", "xlsx"));
            history.setFileSize((long) excelData.length);
            history.setStatus("COMPLETED");
            history.setDownloadUrl("/export/download/" + history.getFileName());
            history.setExpiryDate(LocalDateTime.now().plusDays(7));
            dataExportService.saveExportHistory(history);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(history.getFileName())
                    .build());
            headers.setContentLength(excelData.length);

            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error exporting goals to Excel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error exporting data: " + e.getMessage()).getBytes());
        }
    }

    // Export financial trends to Excel
    @GetMapping("/trends/excel")
    public ResponseEntity<byte[]> exportFinancialTrendsToExcel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "2024") int year) {

        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Long userId = userDetails.getUserId();

            byte[] excelData = dataExportService.exportFinancialTrendsToExcel(userId, year);

            if (excelData == null || excelData.length == 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error generating Excel file".getBytes());
            }

            DataExportHistory history = new DataExportHistory();
            history.setUserId(userId);
            history.setExportType("TRENDS");
            history.setFormat("EXCEL");
            history.setFileName(dataExportService.generateFileName("financial_trends", "xlsx"));
            history.setFileSize((long) excelData.length);
            history.setStatus("COMPLETED");
            history.setDownloadUrl("/export/download/" + history.getFileName());
            history.setExpiryDate(LocalDateTime.now().plusDays(7));
            dataExportService.saveExportHistory(history);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(history.getFileName())
                    .build());
            headers.setContentLength(excelData.length);

            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error exporting trends to Excel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error exporting data: " + e.getMessage()).getBytes());
        }
    }

    // Export expenses to CSV
    @GetMapping("/expenses/csv")
    public ResponseEntity<String> exportExpensesToCsv(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Long userId = userDetails.getUserId();

            if (startDate == null) {
                startDate = LocalDate.now().minusMonths(1);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }

            String csvData = dataExportService.exportExpensesToCsv(userId, startDate, endDate);

            if (csvData == null || csvData.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error generating CSV data");
            }

            DataExportHistory history = new DataExportHistory();
            history.setUserId(userId);
            history.setExportType("EXPENSES");
            history.setFormat("CSV");
            history.setFileName(dataExportService.generateFileName("expenses", "csv"));
            history.setFileSize((long) csvData.getBytes().length);
            history.setStatus("COMPLETED");
            history.setDownloadUrl("/export/download/" + history.getFileName());
            history.setExpiryDate(LocalDateTime.now().plusDays(7));
            dataExportService.saveExportHistory(history);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(history.getFileName())
                    .build());
            headers.setContentLength(csvData.getBytes().length);

            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error exporting expenses to CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    // Export saving goals to CSV
    @GetMapping("/goals/csv")
    public ResponseEntity<String> exportGoalsToCsv(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Long userId = userDetails.getUserId();

            String csvData = dataExportService.exportSavingGoalsToCsv(userId);

            if (csvData == null || csvData.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error generating CSV data");
            }

            DataExportHistory history = new DataExportHistory();
            history.setUserId(userId);
            history.setExportType("GOALS");
            history.setFormat("CSV");
            history.setFileName(dataExportService.generateFileName("goals", "csv"));
            history.setFileSize((long) csvData.getBytes().length);
            history.setStatus("COMPLETED");
            history.setDownloadUrl("/export/download/" + history.getFileName());
            history.setExpiryDate(LocalDateTime.now().plusDays(7));
            dataExportService.saveExportHistory(history);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(history.getFileName())
                    .build());
            headers.setContentLength(csvData.getBytes().length);

            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error exporting goals to CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    // Export budgets to CSV
    @GetMapping("/budgets/csv")
    public ResponseEntity<String> exportBudgetsToCsv(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Long userId = userDetails.getUserId();

            String csvData = dataExportService.exportBudgetsToCsv(userId);

            if (csvData == null || csvData.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error generating CSV data");
            }

            DataExportHistory history = new DataExportHistory();
            history.setUserId(userId);
            history.setExportType("BUDGETS");
            history.setFormat("CSV");
            history.setFileName(dataExportService.generateFileName("budgets", "csv"));
            history.setFileSize((long) csvData.getBytes().length);
            history.setStatus("COMPLETED");
            history.setDownloadUrl("/export/download/" + history.getFileName());
            history.setExpiryDate(LocalDateTime.now().plusDays(7));
            dataExportService.saveExportHistory(history);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(history.getFileName())
                    .build());
            headers.setContentLength(csvData.getBytes().length);

            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error exporting budgets to CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    // Export complete data to JSON - SIMPLIFIED VERSION (using available method)
    @GetMapping("/complete/json")
    public ResponseEntity<String> exportCompleteDataToJson(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Long userId = userDetails.getUserId();

            // Create a simple JSON response since the method doesn't exist
            Map<String, Object> jsonData = new HashMap<>();
            jsonData.put("status", "success");
            jsonData.put("message", "JSON export is not yet implemented");
            jsonData.put("userId", userId);
            jsonData.put("timestamp", LocalDateTime.now().toString());

            String jsonString = objectMapper.writeValueAsString(jsonData);

            DataExportHistory history = new DataExportHistory();
            history.setUserId(userId);
            history.setExportType("COMPLETE");
            history.setFormat("JSON");
            history.setFileName(dataExportService.generateFileName("complete_data", "json"));
            history.setFileSize((long) jsonString.getBytes().length);
            history.setStatus("COMPLETED");
            history.setDownloadUrl("/export/download/" + history.getFileName());
            history.setExpiryDate(LocalDateTime.now().plusDays(7));
            dataExportService.saveExportHistory(history);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(history.getFileName())
                    .build());
            headers.setContentLength(jsonString.getBytes().length);

            return new ResponseEntity<>(jsonString, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error exporting complete data to JSON", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Error generating JSON export: " + e.getMessage() + "\"}");
        }
    }

    // Export financial trends to text - SIMPLIFIED VERSION (using available method)
    @GetMapping("/trends/text")
    public ResponseEntity<String> exportTrendsToText(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "2024") int year) {

        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Long userId = userDetails.getUserId();

            // Create a simple text report since the method doesn't exist
            StringBuilder textData = new StringBuilder();
            textData.append("BUDGETWISE FINANCIAL TRENDS REPORT\n");
            textData.append("===================================\n");
            textData.append("Year: ").append(year).append("\n");
            textData.append("Generated: ").append(LocalDateTime.now()).append("\n");
            textData.append("User ID: ").append(userId).append("\n");
            textData.append("\n");
            textData.append("NOTE: Detailed trends report is not yet implemented.\n");
            textData.append("Please use Excel format for full financial trends.\n");

            String textContent = textData.toString();

            DataExportHistory history = new DataExportHistory();
            history.setUserId(userId);
            history.setExportType("TRENDS");
            history.setFormat("TEXT");
            history.setFileName(dataExportService.generateFileName("financial_trends", "txt"));
            history.setFileSize((long) textContent.getBytes().length);
            history.setStatus("COMPLETED");
            history.setDownloadUrl("/export/download/" + history.getFileName());
            history.setExpiryDate(LocalDateTime.now().plusDays(7));
            dataExportService.saveExportHistory(history);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(history.getFileName())
                    .build());
            headers.setContentLength(textContent.getBytes().length);

            return new ResponseEntity<>(textContent, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error exporting trends to text", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    // Export expenses to HTML
    @GetMapping("/expenses/html")
    public ResponseEntity<String> exportExpensesToHtml(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category) {

        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("<html><body><h1>Please login first</h1></body></html>");
            }

            Long userId = userDetails.getUserId();

            // Set default dates if not provided
            if (startDate == null) {
                startDate = LocalDate.now().minusMonths(1);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }

            // Get HTML content from service
            String htmlContent = dataExportService.exportExpensesToHtml(userId, startDate, endDate, category);

            if (htmlContent == null || htmlContent.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("<html><body><h1>Error generating HTML report</h1></body></html>");
            }

            // Save to history
            DataExportHistory history = new DataExportHistory();
            history.setUserId(userId);
            history.setExportType("EXPENSES");
            history.setFormat("HTML");
            history.setFileName(dataExportService.generateFileName("expenses", "html"));
            history.setFileSize((long) htmlContent.getBytes().length);
            history.setStatus("COMPLETED");
            history.setDownloadUrl("/export/download/" + history.getFileName());
            history.setExpiryDate(LocalDateTime.now().plusDays(7));
            dataExportService.saveExportHistory(history);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(history.getFileName())
                    .build());
            headers.setContentLength(htmlContent.getBytes().length);

            return new ResponseEntity<>(htmlContent, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error exporting expenses to HTML", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("<html><body><h1>Error generating HTML report: " + e.getMessage() + "</h1></body></html>");
        }
    }

    // Get export history - API endpoint
    @GetMapping("/history")
    @ResponseBody
    public ResponseEntity<List<DataExportHistory>> getExportHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(List.of());
            }

            Long userId = userDetails.getUserId();
            List<DataExportHistory> history = dataExportService.getExportHistory(userId);
            return ResponseEntity.ok(history);

        } catch (Exception e) {
            logger.error("Error getting export history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    // Delete export history
    @DeleteMapping("/history/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteExportHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        Map<String, String> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("status", "error");
                response.put("message", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            dataExportService.deleteExportHistory(id);
            response.put("status", "success");
            response.put("message", "Export history deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting export history", e);
            response.put("status", "error");
            response.put("message", "Error deleting export history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Download file endpoint
    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> downloadFile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String fileName) {

        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // For now, return a simple response
            String message = "File download not implemented yet. File: " + fileName;
            byte[] data = message.getBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(fileName)
                    .build());
            headers.setContentLength(data.length);

            return new ResponseEntity<>(data, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error downloading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Test endpoint to check if export is working
    @GetMapping("/test-export")
    public ResponseEntity<byte[]> testExport(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Long userId = userDetails.getUserId();
            String testContent = "Test Export Content\nUser ID: " + userId + "\nDate: " + LocalDateTime.now();
            byte[] data = testContent.getBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("test_export.txt")
                    .build());

            return new ResponseEntity<>(data, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}