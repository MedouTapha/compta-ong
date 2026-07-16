package com.ong.compta.service;

import com.ong.compta.domain.*;
import com.ong.compta.domain.enums.PerimetreType;
import com.ong.compta.domain.enums.StatutEcriture;
import com.ong.compta.domain.enums.TypeOperation;
import com.ong.compta.dto.LigneSaisie;
import com.ong.compta.repository.EcritureRepository;
import com.ong.compta.service.exception.*;
import com.ong.compta.testsupport.Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EcritureServiceTest {

    @Mock
    private EcritureRepository ecritureRepository;
    @Mock
    private NumerotationService numerotationService;
    @Mock
    private PeriodeClotureeService periodeClotureeService;
    @Mock
    private SoldeService soldeService;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private EcritureService ecritureService;

    private final AtomicLong idSeq = new AtomicLong(1);
    private final java.util.Map<Long, Ecriture> store = new java.util.HashMap<>();

    private Compte caisse;
    private Compte banquePartage;
    private Compte compteCharge;
    private Compte compteAides;
    private Compte compteProduit;
    private Destination destination;
    private Financement financementB01;

    @BeforeEach
    void setUp() {
        caisse = Fixtures.compteCaisse();
        banquePartage = Fixtures.compteBanquePartage();
        compteCharge = Fixtures.compteCharge();
        compteAides = Fixtures.compteAides();
        compteProduit = Fixtures.compteProduit();
        destination = Fixtures.destination("AID");
        financementB01 = Fixtures.financement("B01");

        lenient().when(ecritureRepository.save(any(Ecriture.class))).thenAnswer(inv -> {
            Ecriture e = inv.getArgument(0);
            if (e.getId() == null) {
                ReflectionTestUtils.setField(e, "id", idSeq.getAndIncrement());
            }
            store.put(e.getId(), e);
            return e;
        });
        lenient().when(ecritureRepository.findById(anyLong()))
                .thenAnswer(inv -> Optional.ofNullable(store.get(inv.getArgument(0))));
        lenient().when(soldeService.soldeDisponible(any(), any(), any())).thenReturn(BigDecimal.ZERO);
    }

    private Ecriture creerBrouillonEtEnregistrer(PerimetreType perimetre, Financement financement,
                                                   String refPiece, List<LigneSaisie> lignes) {
        return ecritureService.creerBrouillon(perimetre, financement, LocalDate.of(2026, 3, 15),
                "Operation test", refPiece, TypeOperation.ENCAISSEMENT, "comptable1", lignes);
    }

    private List<LigneSaisie> lignesEquilibrees() {
        return List.of(
                LigneSaisie.debit(caisse, new BigDecimal("100.00"), null, null),
                LigneSaisie.credit(compteProduit, new BigDecimal("100.00"), destination, null));
    }

    @Nested
    class RG1_Equilibre {

        @Test
        void test_ecriture_equilibree_est_acceptee() {
            Ecriture e = ecritureService.creerBrouillon(PerimetreType.GENERAL, null, LocalDate.of(2026, 3, 15),
                    "Encaissement", "PIECE-1", TypeOperation.ENCAISSEMENT, "comptable1", lignesEquilibrees());
            assertThat(e.getStatut()).isEqualTo(StatutEcriture.BROUILLON);
            assertThat(e.getLignes()).hasSize(2);
        }

        @Test
        void test_ecriture_desequilibree_leve_exception() {
            List<LigneSaisie> lignes = List.of(
                    LigneSaisie.debit(caisse, new BigDecimal("100.00"), null, null),
                    LigneSaisie.credit(compteProduit, new BigDecimal("90.00"), destination, null));
            assertThrows(EcritureDesequilibreeException.class, () ->
                    ecritureService.creerBrouillon(PerimetreType.GENERAL, null, LocalDate.of(2026, 3, 15),
                            "Encaissement", "PIECE-1", TypeOperation.ENCAISSEMENT, "comptable1", lignes));
        }

        @Test
        void test_ecriture_une_seule_ligne_leve_exception() {
            List<LigneSaisie> lignes = List.of(LigneSaisie.debit(caisse, new BigDecimal("100.00"), null, null));
            assertThrows(EcritureIncompleteException.class, () ->
                    ecritureService.creerBrouillon(PerimetreType.GENERAL, null, LocalDate.of(2026, 3, 15),
                            "Encaissement", "PIECE-1", TypeOperation.ENCAISSEMENT, "comptable1", lignes));
        }

        @Test
        void test_ecriture_montants_negatifs_rejetee() {
            List<LigneSaisie> lignes = List.of(
                    LigneSaisie.debit(caisse, new BigDecimal("-100.00"), null, null),
                    LigneSaisie.credit(compteProduit, new BigDecimal("-100.00"), destination, null));
            assertThrows(MontantInvalideException.class, () ->
                    ecritureService.creerBrouillon(PerimetreType.GENERAL, null, LocalDate.of(2026, 3, 15),
                            "Encaissement", "PIECE-1", TypeOperation.ENCAISSEMENT, "comptable1", lignes));
        }
    }

    @Nested
    class RG2_Intangibilite {

        @Test
        void test_contrepasser_ecriture_validee_cree_ecriture_inverse() {
            Ecriture brouillon = creerBrouillonEtEnregistrer(PerimetreType.GENERAL, null, "PIECE-1", lignesEquilibrees());
            when(numerotationService.prochainNumero(PerimetreType.GENERAL, null)).thenReturn(1, 2);
            Ecriture validee = ecritureService.valider(brouillon.getId(), "comptable1");
            assertThat(validee.getStatut()).isEqualTo(StatutEcriture.VALIDEE);

            // le solde de la caisse refleterait desormais les 100 encaisses par l'ecriture d'origine
            when(soldeService.soldeDisponible(caisse, PerimetreType.GENERAL, null)).thenReturn(new BigDecimal("100.00"));

            Ecriture contrepassation = ecritureService.contrePasser(validee.getId(), "comptable1", LocalDate.of(2026, 3, 16));

            assertThat(contrepassation.getStatut()).isEqualTo(StatutEcriture.VALIDEE);
            assertThat(contrepassation.getEcritureOrigine()).isEqualTo(validee);
            assertThat(contrepassation.getTypeOperation()).isEqualTo(TypeOperation.CONTREPASSATION);
            assertThat(validee.getStatut()).isEqualTo(StatutEcriture.CONTREPASSEE);

            // lignes inversees : le debit devient credit et inversement
            LigneEcriture ligneOrigineCaisse = validee.getLignes().stream()
                    .filter(l -> l.getCompte().equals(caisse)).findFirst().orElseThrow();
            LigneEcriture ligneContrepasseeCaisse = contrepassation.getLignes().stream()
                    .filter(l -> l.getCompte().equals(caisse)).findFirst().orElseThrow();
            assertThat(ligneContrepasseeCaisse.getCredit()).isEqualByComparingTo(ligneOrigineCaisse.getDebit());
            assertThat(ligneContrepasseeCaisse.getDebit()).isEqualByComparingTo(ligneOrigineCaisse.getCredit());
        }

        @Test
        void test_contrepasser_ecriture_brouillon_leve_exception() {
            Ecriture brouillon = creerBrouillonEtEnregistrer(PerimetreType.GENERAL, null, "PIECE-1", lignesEquilibrees());
            assertThrows(EtatInvalideException.class, () ->
                    ecritureService.contrePasser(brouillon.getId(), "comptable1", LocalDate.of(2026, 3, 16)));
        }

        @Test
        void test_aucune_methode_modifier_sur_ecriture_validee() {
            Set<String> methodesPubliques = Stream.of(EcritureService.class.getDeclaredMethods())
                    .filter(m -> java.lang.reflect.Modifier.isPublic(m.getModifiers()))
                    .map(Method::getName)
                    .collect(Collectors.toSet());

            assertThat(methodesPubliques)
                    .as("EcritureService ne doit exposer aucune methode de modification/suppression : "
                            + "l'absence de ces methodes est le controle RG-2 lui-meme")
                    .doesNotContain("modifier", "modifierEcriture", "update", "supprimer", "supprimerEcriture", "delete");
        }
    }

    @Nested
    class RG3_Numerotation {

        @Test
        void test_premiere_ecriture_general_recoit_numero_1() {
            Ecriture brouillon = creerBrouillonEtEnregistrer(PerimetreType.GENERAL, null, "PIECE-1", lignesEquilibrees());
            when(numerotationService.prochainNumero(PerimetreType.GENERAL, null)).thenReturn(1);
            Ecriture validee = ecritureService.valider(brouillon.getId(), "comptable1");
            assertThat(validee.getNumero()).isEqualTo(1);
        }

        @Test
        void test_deuxieme_ecriture_meme_perimetre_recoit_numero_2() {
            Ecriture b1 = creerBrouillonEtEnregistrer(PerimetreType.GENERAL, null, "PIECE-1", lignesEquilibrees());
            when(numerotationService.prochainNumero(PerimetreType.GENERAL, null)).thenReturn(1, 2);
            ecritureService.valider(b1.getId(), "comptable1");

            Ecriture b2 = creerBrouillonEtEnregistrer(PerimetreType.GENERAL, null, "PIECE-2", lignesEquilibrees());
            Ecriture v2 = ecritureService.valider(b2.getId(), "comptable1");

            assertThat(v2.getNumero()).isEqualTo(2);
        }

        @Test
        void test_ecritures_perimetres_differents_ont_sequences_independantes() {
            Ecriture bGeneral = creerBrouillonEtEnregistrer(PerimetreType.GENERAL, null, "PIECE-1", lignesEquilibrees());
            when(numerotationService.prochainNumero(PerimetreType.GENERAL, null)).thenReturn(1);
            Ecriture vGeneral = ecritureService.valider(bGeneral.getId(), "comptable1");

            List<LigneSaisie> lignesProjet = List.of(
                    LigneSaisie.debit(caisse, new BigDecimal("50.00"), null, null),
                    LigneSaisie.credit(compteProduit, new BigDecimal("50.00"), destination, null));
            Ecriture bProjet = creerBrouillonEtEnregistrer(PerimetreType.PROJET, financementB01, "PIECE-2", lignesProjet);
            when(numerotationService.prochainNumero(PerimetreType.PROJET, financementB01)).thenReturn(1);
            Ecriture vProjet = ecritureService.valider(bProjet.getId(), "comptable1");

            assertThat(vGeneral.getNumero()).isEqualTo(1);
            assertThat(vProjet.getNumero()).isEqualTo(1);
        }

        @Test
        void test_echec_validation_ne_consomme_pas_de_numero() {
            Ecriture brouillon = creerBrouillonEtEnregistrer(PerimetreType.GENERAL, null, "PIECE-1", lignesEquilibrees());
            doThrow(new PeriodeClotureeException("periode cloturee"))
                    .when(periodeClotureeService).verifierPeriodeOuverte(any());

            assertThrows(PeriodeClotureeException.class, () -> ecritureService.valider(brouillon.getId(), "comptable1"));

            verify(numerotationService, never()).prochainNumero(any(), any());
            assertThat(brouillon.getNumero()).isNull();
            assertThat(brouillon.getStatut()).isEqualTo(StatutEcriture.BROUILLON);
        }
    }

    @Nested
    class RG4_PieceJustificative {

        @Test
        void test_ecriture_sans_piece_leve_exception() {
            assertThrows(PieceJustificativeManquanteException.class, () ->
                    ecritureService.creerBrouillon(PerimetreType.GENERAL, null, LocalDate.of(2026, 3, 15),
                            "Encaissement", null, TypeOperation.ENCAISSEMENT, "comptable1", lignesEquilibrees()));
        }

        @Test
        void test_ecriture_avec_piece_vide_ou_espaces_leve_exception() {
            assertThrows(PieceJustificativeManquanteException.class, () ->
                    ecritureService.creerBrouillon(PerimetreType.GENERAL, null, LocalDate.of(2026, 3, 15),
                            "Encaissement", "   ", TypeOperation.ENCAISSEMENT, "comptable1", lignesEquilibrees()));
        }
    }

    @Nested
    class RG5_FinancementEtDestination {

        @Test
        void test_ecriture_projet_sans_financement_leve_exception() {
            assertThrows(FinancementManquantException.class, () ->
                    ecritureService.creerBrouillon(PerimetreType.PROJET, null, LocalDate.of(2026, 3, 15),
                            "Depense projet", "PIECE-1", TypeOperation.DECAISSEMENT, "comptable1", lignesEquilibrees()));
        }

        @Test
        void test_ligne_classe_6_sans_destination_leve_exception() {
            List<LigneSaisie> lignes = List.of(
                    LigneSaisie.debit(compteCharge, new BigDecimal("100.00"), null, null),
                    LigneSaisie.credit(caisse, new BigDecimal("100.00"), null, null));
            assertThrows(DestinationManquanteException.class, () ->
                    ecritureService.creerBrouillon(PerimetreType.GENERAL, null, LocalDate.of(2026, 3, 15),
                            "Achat vivres", "PIECE-1", TypeOperation.DECAISSEMENT, "comptable1", lignes));
        }

        @Test
        void test_ligne_classe_5_sans_destination_est_acceptee() {
            Ecriture e = ecritureService.creerBrouillon(PerimetreType.GENERAL, null, LocalDate.of(2026, 3, 15),
                    "Transfert", "PIECE-1", TypeOperation.TRANSFERT, "comptable1", List.of(
                            LigneSaisie.debit(banquePartage, new BigDecimal("100.00"), null, null),
                            LigneSaisie.credit(caisse, new BigDecimal("100.00"), null, null)));
            assertThat(e.getLignes()).hasSize(2);
        }
    }

    @Nested
    class RG6_PeriodeCloturee {

        @Test
        void test_ecriture_dans_periode_ouverte_est_acceptee() {
            Ecriture brouillon = creerBrouillonEtEnregistrer(PerimetreType.GENERAL, null, "PIECE-1", lignesEquilibrees());
            when(numerotationService.prochainNumero(PerimetreType.GENERAL, null)).thenReturn(1);
            Ecriture validee = ecritureService.valider(brouillon.getId(), "comptable1");
            assertThat(validee.getStatut()).isEqualTo(StatutEcriture.VALIDEE);
            verify(periodeClotureeService).verifierPeriodeOuverte(brouillon.getDateOperation());
        }

        @Test
        void test_ecriture_dans_periode_cloturee_leve_exception() {
            Ecriture brouillon = creerBrouillonEtEnregistrer(PerimetreType.GENERAL, null, "PIECE-1", lignesEquilibrees());
            doThrow(new PeriodeClotureeException("cloturee")).when(periodeClotureeService).verifierPeriodeOuverte(any());
            assertThrows(PeriodeClotureeException.class, () -> ecritureService.valider(brouillon.getId(), "comptable1"));
        }
    }

    @Nested
    class RG7_Audit {

        @Test
        void test_validation_ecriture_cree_entree_audit_log() {
            Ecriture brouillon = creerBrouillonEtEnregistrer(PerimetreType.GENERAL, null, "PIECE-1", lignesEquilibrees());
            when(numerotationService.prochainNumero(PerimetreType.GENERAL, null)).thenReturn(1);

            ecritureService.valider(brouillon.getId(), "comptable1");

            verify(auditService).enregistrer(eq("comptable1"), eq("VALIDATION_ECRITURE"), eq("Ecriture"), anyLong(), any());
        }
    }

    @Nested
    class RG8_SoldeDisponible {

        @Test
        void test_decaissement_dans_limite_solde_perimetre_est_accepte() {
            when(soldeService.soldeDisponible(banquePartage, PerimetreType.GENERAL, null))
                    .thenReturn(new BigDecimal("500.00"));
            List<LigneSaisie> lignes = List.of(
                    LigneSaisie.debit(compteCharge, new BigDecimal("200.00"), destination, null),
                    LigneSaisie.credit(banquePartage, new BigDecimal("200.00"), null, null));
            Ecriture brouillon = creerBrouillonEtEnregistrer(PerimetreType.GENERAL, null, "PIECE-1", lignes);
            when(numerotationService.prochainNumero(PerimetreType.GENERAL, null)).thenReturn(1);

            Ecriture validee = ecritureService.valider(brouillon.getId(), "comptable1");

            assertThat(validee.getStatut()).isEqualTo(StatutEcriture.VALIDEE);
        }

        @Test
        void test_decaissement_depassant_solde_perimetre_sur_compte_partage_leve_exception() {
            when(soldeService.soldeDisponible(banquePartage, PerimetreType.GENERAL, null))
                    .thenReturn(new BigDecimal("100.00"));
            List<LigneSaisie> lignes = List.of(
                    LigneSaisie.debit(compteCharge, new BigDecimal("200.00"), destination, null),
                    LigneSaisie.credit(banquePartage, new BigDecimal("200.00"), null, null));
            Ecriture brouillon = creerBrouillonEtEnregistrer(PerimetreType.GENERAL, null, "PIECE-1", lignes);

            assertThrows(SoldeInsuffisantException.class, () -> ecritureService.valider(brouillon.getId(), "comptable1"));
            verify(numerotationService, never()).prochainNumero(any(), any());
        }

        @Test
        void test_decaissement_sur_compte_dedie_ignore_le_controle_de_solde_partage() {
            Compte compteDedie = Fixtures.compteBanqueDedie(financementB01);
            List<LigneSaisie> lignes = List.of(
                    LigneSaisie.debit(compteCharge, new BigDecimal("1000000.00"), destination, null),
                    LigneSaisie.credit(compteDedie, new BigDecimal("1000000.00"), null, null));
            Ecriture brouillon = creerBrouillonEtEnregistrer(PerimetreType.PROJET, financementB01, "PIECE-1", lignes);
            when(numerotationService.prochainNumero(PerimetreType.PROJET, financementB01)).thenReturn(1);

            Ecriture validee = ecritureService.valider(brouillon.getId(), "comptable1");

            assertThat(validee.getStatut()).isEqualTo(StatutEcriture.VALIDEE);
            verifyNoInteractions(soldeService);
        }

        @Test
        void test_deux_projets_sur_meme_compte_partage_ne_se_melangent_pas() {
            Financement financementB02 = Fixtures.financement("B02");

            when(soldeService.soldeDisponible(banquePartage, PerimetreType.PROJET, financementB01))
                    .thenReturn(new BigDecimal("1000.00"));
            when(soldeService.soldeDisponible(banquePartage, PerimetreType.PROJET, financementB02))
                    .thenReturn(new BigDecimal("50.00"));

            List<LigneSaisie> lignesB01 = List.of(
                    LigneSaisie.debit(compteCharge, new BigDecimal("300.00"), destination, null),
                    LigneSaisie.credit(banquePartage, new BigDecimal("300.00"), null, null));
            Ecriture brouillonB01 = creerBrouillonEtEnregistrer(PerimetreType.PROJET, financementB01, "PIECE-1", lignesB01);
            when(numerotationService.prochainNumero(PerimetreType.PROJET, financementB01)).thenReturn(1);
            Ecriture valideeB01 = ecritureService.valider(brouillonB01.getId(), "comptable1");
            assertThat(valideeB01.getStatut()).isEqualTo(StatutEcriture.VALIDEE);

            List<LigneSaisie> lignesB02 = List.of(
                    LigneSaisie.debit(compteCharge, new BigDecimal("300.00"), destination, null),
                    LigneSaisie.credit(banquePartage, new BigDecimal("300.00"), null, null));
            Ecriture brouillonB02 = creerBrouillonEtEnregistrer(PerimetreType.PROJET, financementB02, "PIECE-2", lignesB02);

            assertThrows(SoldeInsuffisantException.class, () -> ecritureService.valider(brouillonB02.getId(), "comptable1"));
        }
    }

    @Nested
    class RG9_CompteDedie {

        @Test
        void test_ecriture_projet_correct_sur_compte_dedie_est_acceptee() {
            Compte compteDedie = Fixtures.compteBanqueDedie(financementB01);
            List<LigneSaisie> lignes = List.of(
                    LigneSaisie.debit(compteCharge, new BigDecimal("100.00"), destination, null),
                    LigneSaisie.credit(compteDedie, new BigDecimal("100.00"), null, null));
            Ecriture brouillon = creerBrouillonEtEnregistrer(PerimetreType.PROJET, financementB01, "PIECE-1", lignes);
            when(numerotationService.prochainNumero(PerimetreType.PROJET, financementB01)).thenReturn(1);

            Ecriture validee = ecritureService.valider(brouillon.getId(), "comptable1");

            assertThat(validee.getStatut()).isEqualTo(StatutEcriture.VALIDEE);
        }

        @Test
        void test_ecriture_autre_projet_sur_compte_dedie_leve_exception() {
            Financement financementB02 = Fixtures.financement("B02");
            Compte compteDedieB01 = Fixtures.compteBanqueDedie(financementB01);
            List<LigneSaisie> lignes = List.of(
                    LigneSaisie.debit(compteCharge, new BigDecimal("100.00"), destination, null),
                    LigneSaisie.credit(compteDedieB01, new BigDecimal("100.00"), null, null));
            Ecriture brouillon = creerBrouillonEtEnregistrer(PerimetreType.PROJET, financementB02, "PIECE-1", lignes);

            assertThrows(CompteDedieIncoherentException.class, () -> ecritureService.valider(brouillon.getId(), "comptable1"));
            verify(numerotationService, never()).prochainNumero(any(), any());
        }

        @Test
        void test_ecriture_generale_sur_compte_dedie_a_un_projet_leve_exception() {
            Compte compteDedie = Fixtures.compteBanqueDedie(financementB01);
            List<LigneSaisie> lignes = List.of(
                    LigneSaisie.debit(compteCharge, new BigDecimal("100.00"), destination, null),
                    LigneSaisie.credit(compteDedie, new BigDecimal("100.00"), null, null));
            Ecriture brouillon = creerBrouillonEtEnregistrer(PerimetreType.GENERAL, null, "PIECE-1", lignes);

            assertThrows(CompteDedieIncoherentException.class, () -> ecritureService.valider(brouillon.getId(), "comptable1"));
        }
    }
}
