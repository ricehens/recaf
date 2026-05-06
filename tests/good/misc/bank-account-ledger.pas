program BankAccountLedger;

type
  Account = record
    id: integer;
    balance: integer;
    active: boolean;
  end;

var
  checking: Account;
  snapshot: Account;
  fee: integer;

begin
  checking.id := 1001;
  checking.balance := 500;
  checking.active := true;

  fee := 25;

  checking.balance := checking.balance + 200;
  checking.balance := checking.balance - fee;

  snapshot := checking;

  checking.balance := checking.balance - 100;

  writeln(snapshot.id);       { expect 1001 }
  writeln(snapshot.balance);  { expect 675 }
  writeln(checking.balance);  { expect 575 }
  writeln(checking.active);   { expect TRUE }
end.
