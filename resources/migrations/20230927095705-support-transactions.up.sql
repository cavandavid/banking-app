CREATE TYPE transaction_type AS ENUM ('debit','credit','transfer');
--;;
CREATE TABLE transactions(
	id          SERIAL PRIMARY KEY,
	created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	account_id 	integer  NOT NULL,
	FOREIGN KEY (account_id) REFERENCES accounts(id),
	type transaction_type not null,
	amount   DOUBLE PRECISION  NOT null,
	recipient_id integer  null,
	FOREIGN KEY (recipient_id) REFERENCES accounts(id)
) ;
