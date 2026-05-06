program BankTransfer;

type
  PAccount = ^Account;
  Account = record
    id: integer;
    balance: integer;
  end;

var
  checking: PAccount;
  savings: PAccount;
  richer: PAccount;

procedure deposit(account: PAccount; amount: integer);
begin
  account^.balance := account^.balance + amount;
end;

procedure withdraw(account: PAccount; amount: integer);
begin
  account^.balance := account^.balance - amount;
end;

procedure transfer(fromAccount, toAccount: PAccount; amount: integer);
begin
  withdraw(fromAccount, amount);
  deposit(toAccount, amount);
end;

function maxAccount(a, b: PAccount): PAccount;
begin
  if a^.balance >= b^.balance then
    maxAccount := a
  else
    maxAccount := b;
end;

begin
  New(checking);
  New(savings);

  checking^.id := 1001;
  checking^.balance := 500;

  savings^.id := 2001;
  savings^.balance := 300;

  transfer(checking, savings, 125);

  richer := maxAccount(checking, savings);

  writeln(checking^.balance);  { expect 375 }
  writeln(savings^.balance);   { expect 425 }
  writeln(richer^.id);         { expect 2001 }

  Dispose(savings);
  Dispose(checking);
end.
