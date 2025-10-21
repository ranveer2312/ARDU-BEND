-- Create the 'admins' table
CREATE TABLE admins (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    mobile_number VARCHAR(255) NOT NULL,
    mobile_verified BOOLEAN DEFAULT FALSE,
    email_verified BOOLEAN DEFAULT FALSE,
    whatsapp_number VARCHAR(255),
    whatsapp_verified BOOLEAN DEFAULT FALSE,
    dl_number VARCHAR(255),
    father_name VARCHAR(255),
    date_of_birth DATE,
    badge_number VARCHAR(255),
    address VARCHAR(255),
    blood_group VARCHAR(255),
    role VARCHAR(255) NOT NULL,
    main_admin BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    image_url VARCHAR(1000),
    image_public_id VARCHAR(500),
    nominee_name VARCHAR(255),
    nominee_relationship VARCHAR(255),
    nominee_contact_number VARCHAR(255),
    PRIMARY KEY (id)
);

-- Create the 'users' table
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    mobile_number VARCHAR(255) NOT NULL UNIQUE,
    mobile_verified BOOLEAN DEFAULT FALSE,
    email_verified BOOLEAN DEFAULT FALSE,
    whatsapp_number VARCHAR(255),
    whatsapp_verified BOOLEAN DEFAULT FALSE,
    role VARCHAR(255),
    approval_status VARCHAR(255),
    father_name VARCHAR(255),
    date_of_birth DATE,
    dl_number VARCHAR(255),
    badge_number VARCHAR(255),
    address VARCHAR(255),
    blood_group VARCHAR(255),
    nominee_name VARCHAR(255),
    nominee_relationship VARCHAR(255),
    nominee_contact_number VARCHAR(255),
    active BOOLEAN DEFAULT FALSE,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    date_of_joining_or_renewal DATE,
    expiry_date DATE,
    image_url VARCHAR(1000),
    image_public_id VARCHAR(500),
    otp_code VARCHAR(255),
    otp_expiry DATETIME(6),
    PRIMARY KEY (id)
);

-- Create the 'post' table
CREATE TABLE post (
    id BIGINT NOT NULL AUTO_INCREMENT,
    content_url VARCHAR(255),
    type VARCHAR(255),
    approved BOOLEAN,
    created_at DATETIME(6),
    expires_at DATETIME(6),
    user_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Create the 'comment' table
CREATE TABLE comment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    text VARCHAR(255),
    created_at DATETIME(6),
    user_id BIGINT,
    post_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (post_id) REFERENCES post (id)
);

-- Create the 'reaction' table
CREATE TABLE reaction (
    id BIGINT NOT NULL AUTO_INCREMENT,
    type VARCHAR(255),
    created_at DATETIME(6),
    user_id BIGINT,
    post_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (post_id) REFERENCES post (id)
);

-- Create the 'story' table
CREATE TABLE story (
    id BIGINT NOT NULL AUTO_INCREMENT,
    content_url VARCHAR(255),
    created_at DATETIME(6),
    expires_at DATETIME(6),
    user_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);