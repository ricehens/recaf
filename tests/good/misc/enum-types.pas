program enum_types;

type
    Color = (Red, Green, Blue);
    Palette = array[0..1] of Color;

var
    c: Color;
    p: Palette;

function next_color(x: Color): Color;
begin
    if x = Red then
        next_color := Green
    else
        next_color := Blue
end;

begin
    c := Red;
    c := next_color(c);
    p[0] := c;
    p[1] := Blue;

    if p[0] = Green then
        WriteLn('ok')
    else
        WriteLn('bad')
end.
