package com.ong.compta.service;

import com.ong.compta.domain.AuditLog;
import com.ong.compta.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

/**
 * RG-7 : toute operation sensible (validation d'ecriture, cloture de liste de paiement, ...)
 * doit laisser une trace dans le journal d'audit.
 */
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void enregistrer(String utilisateur, String action, String entite, Long entiteId, String details) {
        auditLogRepository.save(new AuditLog(utilisateur, action, entite, entiteId, details));
    }
}
