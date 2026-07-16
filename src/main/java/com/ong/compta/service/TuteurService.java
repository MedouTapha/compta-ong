package com.ong.compta.service;

import com.ong.compta.domain.Tuteur;
import com.ong.compta.repository.TuteurRepository;
import com.ong.compta.service.exception.RessourceIntrouvableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TuteurService {

    private final TuteurRepository tuteurRepository;

    public TuteurService(TuteurRepository tuteurRepository) {
        this.tuteurRepository = tuteurRepository;
    }

    public List<Tuteur> lister() {
        return tuteurRepository.findAll();
    }

    public Tuteur trouver(Long id) {
        return tuteurRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Tuteur introuvable : " + id));
    }

    @Transactional
    public Tuteur creer(String nom, String pieceIdentiteType, String pieceIdentiteNumero, String telephone) {
        return tuteurRepository.save(new Tuteur(nom, pieceIdentiteType, pieceIdentiteNumero, telephone));
    }

    @Transactional
    public Tuteur modifier(Long id, String nom, String pieceIdentiteType, String pieceIdentiteNumero, String telephone) {
        Tuteur tuteur = trouver(id);
        tuteur.setNom(nom);
        tuteur.setPieceIdentiteType(pieceIdentiteType);
        tuteur.setPieceIdentiteNumero(pieceIdentiteNumero);
        tuteur.setTelephone(telephone);
        return tuteur;
    }
}
