package com.ong.compta.integration;

import com.ong.compta.domain.*;
import com.ong.compta.domain.enums.*;
import com.ong.compta.dto.LigneSaisie;
import com.ong.compta.repository.*;
import com.ong.compta.service.EcritureService;
import com.ong.compta.service.TresorerieService;
import com.ong.compta.service.exception.BudgetDepasseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Scenario e2e §6.4 : depassement de budget — un decaissement a 100% de la ligne
 * de budget est bloque, puis un directeur debloque et valide.
 */
class ScenarioDepassementBudgetTest extends AbstractIntegrationTest {

    @Autowired private EcritureService ecritureService;
    @Autowired private TresorerieService tresorerieService;
    @Autowired private CompteRepository compteRepository;
    @Autowired private FinancementRepository financementRepository;
    @Autowired private DestinationRepository destinationRepository;
    @Autowired private LigneBudgetRepository ligneBudgetRepository;

    private Compte caisse, compteCharge;
    private Financement financement;
    private Destination sante;
    private LigneBudget ligneBudget;

    @BeforeEach
    void setUp() {
        caisse = compteRepository.findByNumero("5711")
                .orElseGet(() -> compteRepository.save(new Compte("5711", "Caisse", true, null)));
        compteCharge = compteRepository.findByNumero("6031")
                .orElseGet(() -> compteRepository.save(new Compte("6031", "Achats vivres", false, null)));
        var compteProduit = compteRepository.findByNumero("7411")
                .orElseGet(() -> compteRepository.save(new Compte("7411", "Subvention", false, null)));

        financement = financementRepository.findByCode("BUD-TEST")
                .orElseGet(() -> financementRepository.save(new Financement("BUD-TEST", "Budget test",
                        "Bailleur", LocalDate.of(2026, 1, 1), null, StatutFinancement.ACTIF)));

        sante = destinationRepository.findByCode("SANTE")
                .orElseGet(() -> destinationRepository.save(new Destination("SANTE", "Sante")));

        ligneBudget = ligneBudgetRepository.save(new LigneBudget(financement, "Budget vivres test",
                new BigDecimal("100000"), 2026));

        // Encaisser pour avoir du solde en caisse
        List<LigneSaisie> lignesEnc = List.of(
                LigneSaisie.debit(caisse, new BigDecimal("500000"), null, null),
                LigneSaisie.credit(compteProduit, new BigDecimal("500000"), sante, null));
        Ecriture enc = ecritureService.creerBrouillon(PerimetreType.PROJET, financement,
                LocalDate.of(2026, 6, 1), "Encaissement initial", "REC-BUD-001",
                TypeOperation.ENCAISSEMENT, "comptable", lignesEnc);
        ecritureService.valider(enc.getId(), "comptable");
    }

    @Test
    @Transactional
    void scenario_depassement_budget_bloque_puis_directeur_debloque() {
        // 1. Decaissement a 100% du budget => bloque
        Ecriture dec = tresorerieService.decaisser(PerimetreType.PROJET, financement,
                LocalDate.of(2026, 6, 10), compteCharge, caisse, sante, ligneBudget,
                new BigDecimal("100000"), "Achat a 100% budget", "FAC-BUD-001", "comptable");

        assertThatThrownBy(() -> ecritureService.valider(dec.getId(), "comptable"))
                .isInstanceOf(BudgetDepasseException.class);

        // 2. Le directeur debloque le budget et valide
        Ecriture validee = ecritureService.debloquerBudgetEtValider(dec.getId(), "directeur");
        assertThat(validee.getStatut()).isEqualTo(StatutEcriture.VALIDEE);
        assertThat(validee.isBudgetDebloque()).isTrue();
    }
}
