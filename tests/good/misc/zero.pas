program Zero;

procedure printf(...); external;
procedure scanf(...); external;

var
    stdin: array[0..0] of integer;
    N, i: integer;
begin
    scanf('%d', stdin); N := stdin[0];
    for i := 0 to N - 1 do
        printf('%d'#10, i)
end.
