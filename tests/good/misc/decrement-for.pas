program DecrementFor;

var i, j;
begin
    j := 100;
    for i := 10 downto 1 do begin
        i := i - 1;
        j := j - 1;
        WriteLn(i)
    end;
    WriteLn(j)
end.
