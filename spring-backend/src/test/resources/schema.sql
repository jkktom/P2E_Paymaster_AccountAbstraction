-- Test database schema initialization for H2

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    google_id VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    avatar VARCHAR(500),
    smart_wallet_address VARCHAR(42) UNIQUE,
    role_id SMALLINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id SMALLINT PRIMARY KEY,
    name VARCHAR(10) UNIQUE NOT NULL
);

-- Proposals table
CREATE TABLE IF NOT EXISTS proposals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    blockchain_proposal_id INTEGER UNIQUE NOT NULL,
    description TEXT NOT NULL,
    proposer_address VARCHAR(42) NOT NULL,
    deadline TIMESTAMP NOT NULL,
    executed BOOLEAN NOT NULL DEFAULT FALSE,
    canceled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    tx_hash VARCHAR(66),
    proposer_google_id VARCHAR(50),
    db_created_at TIMESTAMP NOT NULL
);

-- Proposal vote counts table
CREATE TABLE IF NOT EXISTS proposal_vote_counts (
    proposal_id BIGINT PRIMARY KEY,
    for_votes DECIMAL(30,0) NOT NULL DEFAULT 0,
    against_votes DECIMAL(30,0) NOT NULL DEFAULT 0,
    total_voters INTEGER NOT NULL DEFAULT 0,
    for_voters INTEGER NOT NULL DEFAULT 0,
    against_voters INTEGER NOT NULL DEFAULT 0,
    last_updated TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

-- User votes table
CREATE TABLE IF NOT EXISTS user_votes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    proposal_id BIGINT NOT NULL,
    user_google_id VARCHAR(50) NOT NULL,
    voter_wallet_address VARCHAR(42) NOT NULL,
    support BOOLEAN NOT NULL,
    voting_power DECIMAL(30,0) NOT NULL,
    tx_hash VARCHAR(66),
    voted_at TIMESTAMP NOT NULL,
    recorded_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_user_votes_proposal_user UNIQUE(proposal_id, user_google_id)
);

-- Insert default roles
INSERT INTO roles (id, name) VALUES (1, 'ADMIN'), (2, 'USER');
