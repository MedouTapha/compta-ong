package com.ong.compta.service.exception;

/**
 * RG-5 : une ecriture de perimetre PROJET doit obligatoirement referencer un financement.
 */
public class FinancementManquantException extends ComptaException {
    public FinancementManquantException(String message) {
        super(message);
    }
}
