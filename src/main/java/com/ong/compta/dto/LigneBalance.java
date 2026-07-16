package com.ong.compta.dto;

import com.ong.compta.domain.Compte;

import java.math.BigDecimal;

public record LigneBalance(Compte compte, BigDecimal totalDebit, BigDecimal totalCredit, BigDecimal solde) {
}
