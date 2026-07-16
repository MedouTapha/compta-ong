package com.ong.compta.service;

import com.ong.compta.domain.CompteurEcriture;
import com.ong.compta.domain.Financement;
import com.ong.compta.domain.enums.PerimetreType;
import com.ong.compta.domain.enums.StatutFinancement;
import com.ong.compta.repository.CompteurEcritureRepository;
import com.ong.compta.repository.FinancementRepository;
import com.ong.compta.service.exception.RessourceIntrouvableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class FinancementService {

    private final FinancementRepository financementRepository;
    private final CompteurEcritureRepository compteurEcritureRepository;

    public FinancementService(FinancementRepository financementRepository,
                               CompteurEcritureRepository compteurEcritureRepository) {
        this.financementRepository = financementRepository;
        this.compteurEcritureRepository = compteurEcritureRepository;
    }

    public List<Financement> lister() {
        return financementRepository.findAll();
    }

    public Financement trouver(Long id) {
        return financementRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Financement introuvable : " + id));
    }

    @Transactional
    public Financement creer(String code, String libelle, String bailleur, LocalDate dateDebut, LocalDate dateFin) {
        Financement financement = new Financement(code, libelle, bailleur, dateDebut, dateFin, StatutFinancement.ACTIF);
        financement = financementRepository.save(financement);
        // le compteur d'ecritures du projet est cree en meme temps que le financement,
        // afin d'eviter toute creation concurrente non verrouillee lors de la premiere validation.
        compteurEcritureRepository.save(new CompteurEcriture(PerimetreType.PROJET, financement));
        return financement;
    }

    @Transactional
    public Financement modifier(Long id, String libelle, String bailleur, LocalDate dateDebut, LocalDate dateFin,
                                 StatutFinancement statut) {
        Financement financement = trouver(id);
        financement.setLibelle(libelle);
        financement.setBailleur(bailleur);
        financement.setDateDebut(dateDebut);
        financement.setDateFin(dateFin);
        financement.setStatut(statut);
        return financement;
    }
}
