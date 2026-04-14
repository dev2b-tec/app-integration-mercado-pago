package br.tec.dev2b.app.infra.mercadopago;

import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoClientConfig {

    private final MercadoPagoProperties properties;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(properties.accessToken());
        log.info("MercadoPago SDK configurado (token: {}...)", properties.accessToken().substring(0, 12));
    }
}
