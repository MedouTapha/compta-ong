package com.ong.compta.service;

import com.ong.compta.domain.Enfant;
import com.ong.compta.domain.Tuteur;
import com.ong.compta.domain.enums.StatutEnfant;
import com.ong.compta.repository.EnfantRepository;
import com.ong.compta.service.exception.RessourceIntrouvableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EnfantService {

    private final EnfantRepository enfantRepository;

    public EnfantService(EnfantRepository enfantRepository) {
        this.enfantRepository = enfantRepository;
    }

    public List<Enfant> lister() {
        return enfantRepository.findAll();
    }

    public Enfant trouver(Long id) {
        return enfantRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Enfant introuvable : " + id));
    }

    @Transactional
    public Enfant creer(String nom, LocalDate dateNaissance, String sexe, Tuteur tuteur, LocalDate dateEntree,
                         String notes) {
        return enfantRepository.save(new Enfant(nom, dateNaissance, sexe, tuteur, dateEntree, StatutEnfant.ACTIF, notes));
    }

    @Transactional
    public Enfant modifier(Long id, String nom, LocalDate dateNaissance, String sexe, Tuteur tuteur,
                            StatutEnfant statut, String notes) {
        Enfant enfant = trouver(id);
        enfant.setNom(nom);
        enfant.setDateNaissance(dateNaissance);
        enfant.setSexe(sexe);
        enfant.setTuteur(tuteur);
        enfant.setStatut(statut);
        enfant.setNotes(notes);
        return enfant;
    }
}
