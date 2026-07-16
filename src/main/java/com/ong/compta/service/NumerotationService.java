package com.ong.compta.service;

import com.ong.compta.domain.CompteurEcriture;
import com.ong.compta.domain.Financement;
import com.ong.compta.domain.enums.PerimetreType;
import com.ong.compta.repository.CompteurEcritureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * RG-3 : numerotation sequentielle sans trou, par perimetre.
 * Le verrou pessimiste (SELECT ... FOR UPDATE) sur la ligne du compteur garantit
 * l'absence de doublon en cas d'acces concurrent : il ne faut jamais s'appuyer
 * uniquement sur une contrainte d'unicite en base pour "rattraper" le probleme.
 */
@Service
public class NumerotationService {

    private final CompteurEcritureRepository compteurEcritureRepository;

    public NumerotationService(CompteurEcritureRepository compteurEcritureRepository) {
        this.compteurEcritureRepository = compteurEcritureRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public int prochainNumero(PerimetreType perimetreType, Financement financement) {
        CompteurEcriture compteur = obtenirCompteurVerrouille(perimetreType, financement);
        return compteur.incrementerEtObtenir();
    }

    private CompteurEcriture obtenirCompteurVerrouille(PerimetreType perimetreType, Financement financement) {
        if (financement == null) {
            return compteurEcritureRepository.findGeneralPourVerrouillage(perimetreType)
                    .orElseGet(() -> compteurEcritureRepository.save(new CompteurEcriture(perimetreType, null)));
        }
        return compteurEcritureRepository.findProjetPourVerrouillage(perimetreType, financement)
                .orElseGet(() -> compteurEcritureRepository.save(new CompteurEcriture(perimetreType, financement)));
    }
}
