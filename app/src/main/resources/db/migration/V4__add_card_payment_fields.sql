-- Suporte a cobrança direta com cartão (PaymentClient)
ALTER TABLE assinaturas ADD COLUMN IF NOT EXISTS mp_card_payment_id VARCHAR(100);
ALTER TABLE assinaturas ADD COLUMN IF NOT EXISTS mp_customer_id     VARCHAR(100);
ALTER TABLE assinaturas ADD COLUMN IF NOT EXISTS mp_card_id         VARCHAR(100);
