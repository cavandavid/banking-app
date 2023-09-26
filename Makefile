setup-test-database:
	docker run --name bank-db -e POSTGRES_PASSWORD=mysecretpassword -d -p 5432:5432 postgres:16.0 


purge-test-database:
	docker stop bank-db
	docker rm bank-db
