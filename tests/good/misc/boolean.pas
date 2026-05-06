program boolean_demo;

var
    ok: boolean;
    value;

function is_even(x): boolean;
begin
    is_even := x mod 2 = 0
end;

begin
    value := 6;
    ok := is_even(value);
    if ok and not is_even(7) then
    begin
        WriteLn('boolean works');
        WriteLn(ok)
    end
end.
