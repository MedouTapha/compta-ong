package com.ong.compta.web;

import com.ong.compta.service.DestinationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/destinations")
public class DestinationController {

    private final DestinationService destinationService;

    public DestinationController(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("destinations", destinationService.lister());
        return "destinations/liste";
    }

    @PostMapping
    public String creer(@RequestParam String code, @RequestParam String libelle) {
        destinationService.creer(code, libelle);
        return "redirect:/destinations";
    }

    @GetMapping("/{id}/edit")
    public String editer(@PathVariable Long id, Model model) {
        model.addAttribute("destination", destinationService.trouver(id));
        return "destinations/edit";
    }

    @PostMapping("/{id}/edit")
    public String modifier(@PathVariable Long id, @RequestParam String libelle,
                            @RequestParam(defaultValue = "false") boolean actif) {
        destinationService.modifier(id, libelle, actif);
        return "redirect:/destinations";
    }
}
