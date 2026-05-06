program array_increment;

var
    a: array[0..2] of int64;
    b: array[0..2] of int64;

begin
    b[0] := 4;
    b[1] := 8;
    b[2] := 12;
    a[0] := 1;
    a[1] := 2;
    a[2] := 3;

    a[1] := a[1] + 1;
    b[0] := b[0] + 1;
    b[2] := b[2] + a[0];

    WriteLn('a = [', a[0], ', ', a[1], ', ', a[2], ']');
    WriteLn('b = [', b[0], ', ', b[1], ', ', b[2], ']')
end.
