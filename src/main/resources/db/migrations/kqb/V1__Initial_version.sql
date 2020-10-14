CREATE TABLE casters(
    name VARCHAR(50) PRIMARY KEY,
    stream_link CLOB NOT NULL,
    bio CLOB NOT NULL,
    games_casted INTEGER NOT NULL
);

CREATE TABLE matches(
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    season VARCHAR(10) NOT NULL,
    circuit VARCHAR(10) NOT NULL,
    division VARCHAR(10) NOT NULL,
    conference VARCHAR(10) NOT NULL,
    week VARCHAR(20) NOT NULL,
    away_team VARCHAR(100) NOT NULL,
    home_team VARCHAR(100) NOT NULL,
    color INTEGER NOT NULL,
    date BIGINT NOT NULL,
    caster VARCHAR(50) NOT NULL,
    co_casters CLOB NOT NULL,
    stream_link CLOB NOT NULL,
    vod_link CLOB NOT NULL,
    away_sets_won INTEGER NOT NULL,
    home_sets_won INTEGER NOT NULL
);

CREATE TABLE teams(
    name VARCHAR(100) NOT NULL,
    captain VARCHAR(100) NOT NULL,
    members CLOB NOT NULL,
    season VARCHAR(10) NOT NULL,
    circuit VARCHAR(10) NOT NULL,
    division VARCHAR(10) NOT NULL,
    conference VARCHAR(10) NOT NULL,
    matches_won INTEGER NOT NULL,
    matches_lost INTEGER NOT NULL,
    matches_played INTEGER NOT NULL,
    sets_won INTEGER NOT NULL,
    sets_lost INTEGER NOT NULL,
    sets_played INTEGER NOT NULL,
    playoff_seed INTEGER NOT NULL,
    info_link CLOB NOT NULL,
    PRIMARY KEY(season, circuit, division, conference, name)
);