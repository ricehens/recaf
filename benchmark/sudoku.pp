program Sudoku;

(*
    INPUT:
      First, an integer n <= 10 (so that the board is n^2 x n^2).
      Then n^4 numbers representing the sudoku board in row‑major order.
    A value of 0 represents an empty cell.

    For example, for a 16×16 puzzle use n = 4.
*)

var
    Board: Array[0..99, 0..99] of Int32;  
    RowUsed,   
    ColUsed,   
    BlockUsed  
        : Array[0..99, 0..100] of Boolean; 
    n, 
    nn: Int32; 
    Solved: Boolean;

procedure SolveSudoku(r, c: Int32);
var b, d: Int32;
begin
    if r >= nn then begin
        Solved := True;
        Exit
    end;

    if c >= nn then begin
        SolveSudoku(r + 1, 0);
        Exit
    end;

    if Board[r, c] <> 0 then begin
        SolveSudoku(r, c + 1);
        Exit
    end;

    b := (r div n) * n + (c div n);
    for d := 1 to nn do 
        if not RowUsed[d, r] and not ColUsed[d, c]
                and not BlockUsed[d, b] then
        begin
            Board[r, c] := d;
            RowUsed[d, r] := True;
            ColUsed[d, c] := True;
            BlockUsed[d, b] := True;
            SolveSudoku(r, c + 1);
            if Solved then Exit;
            Board[r, c] := 0;
            RowUsed[d, r] := False;
            ColUsed[d, c] := False;
            BlockUsed[d, b] := False
        end
end;

procedure PrintBoard;
var i, j: Int32;
begin
    for i := 0 to nn - 1 do begin
        for j := 0 to nn - 2 do
            Write(Board[i, j], ' ');
        WriteLn(Board[i, nn - 1]);
    end
end;

var i, j, d: Int32;
    r, c, b: Int32;
begin
    ReadLn(n);
    nn := n * n;

    if n > 10 then Exit;
    
    for i := 0 to nn - 1 do
        for j := 0 to nn - 1 do
            Board[i, j] := 0;

    for d := 0 to nn do
        for i := 0 to nn - 1 do begin
            RowUsed[d, i] := False;
            ColUsed[d, i] := False;
            BlockUsed[d, i] := False;
        end;

    Solved := False;
    
    for i := 0 to nn - 1 do
        for j := 0 to nn - 1 do begin
            Read(d);
            Board[i, j] := d;
            if d <> 0 then begin
                r := i;
                c := j;
                b := (r div n) * n + (c div n);
                RowUsed[d, r] := True;
                ColUsed[d, c] := True;
                BlockUsed[d, b] := True;
            end;
        end;
    
    SolveSudoku(0, 0);
    PrintBoard
end.

