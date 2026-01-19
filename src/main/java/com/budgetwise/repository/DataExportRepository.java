package com.budgetwise.repository;

import com.budgetwise.entity.DataExportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DataExportRepository extends JpaRepository<DataExportHistory, Long> {

    List<DataExportHistory> findByUserIdOrderByExportDateDesc(Long userId);

    List<DataExportHistory> findByUserIdAndExportType(Long userId, String exportType);

    List<DataExportHistory> findByUserIdAndFormat(Long userId, String format);

    List<DataExportHistory> findByUserIdAndStatus(Long userId, String status);

    List<DataExportHistory> findByExportDateBeforeAndStatus(LocalDateTime date, String status);

    DataExportHistory findByUserIdAndFileName(Long userId, String fileName);

    Long countByUserId(Long userId);
}