program Increment;

procedure Inc(var x: Integer);
begin
    x := x + 1;
end;

var 
    t: Integer;
begin
    t := 6;
    Inc(t);
    WriteLn(t)
end.
