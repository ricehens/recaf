program pointer_scalar_integer_bank;

type
  PInteger = ^integer;

var
  checking: PInteger;
  savings: PInteger;
  selected: PInteger;

procedure deposit(balance: PInteger; amount: integer);
begin
  balance^ := balance^ + amount;
end;

procedure withdraw(balance: PInteger; amount: integer);
begin
  balance^ := balance^ - amount;
end;

function largerBalance(a, b: PInteger): PInteger;
begin
  if a^ >= b^ then
    largerBalance := a
  else
    largerBalance := b;
end;

begin
  New(checking);
  New(savings);

  checking^ := 500;
  savings^ := 300;

  deposit(checking, 125);
  withdraw(savings, 50);

  selected := largerBalance(checking, savings);

  writeln(checking^);  { expect 625 }
  writeln(savings^);   { expect 250 }
  writeln(selected^);  { expect 625 }

  selected^ := selected^ + 10;

  writeln(checking^);  { expect 635 }

  Dispose(savings);
  Dispose(checking);
end.
