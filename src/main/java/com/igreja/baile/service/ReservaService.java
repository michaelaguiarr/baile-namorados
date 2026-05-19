package com.igreja.baile.service;

import com.igreja.baile.dto.ReservaForm;
import com.igreja.baile.dto.ResumoDTO;
import com.igreja.baile.exception.NomeDuplicadoException;
import com.igreja.baile.model.Reserva;
import com.igreja.baile.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository repository;

    @Value("${app.valor.casal:100.00}")
    private BigDecimal valorCasal;

    @Value("${app.valor.individual:50.00}")
    private BigDecimal valorIndividual;

    public List<Reserva> listarTodas() {
        return repository.findAllByOrderByNomeAsc();
    }

    public List<Reserva> listarPorStatus(Boolean pago) {
        return repository.findByPagoOrderByNomeAsc(pago);
    }

    public List<Reserva> buscarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCaseOrderByNomeAsc(nome);
    }

    @Transactional
    public Reserva salvar(ReservaForm form) {
        boolean edicao = form.getId() != null;
        String nome = form.getNome().trim();

        if (edicao) {
            if (repository.existsByNomeIgnoreCaseAndIdNot(nome, form.getId())) {
                throw new NomeDuplicadoException("Já existe uma reserva com o nome \"" + nome + "\".");
            }
        } else {
            if (repository.existsByNomeIgnoreCase(nome)) {
                throw new NomeDuplicadoException("Já existe uma reserva com o nome \"" + nome + "\".");
            }
        }

        Reserva reserva = edicao
                ? repository.findById(form.getId()).orElse(new Reserva())
                : new Reserva();

        reserva.setNome(nome);
        reserva.setTipo(form.getTipo());
        reserva.setValor(form.getTipo() == Reserva.TipoReserva.CASAL ? valorCasal : valorIndividual);

        boolean pagouAntes = reserva.getPago() != null && reserva.getPago();
        boolean pagaAgora = Boolean.TRUE.equals(form.getPago());

        reserva.setPago(pagaAgora);
        if (pagaAgora && !pagouAntes) {
            reserva.setDataPagamento(LocalDateTime.now());
        } else if (!pagaAgora) {
            reserva.setDataPagamento(null);
        }

        return repository.save(reserva);
    }

    @Transactional
    public void togglePagamento(Long id) {
        Reserva reserva = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada: " + id));
        boolean novoPago = !Boolean.TRUE.equals(reserva.getPago());
        reserva.setPago(novoPago);
        reserva.setDataPagamento(novoPago ? LocalDateTime.now() : null);
        repository.save(reserva);
    }

    @Transactional
    public void excluir(Long id) {
        repository.deleteById(id);
    }

    public ResumoDTO resumo() {
        long total = repository.count();
        long pagos = repository.countByPago(true);
        long pendentes = repository.countByPago(false);
        long casais = repository.countByTipo(Reserva.TipoReserva.CASAL);
        long individuais = repository.countByTipo(Reserva.TipoReserva.INDIVIDUAL);
        BigDecimal arrecadado = repository.totalArrecadado();
        BigDecimal previsto = repository.totalPrevisto();
        BigDecimal pendentePagamento = previsto.subtract(arrecadado);

        return new ResumoDTO(total, pagos, pendentes, casais, individuais,
                arrecadado, previsto, pendentePagamento);
    }

    public Reserva buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada: " + id));
    }
}
