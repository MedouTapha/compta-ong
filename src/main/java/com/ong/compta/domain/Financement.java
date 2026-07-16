package com.ong.compta.domain;

import com.ong.compta.domain.enums.StatutFinancement;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "financement")
public class Financement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String libelle;

    private String bailleur;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutFinancement statut;

    protected Financement() {
    }

    public Financement(String code, String libelle, String bailleur, LocalDate dateDebut,
                        LocalDate dateFin, StatutFinancement statut) {
        this.code = code;
        this.libelle = libelle;
        this.bailleur = bailleur;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getBailleur() {
        return bailleur;
    }

    public void setBailleur(String bailleur) {
        this.bailleur = bailleur;
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

    public StatutFinancement getStatut() {
        return statut;
    }

    public void setStatut(StatutFinancement statut) {
        this.statut = statut;
    }
}
