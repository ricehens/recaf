program Leetcode0664;

function getchar; external;

var
    n, i, j, k, t;
    c:  array[0..100];
    dp: array[0..99, 0..99];
begin
    for n := 0 to 100 do begin
        c[n] := getchar;
        if (c[n] = 10) or (c[n] = -1) then Break
    end;

    if n > 100 then Exit;

    for i := n - 1 downto 0 do begin
        dp[i, i] := 1;
        for j := i + 1 to n - 1 do begin
            if c[i] = c[j] then begin
                dp[i, j] := dp[i, j - 1];
                Continue
            end;

            dp[i, j] := 1 + dp[i + 1, j];
            for k := i + 1 to j - 1 do begin
                t := dp[i, k] + dp[k + 1, j];
                if t < dp[i, j] then dp[i, j] := t
            end
        end
    end;

    WriteLn(dp[0, n - 1])
end.
