program Squeeze;

var n, i, j, k;
begin
    for i := 0 to 2 do
        for j := 0 to 2 do
            for k := i to j - 1 do begin
                WriteLn(100 * i + k);
                WriteLn(i, ' ', j, ' ', k)
            end
end.
