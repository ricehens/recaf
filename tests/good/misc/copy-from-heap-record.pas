program copy_heap_record_to_global;

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

  heapAccount^.id := 202;
  heapAccount^.balance := 990;
  heapAccount^.status := 1;

  globalAccount := heapAccount^;

  heapAccount^.id := 999;
  heapAccount^.balance := 0;
  heapAccount^.status := 0;

  writeln(globalAccount.id);       { expect 202 }
  writeln(globalAccount.balance);  { expect 990 }
  writeln(globalAccount.status);   { expect 1 }

  Dispose(heapAccount);
end.
