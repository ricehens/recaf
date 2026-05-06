program Shadow;

var a, b, c;

function f(a);
var b, c;
begin
    b := a + a;
    c := b + b;
    f := c + c
end;

begin
    a := 5;
    b := a * a;
    c := b * b;
    WriteLn(f(c))
end.
