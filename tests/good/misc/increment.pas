program Increment;

procedure Inc(var x: Integer);
begin
    x := x + 1;
end;

var 
    t: Integer;
begin
    t := 4;
    Inc(t);
    WriteLn(t)
end.
