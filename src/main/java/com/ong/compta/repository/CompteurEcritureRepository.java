package com.ong.compta.repository;

import com.ong.compta.domain.CompteurEcriture;
import com.ong.compta.domain.Financement;
import com.ong.compta.domain.enums.PerimetreType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompteurEcritureRepository extends JpaRepository<CompteurEcriture, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CompteurEcriture c where c.perimetreType = :perimetreType and c.financement is null")
    Optional<CompteurEcriture> findGeneralPourVerrouillage(@Param("perimetreType") PerimetreType perimetreType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CompteurEcriture c where c.perimetreType = :perimetreType and c.financement = :financement")
    Optional<CompteurEcriture> findProjetPourVerrouillage(@Param("perimetreType") PerimetreType perimetreType,
                                                            @Param("financement") Financement financement);
}
