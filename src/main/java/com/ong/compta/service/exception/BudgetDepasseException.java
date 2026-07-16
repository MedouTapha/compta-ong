package com.ong.compta.service.exception;

/**
 * US-4.3 : le decaissement ferait depasser 100% d'une ligne de budget et n'a pas ete
 * debloque par un Directeur.
 */
public class BudgetDepasseException extends ComptaException {
    public BudgetDepasseException(String message) {
        super(message);
    }
}
