program Timmy;

var N, size, left;
begin
    N := 100;
    size := 5;
    while size < N do
    begin
        left := 1;
        while left <= N do
        begin
            WriteLn('left = ', left, ', size = ', size);
            left := left + 2 * size
        end;
        size := size * 2
    end
end.
