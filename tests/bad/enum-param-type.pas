program enum_param_type;

type
    Color = (Red, Green);
    State = (Off, On);

function id_color(x: Color): Color;
begin
    id_color := x
end;

var
    s: State;

begin
    s := On;
    id_color(s)
end.
