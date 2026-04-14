package br.tec.dev2b.app.infra.config;

import br.tec.dev2b.app.infra.mercadopago.MercadoPagoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class MpConfigController {

    private final MercadoPagoProperties mpProperties;

    @GetMapping("/mp")
    public ResponseEntity<Map<String, String>> mpConfig() {
        return ResponseEntity.ok(Map.of("publicKey", mpProperties.publicKey() != null ? mpProperties.publicKey() : ""));
    }
}
