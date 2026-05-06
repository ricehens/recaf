program Leetcode0009;

function IsPalindrome(x: Integer): Boolean;
var y, z;
begin
    y := x;
    z := 0;
    while y > 0 do
    begin
        z := z * 10 + y mod 10;
        y := y div 10
    end;
    IsPalindrome := x = z
end;

var i;
begin
    for i := 1 to 199 do
        if IsPalindrome(i) then WriteLn(i, ' is a palindrome!')
end.

