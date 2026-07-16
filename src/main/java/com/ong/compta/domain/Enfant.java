package com.ong.compta.domain;

import com.ong.compta.domain.enums.StatutEnfant;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "enfant")
public class Enfant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    private String sexe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tuteur_id", nullable = false)
    private Tuteur tuteur;

    @Column(name = "date_entree", nullable = false)
    private LocalDate dateEntree;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutEnfant statut;

    private String notes;

    protected Enfant() {
    }

    public Enfant(String nom, LocalDate dateNaissance, String sexe, Tuteur tuteur, LocalDate dateEntree,
                  StatutEnfant statut, String notes) {
        this.nom = nom;
        this.dateNaissance = dateNaissance;
        this.sexe = sexe;
        this.tuteur = tuteur;
        this.dateEntree = dateEntree;
        this.statut = statut;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public Tuteur getTuteur() {
        return tuteur;
    }

    public void setTuteur(Tuteur tuteur) {
        this.tuteur = tuteur;
    }

    public LocalDate getDateEntree() {
        return dateEntree;
    }

    public void setDateEntree(LocalDate dateEntree) {
        this.dateEntree = dateEntree;
    }

    public StatutEnfant getStatut() {
        return statut;
    }

    public void setStatut(StatutEnfant statut) {
        this.statut = statut;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
