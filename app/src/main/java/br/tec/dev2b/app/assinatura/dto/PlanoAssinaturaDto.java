package br.tec.dev2b.app.assinatura.dto;

import br.tec.dev2b.app.assinatura.model.PlanoAssinatura;
import br.tec.dev2b.app.assinatura.model.PlanoTipo;

import java.math.BigDecimal;
import java.util.UUID;

public record PlanoAssinaturaDto(
        UUID id,
        String nome,
        PlanoTipo tipo,
        BigDecimal valorMensal,
        Integer limiteIaResumos,
        Integer limiteIaAudios
) {
    public static PlanoAssinaturaDto from(PlanoAssinatura p) {
        return new PlanoAssinaturaDto(
                p.getId(),
                p.getNome(),
                p.getTipo(),
                p.getValorMensal(),
                p.getLimiteIaResumos(),
                p.getLimiteIaAudios()
        );
    }
}
