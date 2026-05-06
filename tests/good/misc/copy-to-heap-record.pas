program copy_global_record_to_heap;

type
  Account = record
    id: integer;
    balance: integer;
    status: integer;
  end;

  PAccount = ^Account;

var
  globalAccount: Account;
  heapAccount: PAccount;

begin
  New(heapAccount);

  globalAccount.id := 101;
  globalAccount.balance := 750;
  globalAccount.status := 1;

  heapAccount^ := globalAccount;

  globalAccount.balance := 0;
  globalAccount.status := 9;

  writeln(heapAccount^.id);       { expect 101 }
  writeln(heapAccount^.balance);  { expect 750 }
  writeln(heapAccount^.status);   { expect 1 }

  Dispose(heapAccount);
end.
