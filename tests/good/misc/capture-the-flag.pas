program CaptureTheFlag;

var i, flag;
begin
    i := 0;
    flag := 0;

    if i < 1 then
        while True and (flag = 0) do
            flag := 1;

    WriteLn(flag)
end.
