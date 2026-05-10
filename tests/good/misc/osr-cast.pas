program osr_cast;

var
    N, i, j: integer;
    increase, mul: int64;

begin
    Read(N, increase, mul);

    for i := 0 to N - 1 do
        for j := 0 to N - 1 do
            WriteLn(j, '+', increase, '+', mul, '*', i, '=', j + increase + mul * i)
end.
                

