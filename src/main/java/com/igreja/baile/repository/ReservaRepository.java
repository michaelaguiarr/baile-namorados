package com.igreja.baile.repository;

import com.igreja.baile.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findAllByOrderByNomeAsc();

    List<Reserva> findByPagoOrderByNomeAsc(Boolean pago);

    List<Reserva> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    long countByPago(Boolean pago);

    long countByTipo(Reserva.TipoReserva tipo);

    @Query("SELECT COALESCE(SUM(r.valor), 0) FROM Reserva r WHERE r.pago = true")
    BigDecimal totalArrecadado();

    @Query("SELECT COALESCE(SUM(r.valor), 0) FROM Reserva r")
    BigDecimal totalPrevisto();
}
