program CopyFields;

type
    PRec = ^TRec;
    TRec = record
        arr: Array[0..4] of Integer;
    end;

var
    i, j: Integer;
    a, b: PRec;
    c, d: array[0..2] of TRec;
begin
    New(a);
    New(b);
    for i := 0 to 4 do a^.arr[i] := i;
    b^.arr := a^.arr;
    for i := 0 to 4 do WriteLn(b^.arr[i]);

    for i := 0 to 2 do begin
        c[i].arr := a^.arr;
        for j := 0 to 4 do c[i].arr[j] := c[i].arr[j] + i
    end;

    d[0] := c[1];
    d[1] := c[2];
    d[2] := c[0];

    WriteLn(d[1].arr[1])
end.
