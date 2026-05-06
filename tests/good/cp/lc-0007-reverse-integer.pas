program Leetcode0007;
const INT_MAX = $7FFFFFFF;

function reverse(x);
var
    v, u: array[0..99];
    i, j, y, ans, a, e, d, pow, p;
    positive: boolean;
begin
    positive := true;
    if x < 0 then begin
        positive := false;
        x := -x;
    end;
    
    i := 0;
    while x > 0 do begin
        v[i] := x mod 10;
        x := (x - x mod 10) div 10;
        i := i + 1
    end;

    j := 0;
    y := INT_MAX;
    while y > 0 do begin
        u[j] := y mod 10;
        y := (y - y mod 10) div 10;
        j := j + 1
    end;

    if j = i then begin
        d := 0;
        while v[d] = u[j - d - 1] do d := d + 1; 
        if (d < i) and (v[d] > u[j - d - 1]) then begin
            reverse := 0;
            Exit
        end
    end;

    ans := 0;
    for a := 0 to i - 1 do begin
        pow := 1;
        for p := 0 to i - 2 - a do pow := pow * 10;
        ans := ans + pow * v[a];
    end;

    if positive then reverse := ans else reverse := -ans
end;

begin
    WriteLn(reverse(-2147483648));
    WriteLn(reverse(-67));
    WriteLn(reverse(193863));
end.
