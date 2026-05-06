program favorite_numbers;

var
    small, big;
    reallySmall, reallyBig: int64;

begin
    small := -2147483648;
    big := $7FFFFFFF;
    WriteLn('My favorite numbers are ', small, ' and ', big);

    reallySmall := -9223372036854775808;
    reallyBig := $7FFFFFFFFFFFFFFF;
    WriteLn('My next favorite numbers are ', reallySmall, ' and ', reallyBig)
end.
