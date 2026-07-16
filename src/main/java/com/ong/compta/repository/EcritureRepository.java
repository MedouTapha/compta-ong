package com.ong.compta.repository;

import com.ong.compta.domain.Ecriture;
import com.ong.compta.domain.Financement;
import com.ong.compta.domain.enums.PerimetreType;
import com.ong.compta.domain.enums.StatutEcriture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EcritureRepository extends JpaRepository<Ecriture, Long> {

    List<Ecriture> findByStatutOrderByDateOperationAscNumeroAsc(StatutEcriture statut);

    List<Ecriture> findByPerimetreTypeAndFinancementAndStatutOrderByNumeroAsc(
            PerimetreType perimetreType, Financement financement, StatutEcriture statut);

    List<Ecriture> findByPerimetreTypeAndFinancementIsNullAndStatutOrderByNumeroAsc(
            PerimetreType perimetreType, StatutEcriture statut);

    List<Ecriture> findByDateOperationBetweenOrderByDateOperationAscNumeroAsc(LocalDate debut, LocalDate fin);

    List<Ecriture> findByFinancementAndStatutOrderByDateOperationAscNumeroAsc(Financement financement, StatutEcriture statut);
}
