program Strong;

var
    a: Array[0..60];
    i;

begin
    for i := 0 to 29 do a[2 * i + 1] := 3 * i;
    for i := 0 to 14 do WriteLn(a[4 * i + 1])
end.
