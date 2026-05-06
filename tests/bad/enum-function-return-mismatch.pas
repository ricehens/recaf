program enum_function_return_mismatch;
type
  A = (a0, a1);
  B = (b0, b1);

function id_a(x: A): A;
begin
  id_a := x;
end;

var
  y: B;
begin
  y := id_a(a0);
end.
