package com.ong.compta.service.exception;

/**
 * RG-2 : operation demandee incompatible avec le statut courant de l'entite
 * (ex: contre-passer une ecriture qui n'est pas VALIDEE).
 */
public class EtatInvalideException extends ComptaException {
    public EtatInvalideException(String message) {
        super(message);
    }
}
