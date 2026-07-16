package com.ong.compta.domain;

import com.ong.compta.domain.enums.StatutLignePaiement;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ligne_paiement")
public class LignePaiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "liste_id", nullable = false)
    private ListePaiement liste;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aide_id", nullable = false)
    private Aide aide;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutLignePaiement statut;

    @Column(name = "date_versement")
    private LocalDate dateVersement;

    @Column(name = "motif_non_versement")
    private String motifNonVersement;

    @Column(name = "reference_decharge")
    private String referenceDecharge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ecriture_id")
    private Ecriture ecriture;

    protected LignePaiement() {
    }

    public LignePaiement(Aide aide, BigDecimal montant) {
        this.aide = aide;
        this.montant = montant;
        this.statut = StatutLignePaiement.A_PAYER;
    }

    public void marquerVerse(LocalDate dateVersement, String referenceDecharge) {
        this.statut = StatutLignePaiement.VERSE;
        this.dateVersement = dateVersement;
        this.referenceDecharge = referenceDecharge;
        this.motifNonVersement = null;
    }

    public void marquerNonVerse(String motif) {
        this.statut = StatutLignePaiement.NON_VERSE;
        this.motifNonVersement = motif;
        this.dateVersement = null;
    }

    public Long getId() {
        return id;
    }

    public ListePaiement getListe() {
        return liste;
    }

    public void setListe(ListePaiement liste) {
        this.liste = liste;
    }

    public Aide getAide() {
        return aide;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public StatutLignePaiement getStatut() {
        return statut;
    }

    public LocalDate getDateVersement() {
        return dateVersement;
    }

    public String getMotifNonVersement() {
        return motifNonVersement;
    }

    public String getReferenceDecharge() {
        return referenceDecharge;
    }

    public Ecriture getEcriture() {
        return ecriture;
    }

    public void setEcriture(Ecriture ecriture) {
        this.ecriture = ecriture;
    }
}
