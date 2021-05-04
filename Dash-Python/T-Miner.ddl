CREATE TABLE system(
sys_id integer primary key,
sys_name text not null,
corpus text not null);
CREATE TABLE vec(
sys_id INTEGER,
vec_type text,
link_type text,
path text,
PRIMARY KEY (sys_id, vec_type),
FOREIGN KEY (sys_id)
REFERENCES system (sys_id));
