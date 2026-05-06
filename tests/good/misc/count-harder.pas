program CountHarder;

procedure printf(...); external;
procedure scanf(...); external;

var
    i, N: integer;
    stdin: array[0..0] of integer;

begin
    scanf('%d', stdin); N := stdin[0];
    for i := 0 to N - 1 do
        printf('%d'#10, i)
end.
