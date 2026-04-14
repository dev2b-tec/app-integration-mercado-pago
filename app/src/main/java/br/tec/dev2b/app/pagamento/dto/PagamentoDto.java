package br.tec.dev2b.app.pagamento.dto;

import br.tec.dev2b.app.pagamento.model.Pagamento;
import br.tec.dev2b.app.pagamento.model.StatusPagamento;
import br.tec.dev2b.app.pagamento.model.TipoPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PagamentoDto(
        UUID id,
        UUID empresaId,
        UUID usuarioId,
        StatusPagamento status,
        TipoPagamento tipo,
        BigDecimal valor,
        String descricao,
        String metodoPagamento,
        String mpPaymentId,
        LocalDateTime pagoEm,
        LocalDateTime createdAt
) {
    public static PagamentoDto from(Pagamento p) {
        return new PagamentoDto(
                p.getId(),
                p.getEmpresaId(),
                p.getUsuarioId(),
                p.getStatus(),
                p.getTipo(),
                p.getValor(),
                p.getDescricao(),
                p.getMetodoPagamento(),
                p.getMpPaymentId(),
                p.getPagoEm(),
                p.getCreatedAt()
        );
    }
}
