package com.ong.compta.domain;

import com.ong.compta.domain.enums.StatutAide;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Entity
@Table(name = "aide")
public class Aide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enfant_id", nullable = false)
    private Enfant enfant;

    @Column(name = "montant_mensuel", nullable = false, precision = 14, scale = 2)
    private BigDecimal montantMensuel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "financement_id", nullable = false)
    private Financement financement;

    @Column(name = "reference_decision", nullable = false)
    private String referenceDecision;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutAide statut;

    protected Aide() {
    }

    public Aide(Enfant enfant, BigDecimal montantMensuel, Financement financement, String referenceDecision,
                LocalDate dateDebut, LocalDate dateFin, StatutAide statut) {
        this.enfant = enfant;
        this.montantMensuel = montantMensuel;
        this.financement = financement;
        this.referenceDecision = referenceDecision;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
    }

    /**
     * Une aide est active pour un mois donne si son statut est ACTIVE et que
     * la periode [dateDebut, dateFin] recouvre ce mois (dateFin nulle = sans fin).
     */
    public boolean estActivePour(int annee, int mois) {
        if (statut != StatutAide.ACTIVE) {
            return false;
        }
        YearMonth cible = YearMonth.of(annee, mois);
        YearMonth debutMois = YearMonth.from(dateDebut);
        if (cible.isBefore(debutMois)) {
            return false;
        }
        if (dateFin != null) {
            YearMonth finMois = YearMonth.from(dateFin);
            if (cible.isAfter(finMois)) {
                return false;
            }
        }
        return true;
    }

    public Long getId() {
        return id;
    }

    public Enfant getEnfant() {
        return enfant;
    }

    public void setEnfant(Enfant enfant) {
        this.enfant = enfant;
    }

    public BigDecimal getMontantMensuel() {
        return montantMensuel;
    }

    public void setMontantMensuel(BigDecimal montantMensuel) {
        this.montantMensuel = montantMensuel;
    }

    public Financement getFinancement() {
        return financement;
    }

    public void setFinancement(Financement financement) {
        this.financement = financement;
    }

    public String getReferenceDecision() {
        return referenceDecision;
    }

    public void setReferenceDecision(String referenceDecision) {
        this.referenceDecision = referenceDecision;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public StatutAide getStatut() {
        return statut;
    }

    public void setStatut(StatutAide statut) {
        this.statut = statut;
    }
}
