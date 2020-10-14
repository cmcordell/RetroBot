CREATE TABLE guild_settings(
    id VARCHAR(50) PRIMARY KEY,
    command_prefix CLOB NOT NULL,
    bot_nickname VARCHAR(32) NOT NULL,
    bot_highlight_color VARCHAR(7) NOT NULL
);