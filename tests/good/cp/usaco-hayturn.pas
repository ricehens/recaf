program hayturn;

procedure scanf(...); external;

var
    x: array[0..700000] of integer;
    dp: array[0..700000] of int64;
    N, i, max, nex: integer;

begin
    Read(N);
    for i := 0 to N - 1 do Read(x[i]);

    dp[N - 1] := x[N - 1];
    max := N - 1;
    nex := 0;
    for i := N - 2 downto 0 do begin
        dp[i] := x[i] + dp[nex];
        if dp[i] >= dp[max] then begin
            nex := max;
            max := i
        end
    end;

    WriteLn(dp[max], ' ', dp[nex])
end.


