program CollatzConjecture;

procedure printf(...); external;
procedure scanf(...); external;

var n;

function readInt;
var input: ^integer;
begin
    new(input);
    scanf('%d', input);
    readInt := input^;
    dispose(input)
end;

function collatz(n);
begin
    if n mod 2 = 0 then
        collatz := n div 2
    else
        collatz := 3 * n + 1
end;

begin
    n := readInt;
    while n > 1 do
    begin
        n := collatz(n);
        printf('%d'#10, n)
    end
end.
