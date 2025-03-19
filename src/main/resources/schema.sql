-- Arquivo schema.sql em src/main/resources
CREATE TABLE IF NOT EXISTS plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_premium BOOLEAN NOT NULL,
    daily_quota INTEGER NOT NULL,
    monthly_price DOUBLE NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email_verified BOOLEAN,
    verification_token VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);