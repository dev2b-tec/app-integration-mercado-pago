package br.tec.dev2b.app.assinatura.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "planos_assinatura")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanoAssinatura {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PlanoTipo tipo;

    @Column(name = "valor_mensal", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMensal;

    /** ID do plano no Mercado Pago (preapproval_plan) */
    @Column(name = "mp_plan_id", length = 100)
    private String mpPlanId;

    @Column(name = "limite_ia_resumos", nullable = false)
    @Builder.Default
    private Integer limiteIaResumos = 50;

    @Column(name = "limite_ia_audios", nullable = false)
    @Builder.Default
    private Integer limiteIaAudios = 30;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
