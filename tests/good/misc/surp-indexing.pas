program SurpIndexing;

var
    N, i, j;

begin
    Read(N);
    for i := 0 to N - 1 do
        for j := 0 to N - 1 do
            WriteLn(j, '+4*', i, '=', int64(j) + 4 * int64(i));
end.
