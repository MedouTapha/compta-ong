package com.ong.compta.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String utilisateur;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String entite;

    @Column(name = "entite_id")
    private Long entiteId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false)
    private LocalDateTime horodatage;

    protected AuditLog() {
    }

    public AuditLog(String utilisateur, String action, String entite, Long entiteId, String details) {
        this.utilisateur = utilisateur;
        this.action = action;
        this.entite = entite;
        this.entiteId = entiteId;
        this.details = details;
        this.horodatage = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getUtilisateur() {
        return utilisateur;
    }

    public String getAction() {
        return action;
    }

    public String getEntite() {
        return entite;
    }

    public Long getEntiteId() {
        return entiteId;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getHorodatage() {
        return horodatage;
    }
}
