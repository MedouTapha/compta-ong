package com.ong.compta.domain;

import com.ong.compta.domain.enums.PerimetreType;
import com.ong.compta.domain.enums.StatutEcriture;
import com.ong.compta.domain.enums.TypeOperation;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ecriture")
public class Ecriture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "perimetre_type", nullable = false)
    private PerimetreType perimetreType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financement_id")
    private Financement financement;

    @Column(name = "date_operation", nullable = false)
    private LocalDate dateOperation;

    @Column(nullable = false)
    private String libelle;

    @Column(name = "reference_piece", nullable = false)
    private String referencePiece;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_operation", nullable = false)
    private TypeOperation typeOperation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutEcriture statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ecriture_origine_id")
    private Ecriture ecritureOrigine;

    @Column(name = "cree_par", nullable = false)
    private String creePar;

    @Column(name = "valide_par")
    private String validePar;

    @Column(name = "cree_le", nullable = false)
    private LocalDateTime creeLe;

    @Column(name = "valide_le")
    private LocalDateTime valideLe;

    @Version
    private Long version;

    @OneToMany(mappedBy = "ecriture", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LigneEcriture> lignes = new ArrayList<>();

    protected Ecriture() {
    }

    public Ecriture(PerimetreType perimetreType, Financement financement, LocalDate dateOperation,
                     String libelle, String referencePiece, TypeOperation typeOperation, String creePar) {
        this.perimetreType = perimetreType;
        this.financement = financement;
        this.dateOperation = dateOperation;
        this.libelle = libelle;
        this.referencePiece = referencePiece;
        this.typeOperation = typeOperation;
        this.statut = StatutEcriture.BROUILLON;
        this.creePar = creePar;
        this.creeLe = LocalDateTime.now();
    }

    public void ajouterLigne(LigneEcriture ligne) {
        ligne.setEcriture(this);
        this.lignes.add(ligne);
    }

    public Long getId() {
        return id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public PerimetreType getPerimetreType() {
        return perimetreType;
    }

    public Financement getFinancement() {
        return financement;
    }

    public LocalDate getDateOperation() {
        return dateOperation;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getReferencePiece() {
        return referencePiece;
    }

    public TypeOperation getTypeOperation() {
        return typeOperation;
    }

    public StatutEcriture getStatut() {
        return statut;
    }

    public void setStatut(StatutEcriture statut) {
        this.statut = statut;
    }

    public Ecriture getEcritureOrigine() {
        return ecritureOrigine;
    }

    public void setEcritureOrigine(Ecriture ecritureOrigine) {
        this.ecritureOrigine = ecritureOrigine;
    }

    public String getCreePar() {
        return creePar;
    }

    public String getValidePar() {
        return validePar;
    }

    public void setValidePar(String validePar) {
        this.validePar = validePar;
    }

    public LocalDateTime getCreeLe() {
        return creeLe;
    }

    public LocalDateTime getValideLe() {
        return valideLe;
    }

    public void setValideLe(LocalDateTime valideLe) {
        this.valideLe = valideLe;
    }

    public Long getVersion() {
        return version;
    }

    public List<LigneEcriture> getLignes() {
        return lignes;
    }
}
