create table if not exists permissions (
  id uuid not null,
  created_at timestamp(6) with time zone not null,
  created_by varchar(255) not null,
  deleted_at timestamp(6) with time zone,
  deleted_by varchar(255),
  updated_at timestamp(6) with time zone not null,
  updated_by varchar(255) not null,
  resource_path_id uuid not null,
  role_id uuid not null,
  primary key (id)
);

create table revoked_tokens (
  id uuid not null,
  created_at timestamp(6) with time zone not null,
  created_by varchar(255) not null,
  deleted_at timestamp(6) with time zone,
  deleted_by varchar(255),
  updated_at timestamp(6) with time zone not null,
  updated_by varchar(255) not null,
  expired_at timestamp(6) with time zone not null,
  jwt varchar(1000) not null unique,
  primary key (id)
);

create table sysconfig_types (
  id uuid not null,
  created_at timestamp(6) with time zone not null,
  created_by varchar(255) not null,
  deleted_at timestamp(6) with time zone,
  deleted_by varchar(255),
  updated_at timestamp(6) with time zone not null,
  updated_by varchar(255) not null,
  description varchar(255) not null,
  name varchar(255) not null,
  primary key (id)
);

create table sysconfigs (
  id uuid not null,
  created_at timestamp(6) with time zone not null,
  created_by varchar(255) not null,
  deleted_at timestamp(6) with time zone,
  deleted_by varchar(255),
  updated_at timestamp(6) with time zone not null,
  updated_by varchar(255) not null,
  key varchar(255) not null,
  value varchar(255) not null,
  sysconfig_type_id uuid not null,
  primary key (id)
);

create table users (
  id uuid not null,
  created_at timestamp(6) with time zone not null,
  created_by varchar(255) not null,
  deleted_at timestamp(6) with time zone,
  deleted_by varchar(255),
  updated_at timestamp(6) with time zone not null,
  updated_by varchar(255) not null,
  email varchar(255) not null unique,
  full_name varchar(255) not null,
  password_hash varchar(255),
  role_id uuid not null,
  primary key (id)
);

create table products (
  id uuid not null,
  created_at timestamp(6) with time zone not null,
  created_by varchar(255) not null,
  deleted_at timestamp(6) with time zone,
  deleted_by varchar(255),
  updated_at timestamp(6) with time zone not null,
  updated_by varchar(255) not null,
  description varchar(255),
  name varchar(255) not null,
  price numeric(19,2) not null,
  sku varchar(255) not null unique,
  primary key (id)
);

alter table if exists permissions
add constraint FKgbbliiluax6e4bwk5qxohc2fy foreign key (resource_path_id) references sysconfigs;

alter table if exists permissions
add constraint FKdxol2q5vlrf4ybqx7r82imj4r foreign key (role_id) references sysconfigs;

alter table if exists sysconfigs
add constraint FK58x4i5xuxqpgd5fqulv9yfej3 foreign key (sysconfig_type_id) references sysconfig_types;

alter table if exists users
add constraint FK7hhxskfp2mwm6k9fwef36n0hx foreign key (role_id) references sysconfigs;
