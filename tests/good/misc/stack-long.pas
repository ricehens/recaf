program StackLong2;

procedure f(y);
var
    x: Array[0..14] of Int64;
    i: Int64;
begin
    for i := 0 to 14 do
        x[Integer(i)] := i * 7;

    if y = 0 then Exit;

    WriteLn(x[y mod 15]);
    f(y - 1)
end;

begin f(100) end.
