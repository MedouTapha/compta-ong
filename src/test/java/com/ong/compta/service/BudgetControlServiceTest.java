package com.ong.compta.service;

import com.ong.compta.domain.*;
import com.ong.compta.domain.enums.PerimetreType;
import com.ong.compta.domain.enums.TypeOperation;
import com.ong.compta.repository.LigneEcritureRepository;
import com.ong.compta.service.exception.BudgetDepasseException;
import com.ong.compta.testsupport.Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetControlServiceTest {

    @Mock
    private LigneEcritureRepository ligneEcritureRepository;

    @InjectMocks
    private BudgetControlService budgetControlService;

    private Financement financement;
    private LigneBudget ligneBudget;
    private Compte compteCharge;
    private Compte caisse;
    private Destination destination;

    @BeforeEach
    void setUp() {
        financement = Fixtures.financement("B01");
        ligneBudget = Fixtures.ligneBudget(financement, new BigDecimal("1000.00"));
        compteCharge = Fixtures.compteCharge();
        caisse = Fixtures.compteCaisse();
        destination = Fixtures.destination("AID");
    }

    private Ecriture ecritureAvecMontant(BigDecimal montant) {
        Ecriture ecriture = new Ecriture(PerimetreType.PROJET, financement, LocalDate.of(2026, 3, 1),
                "Decaissement", "PIECE-1", TypeOperation.DECAISSEMENT, "comptable1");
        ecriture.ajouterLigne(new LigneEcriture(compteCharge, montant, BigDecimal.ZERO, destination, ligneBudget));
        ecriture.ajouterLigne(new LigneEcriture(caisse, BigDecimal.ZERO, montant, null, null));
        return ecriture;
    }

    @Test
    void depense_sous_80_pourcent_ne_leve_pas_exception() {
        when(ligneEcritureRepository.montantEngageParLigneBudget(ligneBudget)).thenReturn(BigDecimal.ZERO);
        assertDoesNotThrow(() -> budgetControlService.verifierSeuils(ecritureAvecMontant(new BigDecimal("500.00"))));
    }

    @Test
    void depense_entre_80_et_100_pourcent_est_toleree_mais_signalee() {
        when(ligneEcritureRepository.montantEngageParLigneBudget(ligneBudget)).thenReturn(BigDecimal.ZERO);
        Ecriture ecriture = ecritureAvecMontant(new BigDecimal("850.00"));
        assertDoesNotThrow(() -> budgetControlService.verifierSeuils(ecriture));
        assertThat(budgetControlService.suivi(ligneBudget).depasseAlerte()).isFalse(); // rien encore valide
    }

    @Test
    void depense_depassant_100_pourcent_leve_exception() {
        when(ligneEcritureRepository.montantEngageParLigneBudget(ligneBudget)).thenReturn(new BigDecimal("900.00"));
        Ecriture ecriture = ecritureAvecMontant(new BigDecimal("200.00"));
        assertThrows(BudgetDepasseException.class, () -> budgetControlService.verifierSeuils(ecriture));
    }

    @Test
    void depassement_debloque_par_directeur_ne_leve_plus_exception() {
        org.mockito.Mockito.lenient().when(ligneEcritureRepository.montantEngageParLigneBudget(ligneBudget))
                .thenReturn(new BigDecimal("900.00"));
        Ecriture ecriture = ecritureAvecMontant(new BigDecimal("200.00"));
        ecriture.debloquerBudget("directeur1");
        assertDoesNotThrow(() -> budgetControlService.verifierSeuils(ecriture));
    }
}
