package com.ong.compta.testsupport;

import com.ong.compta.domain.*;
import com.ong.compta.domain.enums.StatutFinancement;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fabriques d'entites pour les tests unitaires (identifiants attribues par reflexion,
 * comme le ferait Hibernate apres un flush).
 */
public final class Fixtures {

    private static final AtomicLong SEQ = new AtomicLong(1);

    private Fixtures() {
    }

    public static <T> T withId(T entity) {
        ReflectionTestUtils.setField(entity, "id", SEQ.getAndIncrement());
        return entity;
    }

    public static Financement financement(String code) {
        return withId(new Financement(code, "Financement " + code, "Bailleur test",
                LocalDate.of(2025, 1, 1), null, StatutFinancement.ACTIF));
    }

    public static Compte compteCaisse() {
        return withId(new Compte("5711", "Caisse", true, null));
    }

    public static Compte compteBanquePartage() {
        return withId(new Compte("5211", "Banque", true, null));
    }

    public static Compte compteBanqueDedie(Financement financement) {
        return withId(new Compte("5212", "Banque dediee " + financement.getCode(), true, financement));
    }

    public static Compte compteCharge() {
        return withId(new Compte("6011", "Achats vivres", false, null));
    }

    public static Compte compteAides() {
        return withId(new Compte("6511", "Aides", false, null));
    }

    public static Compte compteProduit() {
        return withId(new Compte("7411", "Subvention", false, null));
    }

    public static Destination destination(String code) {
        return withId(new Destination(code, "Destination " + code));
    }

    public static LigneBudget ligneBudget(Financement financement, BigDecimal montant) {
        return withId(new LigneBudget(financement, "Ligne budget", montant, 2026));
    }
}
