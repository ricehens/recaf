program Leetcode0069;

function MySqrt(x);
var i;
begin
    if x >= 2147395600 then begin
        MySqrt := 46340;
        Exit
    end;

    for i := 0 to x div 2 + 1 do begin
        if i * i = x then begin
            MySqrt := i;
            Exit
        end;

        if i * i > x then begin
            MySqrt := i - 1;
            Exit
        end
    end;

    MySqrt := -1
end;

begin
    WriteLn('sqrt(', 360, ') = ', MySqrt(360));
end.
