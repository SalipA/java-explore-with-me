CREATE TABLE IF NOT EXISTS categories
(
    id   BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name VARCHAR(50)                             NOT NULL UNIQUE,
    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT size_name_cat CHECK ( LENGTH(name) >= 1 AND LENGTH(name) <= 50)
);

CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name  VARCHAR(250)                            NOT NULL UNIQUE,
    email VARCHAR(254)                            NOT NULL UNIQUE,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT size_name_u CHECK (LENGTH(name) >= 2 AND LENGTH(name) <= 250),
    CONSTRAINT email_size CHECK (LENGTH(email) >= 6 AND LENGTH(email) <= 254)
);

CREATE TABLE IF NOT EXISTS events
(
    id                 BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    annotation         VARCHAR(2000)                           NOT NULL,
    category_id        BIGINT                                  NOT NULL,
    created            TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    description        VARCHAR(7000)                           NOT NULL,
    event_date         TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    user_id            BIGINT                                  NOT NULL,
    paid               BOOLEAN                                 NOT NULL,
    participant_limit  INTEGER                                 NOT NULL,
    published_on       TIMESTAMP WITHOUT TIME ZONE,
    request_moderation BOOLEAN                                 NOT NULL,
    state              VARCHAR(9)                              NOT NULL,
    title              VARCHAR(120)                            not null,
    lat                FLOAT                                   NOT NULL,
    lon                FLOAT                                   NOT NULL,
    CONSTRAINT pk_events PRIMARY KEY (id),
    CONSTRAINT fk_users_e FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_categories_e FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT annotation_size CHECK (LENGTH(annotation) >= 20 AND LENGTH(annotation) <= 2000),
    CONSTRAINT description_size CHECK (LENGTH(description) >= 20 AND LENGTH(description) <= 7000),
    CONSTRAINT part_lim_pos_or_zero CHECK (participant_limit >= 0 ),
    CONSTRAINT title_size CHECK (LENGTH(title) >= 3 AND LENGTH(title) <= 120),
    CONSTRAINT enum_state_e CHECK (state IN ('PENDING', 'PUBLISHED', 'CANCELED'))
);

CREATE TABLE IF NOT EXISTS requests
(
    id       BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    created  TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    event_id BIGINT                                  NOT NULL,
    user_id  BIGINT                                  NOT NULL,
    state    VARCHAR(10),
    CONSTRAINT pk_requests PRIMARY KEY (id),
    CONSTRAINT fk_events_r FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_users_r FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT enum_state_r CHECK (state IN ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELED')),
    CONSTRAINT unique_event_user UNIQUE (event_id, user_id)
);

CREATE TABLE IF NOT EXISTS compilations
(
    id     BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    title  VARCHAR(50)                             NOT NULL,
    pinned BOOLEAN                                 NOT NULL,
    CONSTRAINT pk_compilations PRIMARY KEY (id),
    CONSTRAINT size_name_com CHECK (LENGTH(title) >= 1 AND LENGTH(title) <= 50)
);

CREATE TABLE IF NOT EXISTS events_compilations
(
    event_id       bigint NOT NULL REFERENCES events (id),
    compilation_id bigint NOT NULL REFERENCES compilations (id),
    CONSTRAINT pk_events_compilations PRIMARY KEY (event_id, compilation_id)
);