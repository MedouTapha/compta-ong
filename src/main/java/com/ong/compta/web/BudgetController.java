package com.ong.compta.web;

import com.ong.compta.service.FinancementService;
import com.ong.compta.service.LigneBudgetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/budgets")
public class BudgetController {

    private final LigneBudgetService ligneBudgetService;
    private final FinancementService financementService;

    public BudgetController(LigneBudgetService ligneBudgetService, FinancementService financementService) {
        this.ligneBudgetService = ligneBudgetService;
        this.financementService = financementService;
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("suivis", ligneBudgetService.suiviGlobal());
        model.addAttribute("financements", financementService.lister());
        return "budgets/liste";
    }

    @PostMapping
    public String creer(@RequestParam Long financementId, @RequestParam String libelle,
                         @RequestParam BigDecimal montant, @RequestParam int exercice) {
        ligneBudgetService.creer(financementService.trouver(financementId), libelle, montant, exercice);
        return "redirect:/budgets";
    }
}
