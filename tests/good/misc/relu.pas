program relu;

function max(a, b);
begin
    if a > b then max := a else max := b
end;

var n;
begin
    ReadLn(n);
    WriteLn(max(0, n))
end.
