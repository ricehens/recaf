program VeryNest;

const MAX = 10000;

var i, j;
begin
    for i := 0 to MAX do begin
        while True do begin
            for j := 0 to MAX do begin
                if j < 100 then Continue;
                Break
            end;
            if i mod 50 <> 0 then begin
                i := i + 1;
                Continue
            end;
            WriteLn('B', i);
            Break
        end;
        if i < 300 then Continue;
        WriteLn('A', i);
        Break
    end
end.
