program EofTest;

var cnt;
begin
    cnt := 0;
    while not eof do begin
        ReadLn;
        cnt := cnt + 1
    end;
    WriteLn(cnt)
end.
