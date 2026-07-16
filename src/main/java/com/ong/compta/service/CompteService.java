package com.ong.compta.service;

import com.ong.compta.domain.Compte;
import com.ong.compta.domain.Financement;
import com.ong.compta.repository.CompteRepository;
import com.ong.compta.service.exception.RessourceIntrouvableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompteService {

    private final CompteRepository compteRepository;

    public CompteService(CompteRepository compteRepository) {
        this.compteRepository = compteRepository;
    }

    public List<Compte> lister() {
        return compteRepository.findAll();
    }

    public Compte trouver(Long id) {
        return compteRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Compte introuvable : " + id));
    }

    @Transactional
    public Compte creer(String numero, String libelle, boolean estTresorerie, Financement projetDedie) {
        Compte compte = new Compte(numero, libelle, estTresorerie, projetDedie);
        return compteRepository.save(compte);
    }

    @Transactional
    public Compte modifier(Long id, String libelle, boolean estTresorerie, Financement projetDedie, boolean actif) {
        Compte compte = trouver(id);
        compte.setLibelle(libelle);
        compte.setEstTresorerie(estTresorerie);
        compte.setProjetDedie(projetDedie);
        compte.setActif(actif);
        return compte;
    }
}
