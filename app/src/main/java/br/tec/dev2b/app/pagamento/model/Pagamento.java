package br.tec.dev2b.app.pagamento.model;

import br.tec.dev2b.app.assinatura.model.Assinatura;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pagamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assinatura_id")
    private Assinatura assinatura;

    /** ID do pagamento no Mercado Pago */
    @Column(name = "mp_payment_id", length = 100)
    private String mpPaymentId;

    /** ID da preference no MP (checkout pro) */
    @Column(name = "mp_preference_id", length = 200)
    private String mpPreferenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusPagamento status = StatusPagamento.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPagamento tipo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "descricao", length = 255)
    private String descricao;

    /** Método usado: credit_card, pix, boleto */
    @Column(name = "metodo_pagamento", length = 50)
    private String metodoPagamento;

    /** Dados brutos do último webhook recebido */
    @Column(name = "mp_raw_status", length = 50)
    private String mpRawStatus;

    @Column(name = "pago_em")
    private LocalDateTime pagoEm;

    /** QR Code PIX em formato texto (copia e cola) */
    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;

    /** QR Code PIX em base64 para renderizar imagem */
    @Column(name = "qr_code_base64", columnDefinition = "TEXT")
    private String qrCodeBase64;

    /** Data de expiração do PIX */
    @Column(name = "pix_expiration_date")
    private LocalDateTime pixExpirationDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
