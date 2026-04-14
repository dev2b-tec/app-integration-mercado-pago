package br.tec.dev2b.app.infra.webhook;

import br.tec.dev2b.app.assinatura.model.StatusAssinatura;
import br.tec.dev2b.app.assinatura.service.AssinaturaService;
import br.tec.dev2b.app.infra.mercadopago.MercadoPagoProperties;
import br.tec.dev2b.app.pagamento.service.PagamentoService;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preapproval.PreapprovalClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preapproval.Preapproval;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoWebhookController {

    private final PagamentoService pagamentoService;
    private final AssinaturaService assinaturaService;
    private final MercadoPagoProperties mpProperties;

    /**
     * Endpoint notificado pelo Mercado Pago para pagamentos e assinaturas.
     * O MP envia: { "action": "payment.updated", "type": "payment", "data": { "id": "..." } }
     */
    @PostMapping("/mercadopago")
    public ResponseEntity<Void> receberNotificacao(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId
    ) {
        log.info("Webhook MP recebido: type={} action={}", payload.get("type"), payload.get("action"));

        try {
            String type = String.valueOf(payload.get("type"));
            Map<?, ?> data = (Map<?, ?>) payload.get("data");
            if (data == null) return ResponseEntity.ok().build();

            String resourceId = String.valueOf(data.get("id"));

            switch (type) {
                case "payment" -> processarPagamento(resourceId);
                case "preapproval" -> processarAssinatura(resourceId);
                default -> log.debug("Tipo de webhook não tratado: {}", type);
            }
        } catch (Exception e) {
            // Retornar 200 mesmo em erro para o MP não reenviar indefinidamente.
            // O erro já foi logado para investigação.
            log.error("Erro processando webhook MP: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok().build();
    }

    private void processarPagamento(String mpPaymentId) throws MPException, MPApiException {
        PaymentClient client = new PaymentClient();
        Payment payment = client.get(Long.parseLong(mpPaymentId));

        String externalReference = payment.getExternalReference();
        String status = payment.getStatus();
        String metodoPagamento = payment.getPaymentMethodId();

        // Vincula payment_id ao pagamento local via externalReference (= pagamento.id)
        if (externalReference != null) {
            pagamentoService.vincularPaymentId(externalReference, mpPaymentId);
        }
        pagamentoService.processarPagamentoAprovado(mpPaymentId, status, metodoPagamento);

        // Se foi pagamento PIX aprovado, ativa a assinatura vinculada
        if ("approved".equals(status) && "pix".equals(metodoPagamento)) {
            assinaturaService.ativarPorPixPaymentId(mpPaymentId);
        }
    }

    private void processarAssinatura(String mpPreapprovalId) throws MPException, MPApiException {
        PreapprovalClient client = new PreapprovalClient();
        Preapproval preApproval = client.get(mpPreapprovalId);

        StatusAssinatura novoStatus = switch (preApproval.getStatus()) {
            case "authorized" -> StatusAssinatura.ATIVA;
            case "paused" -> StatusAssinatura.PAUSADA;
            case "cancelled" -> StatusAssinatura.CANCELADA;
            default -> StatusAssinatura.PENDENTE;
        };

        assinaturaService.atualizarStatus(mpPreapprovalId, novoStatus);
    }
}
