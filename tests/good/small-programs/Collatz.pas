program CollatzConjecture;

var n;

function collatz(n);
begin
    if n mod 2 = 0 then
        collatz := n div 2
    else
        collatz := 3 * n + 1
end;

begin
    ReadLn(n);
    while n > 1 do
    begin
        n := collatz(n);
        WriteLn(n)
    end
end.
