program mirror;

procedure printf(...); external;
procedure scanf(...); external;

var
    M, i: integer;
    a, n: integer;
    stdin, stdjn: array[0..0] of integer;

begin
    scanf('%d', stdin); M := stdin[0];
    for i := 0 to M - 1 do begin
        scanf('%d', stdjn); n := stdjn[0];
        writeln(n)
    end
end.
