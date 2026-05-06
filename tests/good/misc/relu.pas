program relu;

procedure printf(...); external;
procedure scanf(...); external;

function max(a, b);
begin
    if a > b then max := a else max := b
end;

var
    stdin: array[0..0] of integer;

begin
    scanf('%d', stdin); 
    printf('%d'#10, max(0, stdin[0]))
end.
