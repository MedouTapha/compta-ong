package com.ong.compta.integration;

import com.ong.compta.domain.Compte;
import com.ong.compta.domain.Ecriture;
import com.ong.compta.domain.Financement;
import com.ong.compta.domain.enums.*;
import com.ong.compta.repository.CompteRepository;
import com.ong.compta.repository.DestinationRepository;
import com.ong.compta.repository.FinancementRepository;
import com.ong.compta.service.EcritureService;
import com.ong.compta.service.TresorerieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Scenario e2e §6.4 : correction d'erreur par contre-passation (RG-2).
 */
class ScenarioCorrectionErreurTest extends AbstractIntegrationTest {

    @Autowired private EcritureService ecritureService;
    @Autowired private TresorerieService tresorerieService;
    @Autowired private CompteRepository compteRepository;
    @Autowired private FinancementRepository financementRepository;
    @Autowired private DestinationRepository destinationRepository;

    private Compte caisse, compteProduit;
    private Financement financement;

    @BeforeEach
    void setUp() {
        caisse = compteRepository.findByNumero("5711")
                .orElseGet(() -> compteRepository.save(new Compte("5711", "Caisse", true, null)));
        compteProduit = compteRepository.findByNumero("7411")
                .orElseGet(() -> compteRepository.save(new Compte("7411", "Subvention", false, null)));
        financement = financementRepository.findByCode("ERR-TEST")
                .orElseGet(() -> financementRepository.save(new Financement("ERR-TEST", "Correction test",
                        "Bailleur", LocalDate.of(2026, 1, 1), null, StatutFinancement.ACTIF)));
        destinationRepository.findByCode("SANTE")
                .orElseGet(() -> destinationRepository.save(new com.ong.compta.domain.Destination("SANTE", "Sante")));
    }

    @Test
    @Transactional
    void scenario_correction_erreur_par_contrepassation() {
        var sante = destinationRepository.findByCode("SANTE").orElseThrow();

        // 1. Creer et valider une ecriture avec un montant errone
        Ecriture erreur = tresorerieService.encaisser(PerimetreType.PROJET, financement,
                LocalDate.of(2026, 6, 10), caisse, compteProduit, sante,
                new BigDecimal("500000"), "Subvention erronee", "REC-ERR-001", "comptable");
        erreur = ecritureService.valider(erreur.getId(), "comptable");
        assertThat(erreur.getStatut()).isEqualTo(StatutEcriture.VALIDEE);

        // 2. Contre-passer (RG-2 : intangibilite)
        Ecriture contrepassation = ecritureService.contrePasser(erreur.getId(), "comptable",
                LocalDate.of(2026, 6, 10));
        assertThat(contrepassation.getStatut()).isEqualTo(StatutEcriture.VALIDEE);
        assertThat(contrepassation.getTypeOperation()).isEqualTo(TypeOperation.CONTREPASSATION);

        // L'ecriture originale est marquee CONTREPASSEE
        Ecriture origine = ecritureService.trouverParId(erreur.getId());
        assertThat(origine.getStatut()).isEqualTo(StatutEcriture.CONTREPASSEE);

        // 3. Saisir la bonne ecriture
        Ecriture correcte = tresorerieService.encaisser(PerimetreType.PROJET, financement,
                LocalDate.of(2026, 6, 10), caisse, compteProduit, sante,
                new BigDecimal("300000"), "Subvention correcte", "REC-COR-001", "comptable");
        correcte = ecritureService.valider(correcte.getId(), "comptable");
        assertThat(correcte.getStatut()).isEqualTo(StatutEcriture.VALIDEE);
    }
}
