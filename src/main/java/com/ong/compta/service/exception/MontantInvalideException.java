package com.ong.compta.service.exception;

/**
 * RG-1 : les montants de debit/credit doivent etre positifs ou nuls.
 */
public class MontantInvalideException extends ComptaException {
    public MontantInvalideException(String message) {
        super(message);
    }
}
