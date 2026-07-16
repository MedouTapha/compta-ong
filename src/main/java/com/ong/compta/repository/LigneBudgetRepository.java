package com.ong.compta.repository;

import com.ong.compta.domain.Financement;
import com.ong.compta.domain.LigneBudget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LigneBudgetRepository extends JpaRepository<LigneBudget, Long> {
    List<LigneBudget> findByFinancement(Financement financement);

    List<LigneBudget> findByFinancementAndExercice(Financement financement, int exercice);
}
