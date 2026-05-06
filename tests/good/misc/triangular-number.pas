program TriangularNumber;

var i, sum;
begin
    sum := 0;
    for i := 1 to 20 do
        sum := sum + i;
    WriteLn(sum)
end.
