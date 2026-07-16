package com.ong.compta.domain;

import com.ong.compta.domain.enums.PerimetreType;
import jakarta.persistence.*;

@Entity
@Table(name = "compteur_ecriture")
public class CompteurEcriture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "perimetre_type", nullable = false)
    private PerimetreType perimetreType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financement_id")
    private Financement financement;

    @Column(name = "dernier_numero", nullable = false)
    private int dernierNumero = 0;

    protected CompteurEcriture() {
    }

    public CompteurEcriture(PerimetreType perimetreType, Financement financement) {
        this.perimetreType = perimetreType;
        this.financement = financement;
        this.dernierNumero = 0;
    }

    public int incrementerEtObtenir() {
        this.dernierNumero += 1;
        return this.dernierNumero;
    }

    public Long getId() {
        return id;
    }

    public PerimetreType getPerimetreType() {
        return perimetreType;
    }

    public Financement getFinancement() {
        return financement;
    }

    public int getDernierNumero() {
        return dernierNumero;
    }
}
