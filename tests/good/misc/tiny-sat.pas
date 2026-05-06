program TinySat;

function f(a, b, c: Boolean): Boolean;
begin
    f := not(not (a and b) or (b and c))
end;

var i, j, k;
begin
    for i := 0 to 1 do
        for j := 0 to 1 do
            for k := 0 to 1 do
                if f(i > 0, j > 0, k > 0) then
                    WriteLn(i, ' ', j, ' ', k)
end.
