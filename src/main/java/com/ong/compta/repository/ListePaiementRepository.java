package com.ong.compta.repository;

import com.ong.compta.domain.ListePaiement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ListePaiementRepository extends JpaRepository<ListePaiement, Long> {
    Optional<ListePaiement> findByAnneeAndMois(int annee, int mois);

    boolean existsByAnneeAndMois(int annee, int mois);
}
