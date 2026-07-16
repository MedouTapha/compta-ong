package com.ong.compta.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ligne_budget")
public class LigneBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "financement_id", nullable = false)
    private Financement financement;

    @Column(nullable = false)
    private String libelle;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false)
    private int exercice;

    protected LigneBudget() {
    }

    public LigneBudget(Financement financement, String libelle, BigDecimal montant, int exercice) {
        this.financement = financement;
        this.libelle = libelle;
        this.montant = montant;
        this.exercice = exercice;
    }

    public Long getId() {
        return id;
    }

    public Financement getFinancement() {
        return financement;
    }

    public void setFinancement(Financement financement) {
        this.financement = financement;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public int getExercice() {
        return exercice;
    }

    public void setExercice(int exercice) {
        this.exercice = exercice;
    }
}
