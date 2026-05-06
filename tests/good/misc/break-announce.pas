program BreakAnnounce;

var i;
begin
    for i := 0 to 9 do begin
        if i = 7 then begin
            WriteLn('Taking a break at ', i);
            Break
        end;
        WriteLn(i)
    end
end.
