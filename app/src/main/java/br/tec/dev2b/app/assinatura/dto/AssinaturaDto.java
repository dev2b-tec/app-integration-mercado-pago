package br.tec.dev2b.app.assinatura.dto;

import br.tec.dev2b.app.assinatura.model.Assinatura;
import br.tec.dev2b.app.assinatura.model.StatusAssinatura;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AssinaturaDto(
        UUID id,
        UUID empresaId,
        UUID usuarioId,
        PlanoAssinaturaDto plano,
        StatusAssinatura status,
        String mpPreapprovalId,
        String mpCardPaymentId,
        /** URL de autorização do MP — presente quando a assinatura ainda não foi autorizada via cartão */
        String initPoint,
        LocalDate proximaCobranca,
        LocalDateTime createdAt
) {
    public static AssinaturaDto from(Assinatura a) {
        return new AssinaturaDto(
                a.getId(),
                a.getEmpresaId(),
                a.getUsuarioId(),
                PlanoAssinaturaDto.from(a.getPlano()),
                a.getStatus(),
                a.getMpPreapprovalId(),
                a.getMpCardPaymentId(),
                a.getInitPoint(),
                a.getProximaCobranca(),
                a.getCreatedAt()
        );
    }
}
