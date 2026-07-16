package com.ong.compta.repository;

import com.ong.compta.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntiteAndEntiteId(String entite, Long entiteId);
}
