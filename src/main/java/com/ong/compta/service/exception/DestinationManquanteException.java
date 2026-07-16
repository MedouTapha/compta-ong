package com.ong.compta.service.exception;

/**
 * RG-5 : les lignes de charges (classe 6) et de produits (classe 7) doivent porter une destination.
 */
public class DestinationManquanteException extends ComptaException {
    public DestinationManquanteException(String message) {
        super(message);
    }
}
