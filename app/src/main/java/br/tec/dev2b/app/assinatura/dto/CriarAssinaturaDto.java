package br.tec.dev2b.app.assinatura.dto;

import br.tec.dev2b.app.assinatura.model.PlanoTipo;

import java.util.UUID;

public record CriarAssinaturaDto(
        UUID empresaId,
        UUID usuarioId,
        String payerEmail,
        PlanoTipo planoTipo,
        String cardTokenId,
        String payerDocumento,
        String payerTipoDocumento,
        String deviceId
) {}
