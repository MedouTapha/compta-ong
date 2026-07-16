package com.ong.compta.web;

import com.ong.compta.service.exception.ComptaException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Traduit les violations de regle de gestion (RG-1 a RG-9) en message utilisateur,
 * sans jamais les laisser remonter en erreur 500 generique.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ComptaException.class)
    public String gererComptaException(ComptaException ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }
}
