package br.tec.dev2b.app.pagamento.dto;

import br.tec.dev2b.app.assinatura.model.PlanoTipo;

import java.math.BigDecimal;
import java.util.UUID;

public record CriarPagamentoPixDto(
        UUID empresaId,
        UUID usuarioId,
        BigDecimal valor,
        String descricao,
        String payerEmail,
        String payerNome,
        PlanoTipo planoTipo
) {}
