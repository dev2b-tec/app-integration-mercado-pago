package br.tec.dev2b.app.pagamento.dto;

import java.math.BigDecimal;

public record PixResponseDto(
        String paymentId,
        String qrCode,
        String qrCodeBase64,
        String status,
        BigDecimal valor
) {}
