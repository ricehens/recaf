program ICantBelieveItCanSort;

procedure printf(...); external;
procedure scanf(...); external;

var
    a: array[0..1000] of integer;
    N, i: integer;

procedure PrintArray;
var
    i: integer;
begin
    printf('[');
    for i := 1 to N - 1 do
        printf('%d, ', a[i]);
    printf('%d]'#10, a[N]);
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
    scanf('%d', a);
    N := a[0];

    if N > 1000 then Exit;

    for i := 1 to N do begin
        scanf('%d', a);
        a[i] := a[0]
    end;

    Sort;
    PrintArray
end.
