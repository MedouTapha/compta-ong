package com.ong.compta.service.exception;

/**
 * RG-6 : impossible de saisir/valider une ecriture dont la date tombe dans une periode cloturee.
 */
public class PeriodeClotureeException extends ComptaException {
    public PeriodeClotureeException(String message) {
        super(message);
    }
}
