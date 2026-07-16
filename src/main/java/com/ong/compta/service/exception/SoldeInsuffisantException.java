package com.ong.compta.service.exception;

/**
 * RG-8 : le decaissement depasserait le solde disponible du perimetre sur un compte de tresorerie partage.
 */
public class SoldeInsuffisantException extends ComptaException {
    public SoldeInsuffisantException(String message) {
        super(message);
    }
}
