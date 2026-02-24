TRUNCATE TABLE wish, option, product, category, member CASCADE;

INSERT INTO member (id, name, email) VALUES (1, '보내는사람', 'sender@test.com');
INSERT INTO member (id, name, email) VALUES (2, '받는사람', 'receiver@test.com');
ALTER SEQUENCE member_id_seq RESTART WITH 3;

INSERT INTO category (id, name) VALUES (1, '식품');
INSERT INTO category (id, name) VALUES (2, '패션');
ALTER SEQUENCE category_id_seq RESTART WITH 3;
