program Codeforces1515E;

var
    dp: array[0..400, 0..400] of int64;
    N, i, j: integer;
    M, ans: int64;

begin
    Read(N, M);

    for i := 0 to N do
        for j := 0 to N do
            dp[i, j] := 0;

    dp[1, 1] := 1;
    for i := 2 to N do
        dp[i, 1] := (2 * dp[i - 1, 1]) mod M;

    for i := 3 to N do
        for j := 2 to (i + 1) div 2 do
            dp[i, j] := ((2 * j) * dp[i - 1, j]
                            + j * dp[i - 2, j - 1]) mod M;

    ans := 0;
    for j := 1 to (N + 1) div 2 do
        ans := (ans + dp[N, j]) mod M;

    WriteLn(ans)
end.
