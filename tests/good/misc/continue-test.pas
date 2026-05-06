program ContinueTest;

var i;
begin
    for i := 0 to 9 do begin
        if i = 7 then Continue;
        WriteLn(i)
    end
end.
