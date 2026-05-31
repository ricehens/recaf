program VarParamArray;

type
    Triple = Array[0..2] of Integer;

procedure IncAll(x: Triple);
begin
    WriteLn(x[0], ' ', x[1], ' ', x[2]);
    x[0] := x[0] + 1;
    x[1] := x[1] + 2;
    x[2] := x[2] + 3
end;

var
    x: Triple;
begin
    x[0] := 4;
    x[1] := 5;
    x[2] := 6;
    IncAll(x);
    WriteLn(x[0], ' ', x[1], ' ', x[2]);
end.
