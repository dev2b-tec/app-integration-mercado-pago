package br.tec.dev2b.app.assinatura.model;

public enum StatusAssinatura {
    PENDENTE,
    /** Aguardando confirmação de pagamento PIX */
    PENDENTE_PIX,
    ATIVA,
    PAUSADA,
    CANCELADA,
    INADIMPLENTE
}
