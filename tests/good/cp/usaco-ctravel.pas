program ctravel;

procedure scanf(...); external;
function getchar; external;

var
    stdin: array[0..0];
    N, M, T;
    dp: array[0..1, 0..101, 0..101];
    r1, c1, r2, c2;
    dx, dy: array[0..3];
    i, j, k, r, c;
    x: array[0..101, 0..101] of boolean;

begin
    scanf('%d', stdin); N := stdin[0];
    scanf('%d', stdin); M := stdin[0];
    scanf('%d', stdin); T := stdin[0];

    for i := 0 to 101 do
        for j := 0 to 101 do begin
            x[i, j] := false;
            dp[0, i, j] := 0;
            dp[1, i, j] := 0;
        end;

    for i := 1 to N do
        for j := 1 to M do begin
            c := -1;
            (*  46 = '.'
                42 = '*'  *)
            while (c <> 46) and (c <> 42) do c := getchar;
            x[i, j] := c = 42
        end;

    scanf('%d', stdin); r1 := stdin[0];
    scanf('%d', stdin); c1 := stdin[0];
    scanf('%d', stdin); r2 := stdin[0];
    scanf('%d', stdin); c2 := stdin[0];

    dx[0] := 1; dx[1] := 0; dx[2] := -1; dx[3] := 0;
    dy[0] := 0; dy[1] := 1; dy[2] := 0; dy[3] := -1;

    dp[0, r1, c1] := 1;
    for i := 1 to T do
        for r := 1 to N do
            for c := 1 to M do begin
                dp[i mod 2, r, c] := 0;
                if not x[r, c] then
                    for k := 0 to 3 do
                        dp[i mod 2, r, c] := dp[i mod 2, r, c]
                            + dp[(i + 1) mod 2, r + dx[k], c + dy[k]];
            end;

    WriteLn(dp[T mod 2, r2, c2])
end.
