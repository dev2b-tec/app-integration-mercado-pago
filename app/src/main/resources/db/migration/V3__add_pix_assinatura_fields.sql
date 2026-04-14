-- Coluna para rastrear o Payment ID do PIX vinculado à assinatura
ALTER TABLE assinaturas ADD COLUMN IF NOT EXISTS mp_pix_payment_id VARCHAR(100);

-- Novo valor de status para assinaturas aguardando confirmação PIX
-- (apenas documentação — o valor é armazenado como string no enum do JPA)
