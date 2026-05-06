program write_enum_arg;

type
    Color = (Red, Green);

var
    c: Color;

begin
    c := Red;
    WriteLn(c)
end.
