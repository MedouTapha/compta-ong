package com.ong.compta.domain;

import com.ong.compta.domain.enums.StatutAide;
import com.ong.compta.domain.enums.StatutFinancement;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AideTest {

    private final Tuteur tuteur = new Tuteur("Tuteur", null, null, null);
    private final Enfant enfant = new Enfant("Enfant", LocalDate.of(2015, 1, 1), "M", tuteur,
            LocalDate.of(2025, 1, 1), com.ong.compta.domain.enums.StatutEnfant.ACTIF, null);
    private final Financement financement = new Financement("FIN01", "Test", "Bailleur",
            LocalDate.of(2025, 1, 1), null, StatutFinancement.ACTIF);

    private Aide aideActive(LocalDate debut, LocalDate fin) {
        return new Aide(enfant, new BigDecimal("50000"), financement, "DEC-001", debut, fin, StatutAide.ACTIVE);
    }

    @Test
    void aide_active_couvrant_le_mois_retourne_true() {
        Aide aide = aideActive(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        assertThat(aide.estActivePour(2026, 6)).isTrue();
    }

    @Test
    void aide_suspendue_retourne_false() {
        Aide aide = new Aide(enfant, new BigDecimal("50000"), financement, "DEC-001",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), StatutAide.SUSPENDUE);
        assertThat(aide.estActivePour(2026, 6)).isFalse();
    }

    @Test
    void aide_active_hors_periode_retourne_false() {
        Aide aide = aideActive(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30));
        assertThat(aide.estActivePour(2026, 1)).isFalse();
        assertThat(aide.estActivePour(2026, 7)).isFalse();
    }

    @Test
    void aide_sans_date_fin_active_indefiniment() {
        Aide aide = aideActive(LocalDate.of(2026, 1, 1), null);
        assertThat(aide.estActivePour(2030, 12)).isTrue();
    }

    @Test
    void aide_active_pour_mois_debut_et_mois_fin() {
        Aide aide = aideActive(LocalDate.of(2026, 3, 15), LocalDate.of(2026, 6, 10));
        assertThat(aide.estActivePour(2026, 3)).isTrue();
        assertThat(aide.estActivePour(2026, 6)).isTrue();
    }
}
