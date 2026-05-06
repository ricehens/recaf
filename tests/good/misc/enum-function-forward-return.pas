program enum_function_forward_return;
type
  Mode = (off, on);

function id_mode(m: Mode): Mode; forward;

function id_mode(m: Mode): Mode;
begin
  id_mode := m;
end;

var
  x: Mode;
begin
  x := id_mode(on);
  if x = on then
    writeln(7)
  else
    writeln(0);
end.
