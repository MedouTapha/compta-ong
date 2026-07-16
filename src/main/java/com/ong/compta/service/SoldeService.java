package com.ong.compta.service;

import com.ong.compta.domain.Compte;
import com.ong.compta.domain.Financement;
import com.ong.compta.domain.enums.PerimetreType;
import com.ong.compta.repository.LigneEcritureRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Calcule le solde disponible d'un compte de tresorerie pour un perimetre donne
 * (RG-8 : plusieurs perimetres peuvent partager un meme compte physique sans se melanger).
 */
@Service
public class SoldeService {

    private final LigneEcritureRepository ligneEcritureRepository;

    public SoldeService(LigneEcritureRepository ligneEcritureRepository) {
        this.ligneEcritureRepository = ligneEcritureRepository;
    }

    public BigDecimal soldeDisponible(Compte compte, PerimetreType perimetreType, Financement financement) {
        BigDecimal solde = ligneEcritureRepository.soldeCompteParPerimetre(compte, perimetreType, financement);
        return solde == null ? BigDecimal.ZERO : solde;
    }
}
