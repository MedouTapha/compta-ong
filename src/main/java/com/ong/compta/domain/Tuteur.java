package com.ong.compta.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "tuteur")
public class Tuteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(name = "piece_identite_type")
    private String pieceIdentiteType;

    @Column(name = "piece_identite_numero")
    private String pieceIdentiteNumero;

    private String telephone;

    protected Tuteur() {
    }

    public Tuteur(String nom, String pieceIdentiteType, String pieceIdentiteNumero, String telephone) {
        this.nom = nom;
        this.pieceIdentiteType = pieceIdentiteType;
        this.pieceIdentiteNumero = pieceIdentiteNumero;
        this.telephone = telephone;
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

    public String getPieceIdentiteType() {
        return pieceIdentiteType;
    }

    public void setPieceIdentiteType(String pieceIdentiteType) {
        this.pieceIdentiteType = pieceIdentiteType;
    }

    public String getPieceIdentiteNumero() {
        return pieceIdentiteNumero;
    }

    public void setPieceIdentiteNumero(String pieceIdentiteNumero) {
        this.pieceIdentiteNumero = pieceIdentiteNumero;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
