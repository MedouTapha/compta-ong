package com.ong.compta.web;

import com.ong.compta.domain.Financement;
import com.ong.compta.service.CompteService;
import com.ong.compta.service.FinancementService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/comptes")
public class CompteController {

    private final CompteService compteService;
    private final FinancementService financementService;

    public CompteController(CompteService compteService, FinancementService financementService) {
        this.compteService = compteService;
        this.financementService = financementService;
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("comptes", compteService.lister());
        model.addAttribute("financements", financementService.lister());
        return "comptes/liste";
    }

    @PostMapping
    public String creer(@RequestParam String numero, @RequestParam String libelle,
                         @RequestParam(defaultValue = "false") boolean estTresorerie,
                         @RequestParam(required = false) Long projetDedieId,
                         Model model) {
        Financement projetDedie = projetDedieId == null ? null : financementService.trouver(projetDedieId);
        compteService.creer(numero, libelle, estTresorerie, projetDedie);
        return "redirect:/comptes";
    }

    @GetMapping("/{id}/edit")
    public String editer(@PathVariable Long id, Model model) {
        model.addAttribute("compte", compteService.trouver(id));
        model.addAttribute("financements", financementService.lister());
        return "comptes/edit";
    }

    @PostMapping("/{id}/edit")
    public String modifier(@PathVariable Long id, @RequestParam String libelle,
                            @RequestParam(defaultValue = "false") boolean estTresorerie,
                            @RequestParam(required = false) Long projetDedieId,
                            @RequestParam(defaultValue = "false") boolean actif) {
        Financement projetDedie = projetDedieId == null ? null : financementService.trouver(projetDedieId);
        compteService.modifier(id, libelle, estTresorerie, projetDedie, actif);
        return "redirect:/comptes";
    }
}
