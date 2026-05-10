(*  https://usaco.org/index.php?page=viewproblem2&cpid=863 *)

program teamwork;

var
    N, K;
    s, dp: array[0..10000];

function min(a, b): integer;
begin
    if a < b then min := a else min := b
end;

function max(a, b): integer;
begin
    if a > b then max := a else max := b
end;

var i, j, mac, alt;
begin
    Read(N, K);

    dp[0] := 0;
    for i := 1 to N do begin
        Read(s[i]);
        dp[i] := 0;
        dp[0] := dp[0] + s[i]
    end;

    for i := 0 to N - 1 do begin
        alt := dp[i];
        mac := 0;
        for j := i + 1 to min(i + K, N) do begin
            if s[j] > mac then begin
                alt := alt + (s[j] - mac) * (j - i - 1);
                mac := s[j]
            end else alt := alt + mac - s[j];
            dp[j] := max(dp[j], alt)
        end;
    end;

    mac := 0;
    for i := 1 to N do mac := max(mac, dp[i]);
    WriteLn(mac)
end.

