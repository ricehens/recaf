program ICantBelieveItCanSort;

var
    a: array[0..9] of int64;
    N, i: integer;

procedure PrintArray;
var
    i: integer;
begin
    write('[');
    for i := 0 to N - 2 do
        write(a[i], ', ');
    writeln(a[N - 1], ']')
end;

procedure Sort;
var
    i, j: integer;
    t: int64;
begin
    for i := 0 to N - 1 do
        for j := 0 to N - 1 do
            if a[i] < a[j] then begin
                t := a[i];
                a[i] := a[j];
                a[j] := t
            end
end;

begin
    N := 10;
    a[0] := 10000000003;
    a[1] := 10000000006;
    a[2] := 1000000000512;
    a[3] := 10000000001024;
    a[4] := -10000000001024;
    a[5] := -10000000004;
    a[6] := 10000000003;
    a[7] := 10000000003;
    a[8] := 10000000003;
    a[9] := 10000000003;

    Sort;
    PrintArray
end.
