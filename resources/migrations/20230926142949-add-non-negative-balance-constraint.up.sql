alter table accounts
add constraint balance_nonnegative check (balance >= 0);
