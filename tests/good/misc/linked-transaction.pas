program LinkedListTransaction;

type
  PTransaction = ^Transaction;
  Transaction = record
    id: integer;
    amount: integer;
    fee: integer;
    next: PTransaction;
  end;

var
  first: PTransaction;
  second: PTransaction;
  third: PTransaction;
  largest: PTransaction;
  total: integer;

procedure initTransaction(t: PTransaction; id, amount, fee: integer);
begin
  t^.id := id;
  t^.amount := amount;
  t^.fee := fee;
end;

function totalDebit(t: PTransaction): integer;
begin
  totalDebit := t^.amount + t^.fee;
end;

function largerTransaction(a, b: PTransaction): PTransaction;
begin
  if totalDebit(a) >= totalDebit(b) then
    largerTransaction := a
  else
    largerTransaction := b;
end;

begin
  New(first);
  New(second);
  New(third);

  initTransaction(first, 1, 60, 2);
  initTransaction(second, 2, 100, 5);
  initTransaction(third, 3, 25, 1);

  first^.next := second;
  second^.next := third;

  total :=
    totalDebit(first) +
    totalDebit(first^.next) +
    totalDebit(first^.next^.next);

  largest := largerTransaction(first, first^.next);
  largest := largerTransaction(largest, first^.next^.next);

  writeln(total);        { expect 193 }
  writeln(largest^.id);  { expect 2 }

  Dispose(third);
  Dispose(second);
  Dispose(first);
end.
