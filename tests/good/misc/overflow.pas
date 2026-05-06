program Overflow;

var i;
begin
    i := 1;
    while True do begin
        if i > 10 then Exit;
        WriteLn(i);
        i := i + 1
    end
end.
