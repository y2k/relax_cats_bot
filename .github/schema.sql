DROP TABLE IF EXISTS last_request_time;
CREATE TABLE IF NOT EXISTS last_request_time (user_id INTEGER PRIMARY KEY, time INTEGER);
