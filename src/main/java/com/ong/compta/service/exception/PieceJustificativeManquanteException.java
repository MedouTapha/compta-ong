package com.ong.compta.service.exception;

/**
 * RG-4 : la reference de piece justificative est obligatoire.
 */
public class PieceJustificativeManquanteException extends ComptaException {
    public PieceJustificativeManquanteException(String message) {
        super(message);
    }
}
