program pointer_params_return;
type
  PCell = ^Cell;
  Cell = record
    value: integer;
  end;

var
  cells: array[0..1] of PCell;
  pp: ^^integer;
  chosen: ^integer;

function pick(a, b: ^integer; useA: boolean): ^integer;
begin
  if useA then
    pick := a
  else
    pick := b;
end;

begin
  New(cells[0]);
  New(cells[1]);
  New(chosen);

  cells[0]^.value := 4;
  cells[1]^.value := 9;
  chosen^ := cells[0]^.value + cells[1]^.value;

  New(pp);
  pp^ := pick(chosen, chosen, true);
  writeln(pp^^);

  Dispose(pp);
  Dispose(chosen);
  Dispose(cells[1]);
  Dispose(cells[0]);
end.
