package com.ong.compta.dto;

import com.ong.compta.domain.LigneBudget;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record SuiviBudgetaire(LigneBudget ligneBudget, BigDecimal montantEngage, BigDecimal pourcentage) {

    public static SuiviBudgetaire de(LigneBudget ligneBudget, BigDecimal montantEngage) {
        BigDecimal pourcentage = ligneBudget.getMontant().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : montantEngage.multiply(BigDecimal.valueOf(100))
                        .divide(ligneBudget.getMontant(), 1, RoundingMode.HALF_UP);
        return new SuiviBudgetaire(ligneBudget, montantEngage, pourcentage);
    }

    public boolean depasseAlerte() {
        return pourcentage.compareTo(BigDecimal.valueOf(80)) >= 0;
    }

    public boolean depasseLimite() {
        return pourcentage.compareTo(BigDecimal.valueOf(100)) > 0;
    }
}
