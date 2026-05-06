program array_copy;
var
  a, b: array[1..3] of integer;
begin
  a[1] := 1;
  a[2] := 2;
  a[3] := 3;
  b := a;
  a[2] := 99;
  writeln(b[1]);
  writeln(b[2]);
  writeln(b[3]);
end.
