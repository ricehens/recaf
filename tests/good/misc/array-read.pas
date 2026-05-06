program array_read;

procedure printf(...); external;
procedure scanf(...); external;

var
    a: array[0..999, 0..999];
    stdin: array[0..0];
    M, N, i, j, k;
begin
    scanf('%d', stdin); M := stdin[0];
    scanf('%d', stdin); N := stdin[0];

    for i := 0 to M - 1 do
        for j := 0 to N - 1 do begin
            scanf('%d', stdin);
            A[i, j] := stdin[0];
        end;
    
    WriteLn(A[m - 1, n - 1]);
end.

