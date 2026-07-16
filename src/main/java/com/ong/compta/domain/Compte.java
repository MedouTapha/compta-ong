package com.ong.compta.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "compte")
public class Compte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    @Column(nullable = false)
    private String libelle;

    @Column(name = "est_tresorerie", nullable = false)
    private boolean estTresorerie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_dedie_id")
    private Financement projetDedie;

    @Column(nullable = false)
    private boolean actif = true;

    protected Compte() {
    }

    public Compte(String numero, String libelle, boolean estTresorerie, Financement projetDedie) {
        this.numero = numero;
        this.libelle = libelle;
        this.estTresorerie = estTresorerie;
        this.projetDedie = projetDedie;
        this.actif = true;
    }

    /**
     * Classe comptable = premier chiffre du numero de compte (ex: 5711 -> classe 5).
     */
    @Transient
    public int getClasse() {
        return Integer.parseInt(numero.substring(0, 1));
    }

    @Transient
    public boolean isDedie() {
        return projetDedie != null;
    }

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public boolean isEstTresorerie() {
        return estTresorerie;
    }

    public void setEstTresorerie(boolean estTresorerie) {
        this.estTresorerie = estTresorerie;
    }

    public Financement getProjetDedie() {
        return projetDedie;
    }

    public void setProjetDedie(Financement projetDedie) {
        this.projetDedie = projetDedie;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
