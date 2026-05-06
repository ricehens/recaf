program hayturn;

procedure scanf(...); external;

var
    x: array[0..700000] of integer;
    dp: array[0..700000] of int64;
    N, i, max, nex: integer;
    stdin: array[0..0] of integer;

begin
    scanf('%d', stdin); N := stdin[0];
    for i := 0 to N - 1 do begin
        scanf('%d', stdin); x[i] := stdin[0]
    end;

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


