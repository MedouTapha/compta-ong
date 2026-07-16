package com.ong.compta.web;

import com.ong.compta.service.TuteurService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tuteurs")
public class TuteurController {

    private final TuteurService tuteurService;

    public TuteurController(TuteurService tuteurService) {
        this.tuteurService = tuteurService;
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("tuteurs", tuteurService.lister());
        return "tuteurs/liste";
    }

    @PostMapping
    public String creer(@RequestParam String nom, @RequestParam(required = false) String pieceIdentiteType,
                         @RequestParam(required = false) String pieceIdentiteNumero,
                         @RequestParam(required = false) String telephone) {
        tuteurService.creer(nom, pieceIdentiteType, pieceIdentiteNumero, telephone);
        return "redirect:/tuteurs";
    }

    @GetMapping("/{id}/edit")
    public String editer(@PathVariable Long id, Model model) {
        model.addAttribute("tuteur", tuteurService.trouver(id));
        return "tuteurs/edit";
    }

    @PostMapping("/{id}/edit")
    public String modifier(@PathVariable Long id, @RequestParam String nom,
                            @RequestParam(required = false) String pieceIdentiteType,
                            @RequestParam(required = false) String pieceIdentiteNumero,
                            @RequestParam(required = false) String telephone) {
        tuteurService.modifier(id, nom, pieceIdentiteType, pieceIdentiteNumero, telephone);
        return "redirect:/tuteurs";
    }
}
