program EightQueens;

const N = 8;

var Row:          Array[0..N-1] of Boolean;
    Col:          Array[0..N-1] of Integer;
    Diag1, Diag2: Array[0..2*N-1] of Boolean;

procedure PrintBoard;
var i, j: Integer;
begin
    for i := 0 to N-1 do begin
        for j := 0 to N-1 do 
            if Col[i] = j then Write(' Q') else Write(' .');
        WriteLn
    end;
    WriteLn
end;

procedure Try(c);
var r: Integer;
begin
    if c = N then PrintBoard
    else for r := 0 to N-1 do 
        if not Row[r] and not Diag1[r+c] and not Diag2[r+7-c] then begin
            Row[r] := True;
            Diag1[r+c] := True;
            Diag2[r+7-c] := True;
            Col[c] := r;
            Try(c+1);
            Row[r] := False;
            Diag1[r+c] := False;
            Diag2[r+7-c] := False
        end
end;

begin
    Try(0)
end.

