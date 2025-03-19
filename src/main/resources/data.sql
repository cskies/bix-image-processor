-- Inserir planos
INSERT INTO plans (name, description, is_premium, daily_quota, monthly_price, created_at, updated_at)
VALUES
('Básico', 'Plano gratuito com recursos limitados', false, 5, 0.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Premium', 'Plano premium com recursos ilimitados', true, 1000, 19.90, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);