program LocalType;

function Local: Integer;
type
    TArray = array[1..3] of Integer;
var
    A: TArray;
begin
    A[1] := 1;
    A[2] := 2;
    A[3] := 3;

    Local := A[1] + A[2] + A[3];
end;

begin
    WriteLn(Local);
end.
