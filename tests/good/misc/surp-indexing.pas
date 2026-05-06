program SurpIndexing;

procedure printf(...); external;
procedure scanf(...); external;

var
    N, i, j;
    stdin: Array[0..0];

begin
    scanf('%d', stdin); N := stdin[0];
    for i := 0 to N - 1 do
        for j := 0 to N - 1 do
            printf('%d+4*%d=%lld'#10, j, i, int64(j) + 4 * int64(i))
end.
