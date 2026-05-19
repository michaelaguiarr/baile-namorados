package com.igreja.baile.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ResumoDTO {
    private long totalReservas;
    private long totalPagos;
    private long totalPendentes;
    private long totalCasais;
    private long totalIndividuais;
    private BigDecimal totalArrecadado;
    private BigDecimal totalPrevisto;
    private BigDecimal totalPendentePagamento;
}
