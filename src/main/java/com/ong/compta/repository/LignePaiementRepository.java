package com.ong.compta.repository;

import com.ong.compta.domain.LignePaiement;
import com.ong.compta.domain.ListePaiement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LignePaiementRepository extends JpaRepository<LignePaiement, Long> {
    List<LignePaiement> findByListe(ListePaiement liste);
}
