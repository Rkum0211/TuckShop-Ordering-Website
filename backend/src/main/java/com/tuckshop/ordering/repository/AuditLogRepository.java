package com.tuckshop.ordering.repository;

import com.tuckshop.ordering.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:search IS NULL OR LOWER(a.description) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.adminUsername) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<AuditLog> findFilteredLogs(@Param("action") String action, @Param("search") String search, Pageable pageable);
}
