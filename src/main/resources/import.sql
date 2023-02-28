INSERT INTO sync_entity (name, timestamp_field) VALUES ('Orders', 'deliveredOn');
INSERT INTO sync_entity (name, timestamp_field) VALUES ('Customers', 'creationDate');

insert into sync_history(timestamp, sync_entity_id) values ('2023-01-01 00:00:00-00', (select id from sync_entity where name = 'Orders'));
