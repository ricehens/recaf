program Sudoku;

(*
    INPUT:
      First, an integer n (so that the board is n^2 x n^2).
      Then n^4 numbers representing the sudoku board in row‑major order.
    A value of 0 represents an empty cell.

    For example, for a 16×16 puzzle use n = 4.
*)

(* 
   We assume the maximum board (n^2) is at most 100.
   Hence the board has at most 100*100 = 10000 cells.
   For each row (or column or block), we need an array of size (N+1)
   to mark which digits (1..N) are used.
*)
var
    Board: Array[0..99, 0..99]; (* Board cells, stored row-major *)
    RowUsed,   (* (d, r) = whether digit d used in row r *)
    ColUsed,   (* (d, c) = whether digit d used in col c *)
    BlockUsed  (* (d, b) = whether digit d used in block b *)
        : Array[0..99, 0..100] of Boolean; 
    n, (* input: block dimension (so board is n^2 x n^2) *)
    nn; (* board dimension (n^2) *)
    Solved: Boolean;

(*
    Recursive solver.
    (r, c) is the current cell index to fill.
*)
procedure SolveSudoku(r, c);
var b, d;
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

    (* Compute block index: the board is divided into n×n blocks. *)
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

(* Print the board: N numbers per row *)
procedure PrintBoard;
var i, j;
begin
    for i := 0 to nn - 1 do begin
        for j := 0 to nn - 2 do
            Write(Board[i, j], ' ');
        WriteLn(Board[i, nn - 1]);
    end
end;

var i, j, d;
    r, c, b;
begin
    (* Read block dimension n *)
    ReadLn(n);
    nn := n * n;

    if n > 10 then Exit;
    
    (* Initialize board and constraint arrays *)
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
    
    (* Read the board: N lines with N numbers each *)
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

