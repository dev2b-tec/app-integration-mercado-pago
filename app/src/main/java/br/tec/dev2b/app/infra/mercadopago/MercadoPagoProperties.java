package br.tec.dev2b.app.infra.mercadopago;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mercadopago")
public record MercadoPagoProperties(
        String accessToken,
        String publicKey,
        String webhookSecret,
        String notificationUrl,
        BackUrls backUrl
) {
    public record BackUrls(String success, String failure, String pending) {}
}
