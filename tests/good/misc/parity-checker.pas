program ParityChecker;

var i;
begin
    for i := 20 to 29 do
        if i mod 2 = 0 then
            WriteLn(i, ' is even!')
        else 
            WriteLn(i, ' is odd!')
end.

