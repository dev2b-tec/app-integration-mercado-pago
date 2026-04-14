-- Campos para suporte a pagamento PIX
ALTER TABLE pagamentos ADD COLUMN IF NOT EXISTS qr_code TEXT;
ALTER TABLE pagamentos ADD COLUMN IF NOT EXISTS qr_code_base64 TEXT;
ALTER TABLE pagamentos ADD COLUMN IF NOT EXISTS pix_expiration_date TIMESTAMP;

-- URL de autorização da assinatura (init_point do MP)
ALTER TABLE assinaturas ADD COLUMN IF NOT EXISTS init_point VARCHAR(500);

-- Payment ID do PIX para ativar assinatura via webhook
ALTER TABLE assinaturas ADD COLUMN IF NOT EXISTS mp_pix_payment_id VARCHAR(100);
