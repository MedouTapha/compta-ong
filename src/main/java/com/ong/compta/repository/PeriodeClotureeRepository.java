package com.ong.compta.repository;

import com.ong.compta.domain.PeriodeCloturee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeriodeClotureeRepository extends JpaRepository<PeriodeCloturee, Long> {
    boolean existsByAnneeAndMois(int annee, int mois);
}
