CREATE TABLE person (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255),
    age VARCHAR(255),
    address VARCHAR(255)
);

INSERT INTO person(name, age, address) VALUES('이현주', '31', '서울');
INSERT INTO person(name, age, address) VALUES('홍길동', '30', '인천');
INSERT INTO person(name, age, address) VALUES('아무새', '25', '부산');