package com.ong.compta.service;

import com.ong.compta.domain.Compte;
import com.ong.compta.domain.Destination;
import com.ong.compta.domain.Ecriture;
import com.ong.compta.domain.Financement;
import com.ong.compta.domain.LigneBudget;
import com.ong.compta.domain.enums.PerimetreType;
import com.ong.compta.domain.enums.TypeOperation;
import com.ong.compta.dto.LigneSaisie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Orchestre la saisie des operations de tresorerie courantes (US-3.1 a US-3.3) en
 * s'appuyant exclusivement sur EcritureService pour l'application des regles de gestion.
 */
@Service
public class TresorerieService {

    private final EcritureService ecritureService;

    public TresorerieService(EcritureService ecritureService) {
        this.ecritureService = ecritureService;
    }

    @Transactional
    public Ecriture encaisser(PerimetreType perimetreType, Financement financement, LocalDate dateOperation,
                               Compte compteTresorerie, Compte compteProduit, Destination destination,
                               BigDecimal montant, String libelle, String referencePiece, String creePar) {
        List<LigneSaisie> lignes = List.of(
                LigneSaisie.debit(compteTresorerie, montant, null, null),
                LigneSaisie.credit(compteProduit, montant, destination, null));
        return ecritureService.creerBrouillon(perimetreType, financement, dateOperation, libelle, referencePiece,
                TypeOperation.ENCAISSEMENT, creePar, lignes);
    }

    @Transactional
    public Ecriture decaisser(PerimetreType perimetreType, Financement financement, LocalDate dateOperation,
                               Compte compteCharge, Compte compteTresorerie, Destination destination,
                               LigneBudget ligneBudget, BigDecimal montant, String libelle, String referencePiece,
                               String creePar) {
        List<LigneSaisie> lignes = List.of(
                LigneSaisie.debit(compteCharge, montant, destination, ligneBudget),
                LigneSaisie.credit(compteTresorerie, montant, null, null));
        return ecritureService.creerBrouillon(perimetreType, financement, dateOperation, libelle, referencePiece,
                TypeOperation.DECAISSEMENT, creePar, lignes);
    }

    @Transactional
    public Ecriture transferer(PerimetreType perimetreType, Financement financement, LocalDate dateOperation,
                                Compte compteSource, Compte compteDestination, BigDecimal montant, String libelle,
                                String referencePiece, String creePar) {
        List<LigneSaisie> lignes = List.of(
                LigneSaisie.debit(compteDestination, montant, null, null),
                LigneSaisie.credit(compteSource, montant, null, null));
        return ecritureService.creerBrouillon(perimetreType, financement, dateOperation, libelle, referencePiece,
                TypeOperation.TRANSFERT, creePar, lignes);
    }
}
