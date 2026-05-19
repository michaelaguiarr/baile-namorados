package com.igreja.baile.dto;

import com.igreja.baile.model.Reserva;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservaForm {

    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotNull(message = "Tipo é obrigatório")
    private Reserva.TipoReserva tipo;

    private Boolean pago = false;
}
