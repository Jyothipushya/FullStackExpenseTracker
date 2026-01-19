package com.budgetwise.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "data_export_history")
public class DataExportHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private LocalDateTime exportDate;
    private String exportType; // EXPENSE, GOALS, BUDGET, TRENDS, COMPLETE
    private String format; // PDF, EXCEL, CSV, JSON, HTML
    private String fileName;
    private Long fileSize; // in bytes
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED

    @Column(columnDefinition = "TEXT")
    private String filterCriteria; // JSON of filters applied

    @Column(columnDefinition = "TEXT")
    private String downloadUrl;

    private LocalDateTime expiryDate; // When download link expires

    // Constructor
    public DataExportHistory() {
        this.exportDate = LocalDateTime.now();
        this.status = "PENDING";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getExportDate() { return exportDate; }
    public void setExportDate(LocalDateTime exportDate) { this.exportDate = exportDate; }

    public String getExportType() { return exportType; }
    public void setExportType(String exportType) { this.exportType = exportType; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFilterCriteria() { return filterCriteria; }
    public void setFilterCriteria(String filterCriteria) { this.filterCriteria = filterCriteria; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
}