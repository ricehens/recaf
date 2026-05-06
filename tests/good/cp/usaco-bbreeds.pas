program bbreeds;

function getchar; external;

var
    s, dp: array[0..1000];
    N, cnt, i, j;

begin
    for N := 0 to 1000 do begin
        s[N] := getchar;
        dp[N] := 0;
        if (s[N] = 10) or (s[N] = -1) then break
    end;

    cnt := 0;
    dp[0] := 1;
    for i := 0 to N - 1 do begin
        if s[i] = 40 (* '(' *) then begin
            cnt := cnt + 1;
            for j := cnt downto 1 do
                dp[j] := (dp[j] + dp[j - 1]) mod 2012;
        end else begin
            cnt := cnt - 1;
            for j := 0 to cnt do
                dp[j] := (dp[j] + dp[j + 1]) mod 2012;
            for j := cnt + 1 to N - 1 do dp[j] := 0;
        end
    end;

    WriteLn(dp[0])
end.
