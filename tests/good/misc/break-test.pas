program BreakTest;

var i;
begin
    for i := 0 to 9 do begin
        WriteLn(i);
        if i = 7 then Break
    end
end.
