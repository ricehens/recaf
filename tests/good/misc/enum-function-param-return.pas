program enum_function_param_return;
type
  Color = (red, green, blue);

function id_color(c: Color): Color;
begin
  id_color := c;
end;

function is_green(c: Color): boolean;
begin
  is_green := c = green;
end;

var
  x: Color;
begin
  x := id_color(green);
  if is_green(x) then
    writeln(1)
  else
    writeln(0);
end.
