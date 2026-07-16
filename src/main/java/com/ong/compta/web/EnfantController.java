package com.ong.compta.web;

import com.ong.compta.domain.Tuteur;
import com.ong.compta.domain.enums.StatutEnfant;
import com.ong.compta.service.EnfantService;
import com.ong.compta.service.TuteurService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/enfants")
public class EnfantController {

    private final EnfantService enfantService;
    private final TuteurService tuteurService;

    public EnfantController(EnfantService enfantService, TuteurService tuteurService) {
        this.enfantService = enfantService;
        this.tuteurService = tuteurService;
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("enfants", enfantService.lister());
        model.addAttribute("tuteurs", tuteurService.lister());
        return "enfants/liste";
    }

    @PostMapping
    public String creer(@RequestParam String nom,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateNaissance,
                         @RequestParam String sexe,
                         @RequestParam Long tuteurId,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateEntree,
                         @RequestParam(required = false) String notes) {
        Tuteur tuteur = tuteurService.trouver(tuteurId);
        enfantService.creer(nom, dateNaissance, sexe, tuteur, dateEntree, notes);
        return "redirect:/enfants";
    }

    @GetMapping("/{id}/edit")
    public String editer(@PathVariable Long id, Model model) {
        model.addAttribute("enfant", enfantService.trouver(id));
        model.addAttribute("tuteurs", tuteurService.lister());
        return "enfants/edit";
    }

    @PostMapping("/{id}/edit")
    public String modifier(@PathVariable Long id, @RequestParam String nom,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateNaissance,
                            @RequestParam String sexe,
                            @RequestParam Long tuteurId,
                            @RequestParam StatutEnfant statut,
                            @RequestParam(required = false) String notes) {
        Tuteur tuteur = tuteurService.trouver(tuteurId);
        enfantService.modifier(id, nom, dateNaissance, sexe, tuteur, statut, notes);
        return "redirect:/enfants";
    }
}
