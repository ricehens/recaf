program array_index_bool_enum;

type
    Color = (Red, Green, Blue);

var
    b: array[false..true] of integer;
    e: array[Red..Blue] of integer;
    i;

begin
    b[false] := 10;
    b[true] := 20;

    e[Red] := 1;
    e[Green] := 2;
    e[Blue] := 3;

    i := 1;
    WriteLn(b[i = 1]);
    WriteLn(e[Green])
end.
