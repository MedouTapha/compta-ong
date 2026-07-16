package com.ong.compta.service;

import com.ong.compta.domain.Destination;
import com.ong.compta.repository.DestinationRepository;
import com.ong.compta.service.exception.RessourceIntrouvableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DestinationService {

    private final DestinationRepository destinationRepository;

    public DestinationService(DestinationRepository destinationRepository) {
        this.destinationRepository = destinationRepository;
    }

    public List<Destination> lister() {
        return destinationRepository.findAll();
    }

    public Destination trouver(Long id) {
        return destinationRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Destination introuvable : " + id));
    }

    @Transactional
    public Destination creer(String code, String libelle) {
        return destinationRepository.save(new Destination(code, libelle));
    }

    @Transactional
    public Destination modifier(Long id, String libelle, boolean actif) {
        Destination destination = trouver(id);
        destination.setLibelle(libelle);
        destination.setActif(actif);
        return destination;
    }
}
