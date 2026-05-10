program RaymondPrime;

function IsPrime(n): Boolean;
var i;
begin
    IsPrime := True;
    if n = 1 then IsPrime := False
    else for i := 1 to n - 1 do
        if IsPrime(i) and (n mod i = 0)
            then IsPrime := False
end;

begin
    WriteLn(IsPrime(17))
end.

