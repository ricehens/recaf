program matrix;

var
    a: Array[0..9, 0..19] of Integer;
    i, j;

begin
    for i := 0 to 9 do
        for j := 0 to 19 do
            a[i, j] := i * j;

    WriteLn(a[6, 16])
end.
