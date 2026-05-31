program PassReferenceRecord;

type
    Pair = record
        x: Integer;
        y: Int64;
    end;

procedure DecBothCopy(z: Pair);
begin
    z.x := z.x - 1;
    z.y := z.y - 1;
    WriteLn(z.x, ' ', z.y)
end;

procedure DecBoth(var z: Pair);
begin
    z.x := z.x - 1;
    z.y := z.y - 1;
    WriteLn(z.x, ' ', z.y)
end;

procedure DecTwice(var z: Pair);
begin
    DecBothCopy(z);
    DecBoth(z);
    DecBoth(z)
end;

var
    z: Pair;
begin
    z.x := 3;
    z.y := 4;
    DecTwice(z);
    WriteLn(z.x, ' ', z.y)
end.
