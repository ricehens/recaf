program PassReference;

procedure Inc(var x: Integer);
begin
    x := x + 1;
end;

procedure AddTwo(var x: Integer);
begin
    Inc(x);
    Inc(x)
end;

var
    x: Integer;
begin
    x := 5;
    AddTwo(x);
    AddTwo(x);
    WriteLn(x)
end.
