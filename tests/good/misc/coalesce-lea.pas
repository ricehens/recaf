program coalesce_lea;

procedure printf(...); external;
procedure scanf(...); external;

var
    n: integer;
    stdin: array[0..0] of integer;

begin
    scanf('%d', stdin); n := stdin[0];
    printf('4*%d+1=%d'#10, n, 4*n+1);
    printf('%d+5*%d=%d'#10, n, n, n+5*n)
end.
