program osr_cast;

procedure printf(...); external;
procedure scanf(...); external;

var
    N, i, j: integer;
    stdin: array[0..0] of integer;
    increase, mul: int64;
    stdjn: array[0..0] of int64;

begin
    scanf('%d', stdin); N := stdin[0];
    scanf('%lld', stdjn); increase := stdjn[0];
    scanf('%lld', stdjn); mul := stdjn[0];

    for i := 0 to N - 1 do
        for j := 0 to N - 1 do
            printf('%d+%lld+%lld*%d=%lld'#10, j, increase,
                mul, i, j + increase + mul * i)
end.
                

