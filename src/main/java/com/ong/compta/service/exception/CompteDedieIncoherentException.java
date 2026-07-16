package com.ong.compta.service.exception;

/**
 * RG-9 : un compte dedie a un projet ne peut recevoir que des ecritures de ce projet.
 */
public class CompteDedieIncoherentException extends ComptaException {
    public CompteDedieIncoherentException(String message) {
        super(message);
    }
}
