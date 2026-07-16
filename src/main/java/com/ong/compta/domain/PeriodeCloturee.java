package com.ong.compta.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "periode_cloturee")
public class PeriodeCloturee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int annee;

    @Column(nullable = false)
    private int mois;

    @Column(name = "cloture_par", nullable = false)
    private String cloturePar;

    @Column(name = "cloture_le", nullable = false)
    private LocalDateTime clotureLe;

    protected PeriodeCloturee() {
    }

    public PeriodeCloturee(int annee, int mois, String cloturePar) {
        this.annee = annee;
        this.mois = mois;
        this.cloturePar = cloturePar;
        this.clotureLe = LocalDateTime.now();
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

    public String getCloturePar() {
        return cloturePar;
    }

    public LocalDateTime getClotureLe() {
        return clotureLe;
    }
}
