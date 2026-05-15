program NilAssign;

procedure printf(...); external;

type
    PInteger = ^Integer;

var
    x: PInteger;
begin
    x := nil;
    printf('%d'#10, x)
end.
