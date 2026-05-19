package com.igreja.baile.controller;

import com.igreja.baile.dto.ReservaForm;
import com.igreja.baile.model.Reserva;
import com.igreja.baile.service.ReservaService;
import com.igreja.baile.exception.NomeDuplicadoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService service;

    // ─── DASHBOARD ───────────────────────────────────────────────────────────

    @GetMapping("/")
    public String dashboard(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String filtro,
            HttpServletRequest request,
            Model model) {

        List<Reserva> reservas;
        if (busca != null && !busca.isBlank()) {
            reservas = service.buscarPorNome(busca);
        } else if ("pagos".equals(filtro)) {
            reservas = service.listarPorStatus(true);
        } else if ("pendentes".equals(filtro)) {
            reservas = service.listarPorStatus(false);
        } else {
            reservas = service.listarTodas();
        }

        String qs = request.getQueryString();
        model.addAttribute("currentQuery", qs != null ? "?" + qs : "");
        model.addAttribute("reservas", reservas);
        model.addAttribute("resumo", service.resumo());
        model.addAttribute("busca", busca);
        model.addAttribute("filtro", filtro);
        model.addAttribute("form", new ReservaForm());
        return "index";
    }

    // ─── SALVAR (criar ou editar) ─────────────────────────────────────────────

    @PostMapping("/reservas/salvar")
    public String salvar(
            @Valid @ModelAttribute("form") ReservaForm form,
            BindingResult result,
            RedirectAttributes redirect,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("reservas", service.listarTodas());
            model.addAttribute("resumo", service.resumo());
            model.addAttribute("currentQuery", "");
            model.addAttribute("modalAberto", true);
            return "index";
        }
        try {
            service.salvar(form);
        } catch (NomeDuplicadoException e) {
            log.warn("Nome duplicado rejeitado: {}", e.getMessage());
            model.addAttribute("reservas", service.listarTodas());
            model.addAttribute("resumo", service.resumo());
            model.addAttribute("currentQuery", "");
            if (form.getId() != null) {
                model.addAttribute("errNome", e.getMessage());
                model.addAttribute("modalEdicaoAberto", true);
            } else {
                result.rejectValue("nome", "duplicado", e.getMessage());
                model.addAttribute("modalAberto", true);
            }
            return "index";
        }
        redirect.addFlashAttribute("sucesso",
                form.getId() == null ? "Reserva cadastrada com sucesso!" : "Reserva atualizada!");
        return "redirect:/";
    }

    // ─── TOGGLE PAGAMENTO (via botão rápido) ─────────────────────────────────

    @PostMapping("/reservas/{id}/toggle-pagamento")
    public String togglePagamento(
            @PathVariable Long id,
            @RequestParam(defaultValue = "") String referer,
            RedirectAttributes redirect) {
        service.togglePagamento(id);
        redirect.addFlashAttribute("sucesso", "Status de pagamento atualizado.");
        String back = referer.isBlank() ? "/" : referer;
        return "redirect:" + back;
    }

    // ─── EXCLUIR ─────────────────────────────────────────────────────────────

    @PostMapping("/reservas/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        service.excluir(id);
        redirect.addFlashAttribute("sucesso", "Reserva removida.");
        return "redirect:/";
    }

    // ─── RELATÓRIOS ──────────────────────────────────────────────────────────

    @GetMapping("/relatorio/financeiro")
    public String relatorioFinanceiro(Model model) {
        model.addAttribute("pagos", service.listarPorStatus(true));
        model.addAttribute("pendentes", service.listarPorStatus(false));
        model.addAttribute("resumo", service.resumo());
        return "relatorio/financeiro";
    }

    @GetMapping("/relatorio/entrada")
    public String relatorioEntrada(Model model) {
        model.addAttribute("reservas", service.listarTodas());
        model.addAttribute("resumo", service.resumo());
        return "relatorio/entrada";
    }
}
