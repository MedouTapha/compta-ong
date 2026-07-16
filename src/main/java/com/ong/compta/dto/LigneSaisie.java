package com.ong.compta.dto;

import com.ong.compta.domain.Compte;
import com.ong.compta.domain.Destination;
import com.ong.compta.domain.LigneBudget;

import java.math.BigDecimal;

/**
 * Saisie d'une ligne d'ecriture avant persistance (une seule des deux valeurs debit/credit
 * doit etre non nulle).
 */
public record LigneSaisie(Compte compte, BigDecimal debit, BigDecimal credit, Destination destination,
                           LigneBudget ligneBudget) {

    public LigneSaisie {
        debit = debit == null ? BigDecimal.ZERO : debit;
        credit = credit == null ? BigDecimal.ZERO : credit;
    }

    public static LigneSaisie debit(Compte compte, BigDecimal montant, Destination destination, LigneBudget ligneBudget) {
        return new LigneSaisie(compte, montant, BigDecimal.ZERO, destination, ligneBudget);
    }

    public static LigneSaisie credit(Compte compte, BigDecimal montant, Destination destination, LigneBudget ligneBudget) {
        return new LigneSaisie(compte, BigDecimal.ZERO, montant, destination, ligneBudget);
    }
}
