program MisalignStruct;

type
    Enum = (A, B, C);
    Line = Array[0..31] of Integer;
    Struct = record
        x: Enum;
        y: Line;
    end;

var
    i: Integer;
    l: Line;
    s: Struct;
begin
    for i := 0 to 31 do l[i] := i;
    s.x := B;
    s.y := l;
    WriteLn(s.x = B)
end.
