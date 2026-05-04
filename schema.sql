-- =============================================================================
-- DDL - Omnilink API
-- Banco de dados: MySQL 8.x
-- =============================================================================

CREATE DATABASE IF NOT EXISTS omnilink_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE omnilink_db;

-- -----------------------------------------------------------------------------
-- Tabela: users
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    username   VARCHAR(50)  NOT NULL,
    email      VARCHAR(100) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    CONSTRAINT pk_users      PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email    UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('USER', 'ADMIN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Tabela: vehicles
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vehicles (
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    brand      VARCHAR(50)    NOT NULL,
    model      VARCHAR(100)   NOT NULL,
    year       INT            NOT NULL,
    plate      VARCHAR(10)    NOT NULL,
    color      VARCHAR(30),
    price      DECIMAL(12, 2),
    status     VARCHAR(20)    NOT NULL DEFAULT 'DISPONIVEL',
    created_at DATETIME(6)    NOT NULL,
    updated_at DATETIME(6)    NOT NULL,
    CONSTRAINT pk_vehicles       PRIMARY KEY (id),
    CONSTRAINT uq_vehicles_plate UNIQUE (plate),
    CONSTRAINT ck_vehicles_year  CHECK (year >= 1900 AND year <= 2027),
    CONSTRAINT ck_vehicles_price CHECK (price IS NULL OR price > 0),
    CONSTRAINT ck_vehicles_status CHECK (status IN ('DISPONIVEL', 'RESERVADO', 'VENDIDO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Tabela: customers
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    cpf        VARCHAR(14)  NOT NULL,
    email      VARCHAR(100) NOT NULL,
    phone      VARCHAR(20),
    address    VARCHAR(255),
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    CONSTRAINT pk_customers       PRIMARY KEY (id),
    CONSTRAINT uq_customers_cpf   UNIQUE (cpf),
    CONSTRAINT uq_customers_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Índices
-- -----------------------------------------------------------------------------
CREATE INDEX idx_vehicles_brand  ON vehicles (brand);
CREATE INDEX idx_vehicles_status ON vehicles (status);
CREATE INDEX idx_customers_name  ON customers (name);
