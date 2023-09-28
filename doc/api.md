# Interacting with the Banking app
### 1. Create an account
```
curl  -XPOST -H 'Content-Type: application/json' -d '{"name":"Mr. Black"}' localhost:3000/account
```
### 2. View an account
```
curl   localhost:3000/account/1
```

### 3. Deposit amount to an account
```
curl  -XPOST -H 'Content-Type: application/json' -d '{"amount":100}' localhost:3000/account/1/deposit
```

### 4. Withdraw amount to an account
```
curl  -XPOST -H 'Content-Type: application/json' -d '{"amount":100}' localhost:3000/account/1/withdraw
```

### 5. Transfer money between accounts
```
curl  -XPOST -H 'Content-Type: application/json' -d '{"amount":100, "account-number":2}' localhost:3000/account/1/send
```

### 6. Audit of an account
```
curl localhost:3000/account/1/audit
```


### 7. Check performance under load by using 2000 concurrent requests that deposit money
```
for i in {1..2000}
do
   curl  -XPOST -H 'Content-Type: application/json' -d '{"amount":100}' localhost:3000/account/1/deposit
done
```
