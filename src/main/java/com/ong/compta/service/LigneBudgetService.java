package com.ong.compta.service;

import com.ong.compta.domain.Financement;
import com.ong.compta.domain.LigneBudget;
import com.ong.compta.dto.SuiviBudgetaire;
import com.ong.compta.repository.LigneBudgetRepository;
import com.ong.compta.service.exception.RessourceIntrouvableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class LigneBudgetService {

    private final LigneBudgetRepository ligneBudgetRepository;
    private final BudgetControlService budgetControlService;

    public LigneBudgetService(LigneBudgetRepository ligneBudgetRepository, BudgetControlService budgetControlService) {
        this.ligneBudgetRepository = ligneBudgetRepository;
        this.budgetControlService = budgetControlService;
    }

    public List<LigneBudget> lister() {
        return ligneBudgetRepository.findAll();
    }

    public LigneBudget trouver(Long id) {
        return ligneBudgetRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Ligne de budget introuvable : " + id));
    }

    @Transactional
    public LigneBudget creer(Financement financement, String libelle, BigDecimal montant, int exercice) {
        return ligneBudgetRepository.save(new LigneBudget(financement, libelle, montant, exercice));
    }

    public List<SuiviBudgetaire> suiviGlobal() {
        return lister().stream().map(budgetControlService::suivi).toList();
    }
}
