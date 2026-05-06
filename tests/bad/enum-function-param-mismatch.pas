program enum_function_param_mismatch;
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
  y := b0;
  id_a(y);
end.
