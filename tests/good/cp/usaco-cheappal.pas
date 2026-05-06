program cheappal;

procedure scanf(...); external;
function getchar; external;

function min(x, y);
begin
    if x < y then min := x else min := y
end;

var
    ia, ib, ic: array[0..0];
    N, M, i, l;
    s: array[0..2000];
    del: array[0..255];
    dp: array[0..1999, 0..2];

begin
    scanf('%d %d', ia, ib);
    N := ia[0]; M := ib[0];

    i := 0;
    while i < M do begin
        s[i] := getchar;
        if (s[i] <> 10) and (s[i] <> 13)  then i := i + 1;
    end;

    for i := 0 to N - 1 do begin
        scanf(' %c %d %d', ia, ib, ic);
        del[ia[0]] := min(ib[0], ic[0])
    end;

    for i := 0 to M - 2 do begin
        dp[i, 0] := 0;
        dp[i, 1] := 0;

        if s[i] = s[i + 1] then dp[i, 2] := 0
        else dp[i, 2] := min(del[s[i]], del[s[i + 1]])
    end;

    for l := 3 to M do
        for i := 0 to M - l do begin
            if s[i] = s[i + l - 1] then begin
                dp[i, l mod 3] := dp[i + 1, (l + 1) mod 3];
                continue
            end;
            dp[i, l mod 3] := min(del[s[i]] + dp[i + 1, (l + 2) mod 3],
                                  del[s[i + l - 1]] + dp[i, (l + 2) mod 3]);
        end;

    WriteLn(dp[0, M mod 3])
end.
