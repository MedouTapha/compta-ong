package com.ong.compta.service.exception;

/**
 * Regle de cloture de liste de paiement : etat incoherent pour l'operation demandee
 * (ex: cloture alors qu'il reste des lignes A_PAYER).
 */
public class ListePaiementInvalideException extends ComptaException {
    public ListePaiementInvalideException(String message) {
        super(message);
    }
}
