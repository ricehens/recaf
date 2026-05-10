program ICantBelieveItCanSort;

var
    a: array[0..1000] of integer;
    N, i: integer;

procedure PrintArray;
var
    i: integer;
begin
    Write('[');
    for i := 1 to N - 1 do
        Write(a[i], ', ');
    WriteLn(a[N], ']')
end;

procedure Sort;
var
    i, j, t: integer;
begin
    for i := 1 to N do
        for j := 1 to N do
            if a[i] < a[j] then begin
                t := a[i];
                a[i] := a[j];
                a[j] := t
            end
end;

begin
    ReadLn(N);
    if N > 1000 then Exit;
    for i := 1 to N do Read(a[i]);
    Sort;
    PrintArray
end.
