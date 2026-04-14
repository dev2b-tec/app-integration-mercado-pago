-- Tabela de planos disponíveis no sistema
CREATE TABLE planos_assinatura (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome                VARCHAR(100)   NOT NULL,
    tipo                VARCHAR(30)    NOT NULL,
    valor_mensal        NUMERIC(10, 2) NOT NULL,
    mp_plan_id          VARCHAR(100),
    limite_ia_resumos   INT            NOT NULL DEFAULT 50,
    limite_ia_audios    INT            NOT NULL DEFAULT 30,
    ativo               BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_plano_tipo UNIQUE (tipo)
);

-- Dados iniciais dos planos
INSERT INTO planos_assinatura (nome, tipo, valor_mensal, limite_ia_resumos, limite_ia_audios) VALUES
    ('Smart IA',   'SMART_IA',   39.90,  50,  30),
    ('Pro IA',     'PRO_IA',     79.90, 200, 200),
    ('Clinica IA', 'CLINICA_IA', 149.90, 9999, 9999);

-- Tabela de assinaturas recorrentes
CREATE TABLE assinaturas (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id          UUID           NOT NULL,
    usuario_id          UUID           NOT NULL,
    plano_id            UUID           NOT NULL REFERENCES planos_assinatura(id),
    mp_preapproval_id   VARCHAR(100),
    mp_payer_email      VARCHAR(200),
    status              VARCHAR(20)    NOT NULL DEFAULT 'PENDENTE',
    proxima_cobranca    DATE,
    created_at          TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP      NOT NULL DEFAULT NOW(),
    cancelada_em        TIMESTAMP,
    CONSTRAINT uk_assinatura_preapproval UNIQUE (mp_preapproval_id)
);

CREATE INDEX idx_assinatura_empresa ON assinaturas(empresa_id);
CREATE INDEX idx_assinatura_status ON assinaturas(status);

-- Tabela de pagamentos individuais
CREATE TABLE pagamentos (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id          UUID           NOT NULL,
    usuario_id          UUID           NOT NULL,
    assinatura_id       UUID           REFERENCES assinaturas(id),
    mp_payment_id       VARCHAR(100),
    mp_preference_id    VARCHAR(200),
    status              VARCHAR(20)    NOT NULL DEFAULT 'PENDENTE',
    tipo                VARCHAR(20)    NOT NULL,
    valor               NUMERIC(10, 2) NOT NULL,
    descricao           VARCHAR(255),
    metodo_pagamento    VARCHAR(50),
    mp_raw_status       VARCHAR(50),
    pago_em             TIMESTAMP,
    created_at          TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_pagamento_mp_id UNIQUE (mp_payment_id)
);

CREATE INDEX idx_pagamento_empresa ON pagamentos(empresa_id);
CREATE INDEX idx_pagamento_status ON pagamentos(status);
CREATE INDEX idx_pagamento_preference ON pagamentos(mp_preference_id);
