program zero_arg_parens;

var
    depth;

function Foo();
begin
    Foo := depth;
    if depth < 3 then
    begin
        depth := depth + 1;
        Foo := Foo() + 1
    end
end;

procedure ping();
begin
    WriteLn('ping')
end;

begin
    depth := 0;
    ping;
    ping();
    WriteLn(Foo())
end.
