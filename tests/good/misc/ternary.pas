program ternary;

function min(a, b);
begin
    if a < b then
        min := a
    else
        min := b
end;

begin
    WriteLn(min(2, 3))
end.
