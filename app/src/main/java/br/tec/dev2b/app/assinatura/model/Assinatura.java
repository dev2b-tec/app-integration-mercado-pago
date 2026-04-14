package br.tec.dev2b.app.assinatura.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "assinaturas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assinatura {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_id", nullable = false)
    private PlanoAssinatura plano;

    /** ID da subscription no Mercado Pago (preapproval) */
    @Column(name = "mp_preapproval_id", length = 100)
    private String mpPreapprovalId;

    /** Email do pagador registrado no MP */
    @Column(name = "mp_payer_email", length = 200)
    private String mpPayerEmail;

    /** URL de autorização retornada pelo MP (init_point) */
    @Column(name = "init_point", length = 500)
    private String initPoint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusAssinatura status = StatusAssinatura.PENDENTE;

    @Column(name = "proxima_cobranca")
    private LocalDate proximaCobranca;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "cancelada_em")
    private LocalDateTime canceladaEm;

    /** Payment ID no MP gerado para cobrança via PIX (primeiro mês) */
    @Column(name = "mp_pix_payment_id", length = 100)
    private String mpPixPaymentId;

    /** Payment ID no MP da cobrança direta por cartão */
    @Column(name = "mp_card_payment_id", length = 100)
    private String mpCardPaymentId;

    /** Customer ID no MP — usado para cobranças recorrentes */
    @Column(name = "mp_customer_id", length = 100)
    private String mpCustomerId;

    /** Card ID salvo no MP — usado para cobranças recorrentes */
    @Column(name = "mp_card_id", length = 100)
    private String mpCardId;

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
