--liquibase formatted sql

--changeset wishlist:issue-per-316-1
create sequence wishlist_item_seq start with 1 increment by 50;
create table wishlist_item (
    id bigint not null,
    created_by varchar(255),
    created_on timestamp(6),
    last_modified_by varchar(255),
    last_modified_on timestamp(6),
    customer_id varchar(255) not null,
    product_id bigint not null,
    primary key (id),
    constraint uk_wishlist_customer_product unique (customer_id, product_id)
);
create index idx_wishlist_customer on wishlist_item (customer_id);
