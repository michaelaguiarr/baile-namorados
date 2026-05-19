---
name: redirect-referer-absoluto
description: O parâmetro referer no toggle de pagamento deve ser path absoluto — sem "/" inicial o Spring resolve relativo ao path atual e causa 404
metadata:
  type: feedback
---

O form hidden de `referer` no `index.html` (toggle de pagamento) deve sempre produzir um path absoluto.

**Why:** Spring MVC resolve `redirect:?filtro=pagos` relativo ao path atual da requisição (`/reservas/{id}/toggle-pagamento`), resultando em `/reservas/{id}/toggle-pagamento?filtro=pagos` — rota inexistente → 404. Descoberto e corrigido em sessão de revisão.

**How to apply:** Ao escrever ou revisar qualquer `referer`/redirect construído a partir de `queryString` no Thymeleaf, garantir que o valor começa com `/`:

```html
th:value="${'/' + (#httpServletRequest.queryString != null ? '?' + #httpServletRequest.queryString : '')}"
```

Errado: `'?' + queryString` → `"?filtro=pagos"`
Correto: `'/?' + queryString` → `"/?filtro=pagos"`