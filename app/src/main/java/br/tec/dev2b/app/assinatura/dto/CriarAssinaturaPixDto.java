package br.tec.dev2b.app.assinatura.dto;

import br.tec.dev2b.app.assinatura.model.PlanoTipo;

import java.util.UUID;

public record CriarAssinaturaPixDto(
        UUID empresaId,
        UUID usuarioId,
        PlanoTipo planoTipo,
        String payerEmail,
        String payerNome
) {}
