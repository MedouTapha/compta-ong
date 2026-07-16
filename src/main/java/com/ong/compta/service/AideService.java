package com.ong.compta.service;

import com.ong.compta.domain.Aide;
import com.ong.compta.domain.Enfant;
import com.ong.compta.domain.Financement;
import com.ong.compta.domain.enums.StatutAide;
import com.ong.compta.repository.AideRepository;
import com.ong.compta.service.exception.RessourceIntrouvableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class AideService {

    private final AideRepository aideRepository;

    public AideService(AideRepository aideRepository) {
        this.aideRepository = aideRepository;
    }

    public List<Aide> lister() {
        return aideRepository.findAll();
    }

    public Aide trouver(Long id) {
        return aideRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Aide introuvable : " + id));
    }

    public List<Aide> historique(Enfant enfant) {
        return aideRepository.findByEnfant(enfant);
    }

    /**
     * US-6.1 : aides actives pour le mois cible (utilise lors de la generation
     * de la liste de paiement).
     */
    public List<Aide> listerActivesPour(int annee, int mois) {
        return aideRepository.findByStatut(StatutAide.ACTIVE).stream()
                .filter(a -> a.estActivePour(annee, mois))
                .toList();
    }

    @Transactional
    public Aide creer(Enfant enfant, BigDecimal montantMensuel, Financement financement, String referenceDecision,
                       LocalDate dateDebut, LocalDate dateFin) {
        return aideRepository.save(new Aide(enfant, montantMensuel, financement, referenceDecision, dateDebut,
                dateFin, StatutAide.ACTIVE));
    }

    @Transactional
    public Aide modifier(Long id, BigDecimal montantMensuel, String referenceDecision, LocalDate dateDebut,
                          LocalDate dateFin, StatutAide statut) {
        Aide aide = trouver(id);
        aide.setMontantMensuel(montantMensuel);
        aide.setReferenceDecision(referenceDecision);
        aide.setDateDebut(dateDebut);
        aide.setDateFin(dateFin);
        aide.setStatut(statut);
        return aide;
    }
}
