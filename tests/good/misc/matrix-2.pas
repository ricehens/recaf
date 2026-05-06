program Matrix2;

var
    i, j;
    a: array[0..6, 0..6];

begin
    for i := 0 to 5 do
        for j := 0 to 5 do
            a[i, j] := i + j;

    for i := 0 to 5 do
        for j := 0 to 5 do
            WriteLn(a[i, j])
end.
