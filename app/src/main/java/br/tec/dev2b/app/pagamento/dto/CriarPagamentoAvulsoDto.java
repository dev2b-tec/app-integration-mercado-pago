package br.tec.dev2b.app.pagamento.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CriarPagamentoAvulsoDto(
        UUID empresaId,
        UUID usuarioId,
        BigDecimal valor,
        String descricao,
        String payerEmail,
        String payerNome
) {}
