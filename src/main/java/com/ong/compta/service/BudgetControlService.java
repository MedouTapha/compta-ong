package com.ong.compta.service;

import com.ong.compta.domain.Ecriture;
import com.ong.compta.domain.LigneBudget;
import com.ong.compta.domain.LigneEcriture;
import com.ong.compta.dto.SuiviBudgetaire;
import com.ong.compta.repository.LigneEcritureRepository;
import com.ong.compta.service.exception.BudgetDepasseException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * US-4.1/US-4.3 : suivi de la consommation budgetaire par ligne de budget et controle
 * du seuil de 100% (avec alerte non bloquante des 80%) lors de la validation d'un decaissement.
 */
@Service
public class BudgetControlService {

    private final LigneEcritureRepository ligneEcritureRepository;

    public BudgetControlService(LigneEcritureRepository ligneEcritureRepository) {
        this.ligneEcritureRepository = ligneEcritureRepository;
    }

    public SuiviBudgetaire suivi(LigneBudget ligneBudget) {
        BigDecimal montantEngage = ligneEcritureRepository.montantEngageParLigneBudget(ligneBudget);
        return SuiviBudgetaire.de(ligneBudget, montantEngage == null ? BigDecimal.ZERO : montantEngage);
    }

    /**
     * Verifie, pour chaque ligne de budget impactee par cette ecriture, que le seuil de 100%
     * n'est pas depasse. Si l'ecriture a ete debloquee par un Directeur (RG budget), le
     * depassement est tolere.
     */
    public void verifierSeuils(Ecriture ecriture) {
        if (ecriture.isBudgetDebloque()) {
            return;
        }
        Map<LigneBudget, BigDecimal> deltaParLigneBudget = new HashMap<>();
        for (LigneEcriture ligne : ecriture.getLignes()) {
            if (ligne.getLigneBudget() == null) {
                continue;
            }
            BigDecimal delta = ligne.getDebit().subtract(ligne.getCredit());
            deltaParLigneBudget.merge(ligne.getLigneBudget(), delta, BigDecimal::add);
        }

        for (Map.Entry<LigneBudget, BigDecimal> entry : deltaParLigneBudget.entrySet()) {
            LigneBudget ligneBudget = entry.getKey();
            SuiviBudgetaire suiviActuel = suivi(ligneBudget);
            BigDecimal montantProjete = suiviActuel.montantEngage().add(entry.getValue());
            SuiviBudgetaire suiviProjete = SuiviBudgetaire.de(ligneBudget, montantProjete);
            if (suiviProjete.depasseLimite()) {
                throw new BudgetDepasseException(
                        "La ligne de budget '" + ligneBudget.getLibelle() + "' serait engagee a "
                                + suiviProjete.pourcentage() + "% (limite 100%). Deblocage requis par un Directeur.");
            }
        }
    }
}
