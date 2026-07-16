package com.ong.compta.integration;

import com.ong.compta.domain.*;
import com.ong.compta.domain.enums.*;
import com.ong.compta.dto.LigneSaisie;
import com.ong.compta.repository.*;
import com.ong.compta.service.EcritureService;
import com.ong.compta.service.exception.EcritureDesequilibreeException;
import com.ong.compta.service.exception.PieceJustificativeManquanteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EcritureIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EcritureService ecritureService;

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private FinancementRepository financementRepository;

    @Autowired
    private DestinationRepository destinationRepository;

    @Autowired
    private CompteurEcritureRepository compteurRepository;

    private Compte caisse;
    private Compte subvention;
    private Destination sante;
    private Financement financement;

    @BeforeEach
    void setUp() {
        caisse = compteRepository.findByNumero("5711")
                .orElseGet(() -> compteRepository.save(new Compte("5711", "Caisse", true, null)));
        subvention = compteRepository.findByNumero("7411")
                .orElseGet(() -> compteRepository.save(new Compte("7411", "Subvention", false, null)));
        sante = destinationRepository.findByCode("SANTE")
                .orElseGet(() -> destinationRepository.save(new Destination("SANTE", "Sante")));
        financement = financementRepository.findByCode("TEST-INT")
                .orElseGet(() -> financementRepository.save(new Financement("TEST-INT", "Integration",
                        "Bailleur", LocalDate.of(2026, 1, 1), null, StatutFinancement.ACTIF)));
    }

    @Test
    @Transactional
    void creer_brouillon_et_valider_genere_numero() {
        List<LigneSaisie> lignes = List.of(
                LigneSaisie.debit(caisse, new BigDecimal("100000"), null, null),
                LigneSaisie.credit(subvention, new BigDecimal("100000"), sante, null));

        Ecriture brouillon = ecritureService.creerBrouillon(PerimetreType.PROJET, financement,
                LocalDate.of(2026, 6, 15), "Subvention recue", "REC-001",
                TypeOperation.ENCAISSEMENT, "comptable", lignes);

        assertThat(brouillon.getStatut()).isEqualTo(StatutEcriture.BROUILLON);
        assertThat(brouillon.getNumero()).isNull();

        Ecriture validee = ecritureService.valider(brouillon.getId(), "comptable");
        assertThat(validee.getStatut()).isEqualTo(StatutEcriture.VALIDEE);
        assertThat(validee.getNumero()).isNotNull().isPositive();
    }

    @Test
    void ecriture_desequilibree_leve_exception() {
        List<LigneSaisie> lignes = List.of(
                LigneSaisie.debit(caisse, new BigDecimal("100000"), null, null),
                LigneSaisie.credit(subvention, new BigDecimal("50000"), sante, null));

        assertThatThrownBy(() -> ecritureService.creerBrouillon(PerimetreType.PROJET, financement,
                LocalDate.of(2026, 6, 15), "Test", "REC-002", TypeOperation.ENCAISSEMENT, "comptable", lignes))
                .isInstanceOf(EcritureDesequilibreeException.class);
    }

    @Test
    void piece_justificative_manquante_leve_exception() {
        List<LigneSaisie> lignes = List.of(
                LigneSaisie.debit(caisse, new BigDecimal("100000"), null, null),
                LigneSaisie.credit(subvention, new BigDecimal("100000"), sante, null));

        assertThatThrownBy(() -> ecritureService.creerBrouillon(PerimetreType.PROJET, financement,
                LocalDate.of(2026, 6, 15), "Test", "", TypeOperation.ENCAISSEMENT, "comptable", lignes))
                .isInstanceOf(PieceJustificativeManquanteException.class);
    }
}
