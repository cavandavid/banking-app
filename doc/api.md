# Interacting with the Banking app
## Create an account
curl  -XPOST -H 'Content-Type: application/json' -d '{"name":"Mr. Black"}' localhost:3000/account

## View an account
curl   localhost:3000/account/1

## Deposit amount to an account
curl  -XPOST -H 'Content-Type: application/json' -d '{"amount":100}' localhost:3000/account/1/deposit

## Withdraw amount to an account
curl  -XPOST -H 'Content-Type: application/json' -d '{"amount":100}' localhost:3000/account/1/withdraw

## Transfer money between accounts
curl  -XPOST -H 'Content-Type: application/json' -d '{"amount":100, "account-number":2}' localhost:3000/account/1/send

## Audit of an account
curl localhost:3000/account/1/audit
