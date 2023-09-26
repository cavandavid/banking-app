CREATE TABLE accounts(
	id      SERIAL PRIMARY KEY,
	name 	  varchar(50)  NOT NULL,
	balance DOUBLE PRECISION  NOT NULL DEFAULT 0
);
