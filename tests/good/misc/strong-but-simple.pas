program StrongButSimple;

var
    a: Array[0..99];
    i, sum;

begin
    for i := 0 to 99 do a[i] := i;
    sum := 0;
    for i := 1 to 20 do sum := sum + a[4 * (i - 1)];
    WriteLn(sum);
end.
