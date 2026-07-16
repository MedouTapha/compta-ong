package com.ong.compta.service.exception;

/**
 * RG-1 : le total des debits doit etre strictement egal au total des credits.
 */
public class EcritureDesequilibreeException extends ComptaException {
    public EcritureDesequilibreeException(String message) {
        super(message);
    }
}
