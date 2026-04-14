package br.tec.dev2b.app.pagamento.service;

import br.tec.dev2b.app.assinatura.model.Assinatura;
import br.tec.dev2b.app.assinatura.model.PlanoAssinatura;
import br.tec.dev2b.app.assinatura.repository.AssinaturaRepository;
import br.tec.dev2b.app.assinatura.repository.PlanoAssinaturaRepository;
import br.tec.dev2b.app.infra.mercadopago.MercadoPagoProperties;
import br.tec.dev2b.app.pagamento.dto.CheckoutResponseDto;
import br.tec.dev2b.app.pagamento.dto.CriarPagamentoAvulsoDto;
import br.tec.dev2b.app.pagamento.dto.CriarPagamentoPixDto;
import br.tec.dev2b.app.pagamento.dto.PagamentoDto;
import br.tec.dev2b.app.pagamento.dto.PixResponseDto;
import br.tec.dev2b.app.pagamento.model.Pagamento;
import br.tec.dev2b.app.pagamento.model.StatusPagamento;
import br.tec.dev2b.app.pagamento.model.TipoPagamento;
import br.tec.dev2b.app.pagamento.repository.PagamentoRepository;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final AssinaturaRepository assinaturaRepository;
    private final PlanoAssinaturaRepository planoAssinaturaRepository;
    private final MercadoPagoProperties mpProperties;

    /** Retorna null se a URL for localhost — MP rejeita URLs não públicas */
    private String notificationUrl() {
        String url = mpProperties.notificationUrl();
        if (url == null || url.contains("localhost") || url.contains("127.0.0.1")) {
            log.warn("notificationUrl ignorada (URL local): {}", url);
            return null;
        }
        return url;
    }

    /**
     * Gera uma preferência de Checkout Pro para pagamento avulso
     * (ex: compra de créditos de IA).
     */
    @Transactional
    public CheckoutResponseDto criarCheckoutAvulso(CriarPagamentoAvulsoDto dto)
            throws MPException, MPApiException {

        // Salva pagamento como pendente antes de chamar o MP
        Pagamento pagamento = Pagamento.builder()
                .empresaId(dto.empresaId())
                .usuarioId(dto.usuarioId())
                .valor(dto.valor())
                .descricao(dto.descricao())
                .tipo(TipoPagamento.AVULSO)
                .status(StatusPagamento.PENDENTE)
                .build();
        pagamento = pagamentoRepository.save(pagamento);

        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .id(pagamento.getId().toString())
                .title(dto.descricao())
                .quantity(1)
                .unitPrice(dto.valor())
                .currencyId("BRL")
                .build();

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(mpProperties.backUrl().success())
                .failure(mpProperties.backUrl().failure())
                .pending(mpProperties.backUrl().pending())
                .build();

        PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .email(dto.payerEmail())
                .build();

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(List.of(item))
                .backUrls(backUrls)
                .payer(payer)
                .notificationUrl(notificationUrl())
                .externalReference(pagamento.getId().toString())
                .build();

        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(preferenceRequest);

        pagamento.setMpPreferenceId(preference.getId());
        pagamentoRepository.save(pagamento);

        log.info("Checkout criado para pagamento {} — preference {}", pagamento.getId(), preference.getId());

        return new CheckoutResponseDto(
                pagamento.getId().toString(),
                preference.getInitPoint(),
                preference.getSandboxInitPoint()
        );
    }

    /**
     * Gera um pagamento PIX e retorna o QR Code.
     */
    @Transactional
    public PixResponseDto criarPix(CriarPagamentoPixDto dto) throws MPException, MPApiException {
        // Se valor não foi enviado, busca pelo tipo de plano
        BigDecimal valor = dto.valor();
        String descricao = dto.descricao();
        if (valor == null && dto.planoTipo() != null) {
            PlanoAssinatura plano = planoAssinaturaRepository.findByTipo(dto.planoTipo())
                    .orElseThrow(() -> new IllegalArgumentException("Plano não encontrado: " + dto.planoTipo()));
            valor = plano.getValorMensal();
            descricao = "Assinatura " + plano.getNome() + " — DEV2B";
        }
        // TODO: remover após testes — força valor mínimo para teste de PIX
       final BigDecimal valorFinal = new BigDecimal("0.20");
       // final String descricaoFinal = descricao;

        Pagamento pagamento = Pagamento.builder()
                .empresaId(dto.empresaId())
                .usuarioId(dto.usuarioId())
                .valor(valorFinal)
                .descricao(dto.descricao())
                .tipo(TipoPagamento.AVULSO)
                .status(StatusPagamento.PENDENTE)
                .metodoPagamento("pix")
                .build();
        pagamento = pagamentoRepository.save(pagamento);

        String[] nomeParts = dto.payerNome() != null ? dto.payerNome().split(" ", 2) : new String[]{"", ""};
        OffsetDateTime dateOfExpiration = OffsetDateTime.now().plusMinutes(10);
        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .transactionAmount(valorFinal)
                .description(dto.descricao())
                .paymentMethodId("pix")
                .payer(PaymentPayerRequest.builder()
                        .email(dto.payerEmail())
                        .firstName(nomeParts[0])
                        .lastName(nomeParts.length > 1 ? nomeParts[1] : "")
                        .build())
                .externalReference(pagamento.getId().toString())
                .notificationUrl(notificationUrl())
                .dateOfExpiration(dateOfExpiration)
                .build();

        PaymentClient client = new PaymentClient();
        Payment payment;
        try {
            payment = client.create(request);
        } catch (MPApiException e) {
            log.error("MP API error ao criar PIX — HTTP {} | body: {}",
                    e.getStatusCode(),
                    e.getApiResponse() != null ? e.getApiResponse().getContent() : "sem body");
            throw e;
        }

        String qrCode = null;
        String qrCodeBase64 = null;
        if (payment.getPointOfInteraction() != null
                && payment.getPointOfInteraction().getTransactionData() != null) {
            qrCode = payment.getPointOfInteraction().getTransactionData().getQrCode();
            qrCodeBase64 = payment.getPointOfInteraction().getTransactionData().getQrCodeBase64();
        }

        pagamento.setMpPaymentId(payment.getId().toString());
        pagamento.setQrCode(qrCode);
        pagamento.setQrCodeBase64(qrCodeBase64);
        pagamentoRepository.save(pagamento);

        log.info("PIX criado — paymentId={} qrCode={}", payment.getId(), qrCode != null ? "ok" : "null");

        return new PixResponseDto(
                payment.getId().toString(),
                qrCode,
                qrCodeBase64,
                payment.getStatus(),
                valorFinal
        );
    }

    /** Webhook do MP notifica aprovação — atualiza pagamento local */
    @Transactional
    public void processarPagamentoAprovado(String mpPaymentId, String mpRawStatus, String metodoPagamento) {
        pagamentoRepository.findByMpPaymentId(mpPaymentId).ifPresent(p -> {
            p.setMpRawStatus(mpRawStatus);
            p.setMetodoPagamento(metodoPagamento);
            p.setStatus(resolverStatus(mpRawStatus));
            if (p.getStatus() == StatusPagamento.APROVADO) {
                p.setPagoEm(LocalDateTime.now());
            }
            pagamentoRepository.save(p);
            log.info("Pagamento {} atualizado para {}", mpPaymentId, p.getStatus());
        });
    }

    /** Registra o mp_payment_id quando o webhook chega antes da confirmação.
     *  externalReference = pagamento.getId().toString() (definido ao criar a preference/PIX) */
    @Transactional
    public void vincularPaymentId(String externalReference, String mpPaymentId) {
        try {
            UUID pagamentoId = UUID.fromString(externalReference);
            pagamentoRepository.findById(pagamentoId).ifPresent(p -> {
                if (p.getMpPaymentId() == null) {
                    p.setMpPaymentId(mpPaymentId);
                    pagamentoRepository.save(p);
                    log.info("mpPaymentId {} vinculado ao pagamento {}", mpPaymentId, pagamentoId);
                }
            });
        } catch (IllegalArgumentException e) {
            log.warn("externalReference não é UUID válido: {}", externalReference);
        }
    }

    public List<PagamentoDto> listarPorEmpresa(UUID empresaId) {
        return pagamentoRepository.findAllByEmpresaIdOrderByCreatedAtDesc(empresaId)
                .stream().map(PagamentoDto::from).toList();
    }

    public PagamentoDto buscarPorId(UUID id) {
        return PagamentoDto.from(pagamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pagamento não encontrado: " + id)));
    }

    private StatusPagamento resolverStatus(String mpStatus) {
        return switch (mpStatus) {
            case "approved" -> StatusPagamento.APROVADO;
            case "rejected", "cancelled" -> StatusPagamento.RECUSADO;
            case "refunded", "charged_back" -> StatusPagamento.REEMBOLSADO;
            case "in_process", "pending" -> StatusPagamento.PENDENTE;
            default -> StatusPagamento.EM_ANALISE;
        };
    }
}
