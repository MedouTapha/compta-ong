package com.ong.compta.integration;

import com.ong.compta.domain.*;
import com.ong.compta.domain.enums.*;
import com.ong.compta.dto.LigneSaisie;
import com.ong.compta.repository.*;
import com.ong.compta.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Scenario e2e §6.4 : mois complet — encaissement d'une subvention, achat de vivres,
 * generation/validation/cloture de la liste de paiement, verification journal et balance.
 */
class ScenarioMoisCompletTest extends AbstractIntegrationTest {

    @Autowired private EcritureService ecritureService;
    @Autowired private TresorerieService tresorerieService;
    @Autowired private ListePaiementService listePaiementService;
    @Autowired private AideService aideService;
    @Autowired private EnfantService enfantService;
    @Autowired private TuteurService tuteurService;
    @Autowired private EtatService etatService;
    @Autowired private CompteRepository compteRepository;
    @Autowired private FinancementRepository financementRepository;
    @Autowired private DestinationRepository destinationRepository;
    @Autowired private LigneBudgetRepository ligneBudgetRepository;

    private Compte caisse, banque, compteCharge, compteAides, compteProduit;
    private Financement financement;
    private Destination sante;
    private LigneBudget ligneBudget;

    @BeforeEach
    void setUp() {
        caisse = compteRepository.findByNumero("5711")
                .orElseGet(() -> compteRepository.save(new Compte("5711", "Caisse", true, null)));
        banque = compteRepository.findByNumero("5211")
                .orElseGet(() -> compteRepository.save(new Compte("5211", "Banque", true, null)));
        compteCharge = compteRepository.findByNumero("6031")
                .orElseGet(() -> compteRepository.save(new Compte("6031", "Achats vivres", false, null)));
        compteAides = compteRepository.findByNumero("6511")
                .orElseGet(() -> compteRepository.save(new Compte("6511", "Aides", false, null)));
        compteProduit = compteRepository.findByNumero("7411")
                .orElseGet(() -> compteRepository.save(new Compte("7411", "Subvention", false, null)));

        financement = financementRepository.findByCode("E2E-TEST")
                .orElseGet(() -> financementRepository.save(new Financement("E2E-TEST", "E2E Project",
                        "UNICEF", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), StatutFinancement.ACTIF)));

        sante = destinationRepository.findByCode("SANTE")
                .orElseGet(() -> destinationRepository.save(new Destination("SANTE", "Sante")));

        ligneBudget = ligneBudgetRepository.save(new LigneBudget(financement, "Vivres E2E", new BigDecimal("5000000"), 2026));
    }

    @Test
    @Transactional
    void scenario_mois_complet() {
        // 1. Encaissement subvention
        Ecriture encaissement = tresorerieService.encaisser(PerimetreType.PROJET, financement,
                LocalDate.of(2026, 6, 1), caisse, compteProduit, sante,
                new BigDecimal("1000000"), "Subvention UNICEF juin", "REC-E2E-001", "comptable");
        encaissement = ecritureService.valider(encaissement.getId(), "comptable");
        assertThat(encaissement.getStatut()).isEqualTo(StatutEcriture.VALIDEE);
        assertThat(encaissement.getNumero()).isPositive();

        // 2. Achat de vivres
        Ecriture decaissement = tresorerieService.decaisser(PerimetreType.PROJET, financement,
                LocalDate.of(2026, 6, 5), compteCharge, caisse, sante, ligneBudget,
                new BigDecimal("200000"), "Achat vivres juin", "FAC-E2E-001", "comptable");
        decaissement = ecritureService.valider(decaissement.getId(), "comptable");
        assertThat(decaissement.getStatut()).isEqualTo(StatutEcriture.VALIDEE);

        // 3. Creer un enfant + aide pour la liste de paiement
        Tuteur tuteur = tuteurService.creer("Diallo E2E", "CNI", "E2E-001", "70000000");
        Enfant enfant = enfantService.creer("Ibrahim E2E", LocalDate.of(2015, 5, 1), "M",
                tuteur, LocalDate.of(2025, 9, 1), null);
        Aide aide = aideService.creer(enfant, new BigDecimal("50000"), financement,
                "DEC-E2E-001", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        // 4. Generer la liste de paiement
        ListePaiement liste = listePaiementService.generer(2026, 6, "comptable");
        assertThat(liste.getStatut()).isEqualTo(StatutListePaiement.GENEREE);
        assertThat(liste.getLignes()).hasSize(1);

        // 5. Valider la liste (role DIRECTEUR)
        liste = listePaiementService.valider(liste.getId(), "directeur");
        assertThat(liste.getStatut()).isEqualTo(StatutListePaiement.VALIDEE);

        // 6. Marquer la ligne comme versee
        LignePaiement ligne = liste.getLignes().get(0);
        listePaiementService.marquerLigneVersee(ligne.getId(), LocalDate.of(2026, 6, 15), "DECH-001");

        // 7. Cloturer la liste (genere une ecriture comptable)
        liste = listePaiementService.cloturer(liste.getId(), "comptable");
        assertThat(liste.getStatut()).isEqualTo(StatutListePaiement.CLOTUREE);

        // 8. Verifier le journal
        var journal = etatService.journal(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));
        assertThat(journal).hasSizeGreaterThanOrEqualTo(3);

        // 9. Verifier la balance
        var balance = etatService.balance(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));
        assertThat(balance).isNotEmpty();
    }
}
