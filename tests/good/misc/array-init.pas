program ArrayInit;

function get(x);
begin
    get := x
end;

var
    a: array[0..63];
    N, i, j;
begin
    N := get(7);
    for i := 0 to N - 1 do
        for j := 0 to N - 1 do
            a[i * N + j] := j;
    WriteLn(a[48])
end.
