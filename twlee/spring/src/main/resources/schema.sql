drop table if exists users;

create table users (
    id varchar(10) primary key,
    name varchar(20) not null,
    password varchar(10) not null
);

alter table users add level integer not null default 1 ;
alter table users add login integer not null default 0 ;
alter table users add recommend integer not null default 0 ;
