# Interacting with the Banking app
## Create an account
curl  -XPOST -H 'Content-Type: application/json' -d '{"name":"Mr. Black"}' localhost:3000/account

## View an account
curl   localhost:3000/account/1

## Deposit amount to an account
curl  -XPOST -H 'Content-Type: application/json' -d '{"amount":100}' localhost:3000/account/1/deposit
