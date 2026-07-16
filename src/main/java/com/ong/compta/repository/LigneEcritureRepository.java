package com.ong.compta.repository;

import com.ong.compta.domain.Compte;
import com.ong.compta.domain.Financement;
import com.ong.compta.domain.LigneBudget;
import com.ong.compta.domain.LigneEcriture;
import com.ong.compta.domain.enums.PerimetreType;
import com.ong.compta.domain.enums.StatutEcriture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface LigneEcritureRepository extends JpaRepository<LigneEcriture, Long> {

    @Query("select coalesce(sum(l.debit), 0) - coalesce(sum(l.credit), 0) " +
           "from LigneEcriture l " +
           "where l.compte = :compte " +
           "and l.ecriture.statut = com.ong.compta.domain.enums.StatutEcriture.VALIDEE " +
           "and l.ecriture.perimetreType = :perimetreType " +
           "and (:financement is null and l.ecriture.financement is null or l.ecriture.financement = :financement)")
    BigDecimal soldeCompteParPerimetre(@Param("compte") Compte compte,
                                        @Param("perimetreType") PerimetreType perimetreType,
                                        @Param("financement") Financement financement);

    List<LigneEcriture> findByEcritureStatutAndCompte(StatutEcriture statut, Compte compte);

    @Query("select coalesce(sum(l.debit), 0) - coalesce(sum(l.credit), 0) " +
           "from LigneEcriture l " +
           "where l.ligneBudget = :ligneBudget " +
           "and l.ecriture.statut = com.ong.compta.domain.enums.StatutEcriture.VALIDEE")
    BigDecimal montantEngageParLigneBudget(@Param("ligneBudget") LigneBudget ligneBudget);
}
