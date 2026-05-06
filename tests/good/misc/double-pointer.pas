program PointerToPointer;

type
  PInteger = ^integer;
  PPInteger = ^PInteger;

var
  a: PInteger;
  b: PInteger;
  selected: PInteger;
  outParam: PPInteger;

function maxIntPointer(x, y: PInteger): PInteger;
begin
  if x^ >= y^ then
    maxIntPointer := x
  else
    maxIntPointer := y;
end;

procedure selectMax(x, y: PInteger; result: PPInteger);
begin
  result^ := maxIntPointer(x, y);
end;

begin
  New(a);
  New(b);
  New(outParam);

  a^ := 42;
  b^ := 99;

  selectMax(a, b, outParam);

  selected := outParam^;

  writeln(selected^);  { expect 99 }
  writeln(outParam^^); { expect 99 }

  selected^ := selected^ + 1;

  writeln(b^);         { expect 100 }
  writeln(outParam^^); { expect 100 }

  Dispose(outParam);
  Dispose(b);
  Dispose(a);
end.
