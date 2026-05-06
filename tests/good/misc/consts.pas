program consts;

procedure printf(...); external;

const
    N = 101;
    TWO_N = 2 * N;
    N_PLUS_1_SQ = (N + 1) * (N + 1);

var
    a: array[1..N_PLUS_1_SQ, 1..TWO_N] of integer;
    i, j: integer;

begin
    for i := 1 to N_PLUS_1_SQ do
        for j := 1 to TWO_N do
            a[i, j] := i + j;
    WriteLn(a[9000, 150])
end.
