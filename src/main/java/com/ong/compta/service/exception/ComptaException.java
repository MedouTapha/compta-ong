package com.ong.compta.service.exception;

/**
 * Racine des exceptions de regle de gestion (RG) du moteur comptable.
 */
public abstract class ComptaException extends RuntimeException {
    protected ComptaException(String message) {
        super(message);
    }
}
