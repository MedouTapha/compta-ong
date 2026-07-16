package com.ong.compta.web;

import com.ong.compta.domain.enums.StatutFinancement;
import com.ong.compta.service.FinancementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Controller
@RequestMapping("/financements")
public class FinancementController {

    private final FinancementService financementService;

    public FinancementController(FinancementService financementService) {
        this.financementService = financementService;
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("financements", financementService.lister());
        return "financements/liste";
    }

    @PostMapping
    public String creer(@RequestParam String code, @RequestParam String libelle, @RequestParam(required = false) String bailleur,
                         @RequestParam @DateTimeFormat(iso = DATE) LocalDate dateDebut,
                         @RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate dateFin) {
        financementService.creer(code, libelle, bailleur, dateDebut, dateFin);
        return "redirect:/financements";
    }

    @GetMapping("/{id}/edit")
    public String editer(@PathVariable Long id, Model model) {
        model.addAttribute("financement", financementService.trouver(id));
        model.addAttribute("statuts", StatutFinancement.values());
        return "financements/edit";
    }

    @PostMapping("/{id}/edit")
    public String modifier(@PathVariable Long id, @RequestParam String libelle, @RequestParam(required = false) String bailleur,
                            @RequestParam @DateTimeFormat(iso = DATE) LocalDate dateDebut,
                            @RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate dateFin,
                            @RequestParam StatutFinancement statut) {
        financementService.modifier(id, libelle, bailleur, dateDebut, dateFin, statut);
        return "redirect:/financements";
    }
}
