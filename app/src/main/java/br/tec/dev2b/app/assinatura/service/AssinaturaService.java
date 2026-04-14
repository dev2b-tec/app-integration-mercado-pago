package br.tec.dev2b.app.assinatura.service;

import br.tec.dev2b.app.assinatura.dto.AssinaturaDto;
import br.tec.dev2b.app.assinatura.dto.CriarAssinaturaDto;
import br.tec.dev2b.app.assinatura.dto.CriarAssinaturaPixDto;
import br.tec.dev2b.app.assinatura.model.Assinatura;
import br.tec.dev2b.app.assinatura.model.PlanoAssinatura;
import br.tec.dev2b.app.assinatura.model.StatusAssinatura;
import br.tec.dev2b.app.assinatura.repository.AssinaturaRepository;
import br.tec.dev2b.app.assinatura.repository.PlanoAssinaturaRepository;
import br.tec.dev2b.app.infra.mercadopago.MercadoPagoProperties;
import br.tec.dev2b.app.pagamento.dto.CriarPagamentoPixDto;
import br.tec.dev2b.app.pagamento.dto.PixResponseDto;
import br.tec.dev2b.app.pagamento.service.PagamentoService;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssinaturaService {

    private final AssinaturaRepository assinaturaRepository;
    private final PlanoAssinaturaRepository planoRepository;
    private final MercadoPagoProperties mpProperties;
    private final PagamentoService pagamentoService;

    /** Cria uma assinatura cobrando diretamente no cartão via cardTokenId */
    @Transactional
    public AssinaturaDto criar(CriarAssinaturaDto dto) throws MPException, MPApiException {
        PlanoAssinatura plano = planoRepository.findByTipo(dto.planoTipo())
                .orElseThrow(() -> new IllegalArgumentException("Plano não encontrado: " + dto.planoTipo()));

        // TODO: remover após testes — força valor mínimo para teste de cartão
        final java.math.BigDecimal valorTeste = new java.math.BigDecimal("1.00");

        com.mercadopago.client.common.IdentificationRequest identification = null;
        if (dto.payerDocumento() != null && dto.payerTipoDocumento() != null) {
            identification = com.mercadopago.client.common.IdentificationRequest.builder()
                    .type(dto.payerTipoDocumento())
                    .number(dto.payerDocumento().replaceAll("\\D", ""))
                    .build();
        }

        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .transactionAmount(valorTeste)
                .description(plano.getNome() + " — DEV2B")
                .token(dto.cardTokenId())
                .installments(1)
                .payer(PaymentPayerRequest.builder()
                        .email(dto.payerEmail())
                        .identification(identification)
                        .build())
                .build();

        PaymentClient client = new PaymentClient();
        Payment payment;
        try {
            com.mercadopago.core.MPRequestOptions requestOptions = null;
            if (dto.deviceId() != null && !dto.deviceId().isBlank()) {
                requestOptions = com.mercadopago.core.MPRequestOptions.builder()
                        .customHeaders(java.util.Map.of("X-meli-session-id", dto.deviceId()))
                        .build();
            }
            payment = requestOptions != null
                    ? client.create(request, requestOptions)
                    : client.create(request);
        } catch (MPApiException e) {
            log.error("MP API error ao cobrar cartão — HTTP {} | body: {}",
                    e.getStatusCode(),
                    e.getApiResponse() != null ? e.getApiResponse().getContent() : "sem body");
            throw e;
        }

        StatusAssinatura status = "approved".equals(payment.getStatus())
                ? StatusAssinatura.ATIVA
                : StatusAssinatura.PENDENTE;

        Assinatura assinatura = Assinatura.builder()
                .empresaId(dto.empresaId())
                .usuarioId(dto.usuarioId())
                .plano(plano)
                .mpPayerEmail(dto.payerEmail())
                .mpCardPaymentId(payment.getId().toString())
                .status(status)
                .proximaCobranca(status == StatusAssinatura.ATIVA ? LocalDate.now().plusMonths(1) : null)
                .build();

        log.info("Assinatura cartão criada — paymentId={} status={} statusDetail={}",
                payment.getId(), payment.getStatus(), payment.getStatusDetail());
        return AssinaturaDto.from(assinaturaRepository.save(assinatura));
    }

    /**
     * Gera cobrança PIX para o primeiro mês de assinatura.
     * A assinatura fica PENDENTE_PIX até o webhook confirmar o pagamento.
     */
    @Transactional
    public PixResponseDto criarPix(CriarAssinaturaPixDto dto) throws MPException, MPApiException {
        PlanoAssinatura plano = planoRepository.findByTipo(dto.planoTipo())
                .orElseThrow(() -> new IllegalArgumentException("Plano não encontrado: " + dto.planoTipo()));

        PixResponseDto pixResponse = pagamentoService.criarPix(new CriarPagamentoPixDto(
                dto.empresaId(),
                dto.usuarioId(),
                plano.getValorMensal(),
                "Assinatura " + plano.getNome() + " — DEV2B",
                dto.payerEmail(),
                dto.payerNome(),
                dto.planoTipo()
        ));

        Assinatura assinatura = Assinatura.builder()
                .empresaId(dto.empresaId())
                .usuarioId(dto.usuarioId())
                .plano(plano)
                .mpPayerEmail(dto.payerEmail())
                .mpPixPaymentId(pixResponse.paymentId())
                .status(StatusAssinatura.PENDENTE_PIX)
                .build();

        assinaturaRepository.save(assinatura);
        log.info("Assinatura PIX criada para empresa={} plano={} paymentId={}", dto.empresaId(), dto.planoTipo(), pixResponse.paymentId());

        return pixResponse;
    }

    public AssinaturaDto buscarPorId(UUID id) {
        return AssinaturaDto.from(assinaturaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assinatura não encontrada: " + id)));
    }

    @Transactional(readOnly = true)
    public List<AssinaturaDto> listarPorEmpresa(UUID empresaId) {
        return assinaturaRepository.findAllByEmpresaId(empresaId)
                .stream().map(AssinaturaDto::from).toList();
    }

    /** Chamado pelo webhook quando o MP confirma ou altera o status */
    @Transactional
    public void atualizarStatus(String mpPreapprovalId, StatusAssinatura novoStatus) {
        assinaturaRepository.findByMpPreapprovalId(mpPreapprovalId).ifPresent(a -> {
            a.setStatus(novoStatus);
            assinaturaRepository.save(a);
            log.info("Assinatura {} atualizada para {}", mpPreapprovalId, novoStatus);
        });
    }

    /** Chamado pelo webhook quando o pagamento PIX é aprovado: ativa a assinatura por 30 dias */
    @Transactional
    public void ativarPorPixPaymentId(String mpPixPaymentId) {
        assinaturaRepository.findByMpPixPaymentId(mpPixPaymentId).ifPresent(a -> {
            if (a.getStatus() == StatusAssinatura.PENDENTE_PIX) {
                a.setStatus(StatusAssinatura.ATIVA);
                a.setProximaCobranca(LocalDate.now().plusMonths(1));
                assinaturaRepository.save(a);
                log.info("Assinatura PIX {} ativada, próxima cobrança em {}", mpPixPaymentId, a.getProximaCobranca());
            }
        });
    }

    /** Cancela a assinatura localmente */
    @Transactional
    public AssinaturaDto cancelar(UUID id) throws MPException, MPApiException {
        Assinatura assinatura = assinaturaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assinatura não encontrada: " + id));

        assinatura.setStatus(StatusAssinatura.CANCELADA);
        assinatura.setCanceladaEm(java.time.LocalDateTime.now());
        return AssinaturaDto.from(assinaturaRepository.save(assinatura));
    }

    public List<PlanoAssinatura> listarPlanos() {
        return planoRepository.findAllByAtivoTrue();
    }
}
