package com.ong.compta.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ligne_ecriture")
public class LigneEcriture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ecriture_id", nullable = false)
    private Ecriture ecriture;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compte_id", nullable = false)
    private Compte compte;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal debit = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal credit = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private Destination destination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ligne_budget_id")
    private LigneBudget ligneBudget;

    @Column(nullable = false)
    private boolean pointe = false;

    protected LigneEcriture() {
    }

    public LigneEcriture(Compte compte, BigDecimal debit, BigDecimal credit, Destination destination,
                          LigneBudget ligneBudget) {
        this.compte = compte;
        this.debit = debit.setScale(2, java.math.RoundingMode.HALF_UP);
        this.credit = credit.setScale(2, java.math.RoundingMode.HALF_UP);
        this.destination = destination;
        this.ligneBudget = ligneBudget;
    }

    public static LigneEcriture debit(Compte compte, BigDecimal montant, Destination destination, LigneBudget ligneBudget) {
        return new LigneEcriture(compte, montant, BigDecimal.ZERO, destination, ligneBudget);
    }

    public static LigneEcriture credit(Compte compte, BigDecimal montant, Destination destination, LigneBudget ligneBudget) {
        return new LigneEcriture(compte, BigDecimal.ZERO, montant, destination, ligneBudget);
    }

    public Long getId() {
        return id;
    }

    public Ecriture getEcriture() {
        return ecriture;
    }

    public void setEcriture(Ecriture ecriture) {
        this.ecriture = ecriture;
    }

    public Compte getCompte() {
        return compte;
    }

    public BigDecimal getDebit() {
        return debit;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public Destination getDestination() {
        return destination;
    }

    public LigneBudget getLigneBudget() {
        return ligneBudget;
    }

    public boolean isPointe() {
        return pointe;
    }

    public void setPointe(boolean pointe) {
        this.pointe = pointe;
    }
}
