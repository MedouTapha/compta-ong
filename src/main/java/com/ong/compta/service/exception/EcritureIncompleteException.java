package com.ong.compta.service.exception;

/**
 * RG-1 : une ecriture doit comporter au moins deux lignes.
 */
public class EcritureIncompleteException extends ComptaException {
    public EcritureIncompleteException(String message) {
        super(message);
    }
}
