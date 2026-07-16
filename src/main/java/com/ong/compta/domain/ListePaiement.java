package com.ong.compta.domain;

import com.ong.compta.domain.enums.StatutListePaiement;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "liste_paiement")
public class ListePaiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int annee;

    @Column(nullable = false)
    private int mois;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutListePaiement statut;

    @Column(name = "generee_par", nullable = false)
    private String genereePar;

    @Column(name = "validee_par")
    private String valideePar;

    @Column(name = "cloturee_par")
    private String clotureePar;

    @OneToMany(mappedBy = "liste", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LignePaiement> lignes = new ArrayList<>();

    protected ListePaiement() {
    }

    public ListePaiement(int annee, int mois, String genereePar) {
        this.annee = annee;
        this.mois = mois;
        this.statut = StatutListePaiement.GENEREE;
        this.genereePar = genereePar;
    }

    public void ajouterLigne(LignePaiement ligne) {
        ligne.setListe(this);
        this.lignes.add(ligne);
    }

    public Long getId() {
        return id;
    }

    public int getAnnee() {
        return annee;
    }

    public int getMois() {
        return mois;
    }

    public StatutListePaiement getStatut() {
        return statut;
    }

    public void setStatut(StatutListePaiement statut) {
        this.statut = statut;
    }

    public String getGenereePar() {
        return genereePar;
    }

    public String getValideePar() {
        return valideePar;
    }

    public void setValideePar(String valideePar) {
        this.valideePar = valideePar;
    }

    public String getClotureePar() {
        return clotureePar;
    }

    public void setClotureePar(String clotureePar) {
        this.clotureePar = clotureePar;
    }

    public List<LignePaiement> getLignes() {
        return lignes;
    }
}
