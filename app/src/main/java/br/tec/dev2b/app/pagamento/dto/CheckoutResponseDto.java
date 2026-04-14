package br.tec.dev2b.app.pagamento.dto;

/** Retornado ao frontend para redirecionar ao checkout do MP */
public record CheckoutResponseDto(
        String pagamentoId,
        String checkoutUrl,
        String sandboxUrl
) {}
