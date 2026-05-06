program DecrementFor2;

var i;
begin
    for i := 5 downto 0 do begin
        if i mod 2 = 0 then Continue;
        Write(i, ' ')
    end;
    WriteLn
end.
