program StrassenMatMul;

{
    INPUT: the dimension N (a power of 2 and <= 1024),
    followed by N^2 entries
    for the first matrix (row-major order), 
    then N^2 entries for the second matrix.
}
procedure scanf(...); external;
procedure printf(...); external;

type
    IDX = (A, B, C, T1, T2, M1, M2, M3, M4, M5, M6, M7);
var
    M: array[A..M7, 0..2047, 0..1023];
    N, i, j;

function readInt;
var input: array[0..0];
begin
    scanf('%d', input);
    readInt := input[0]
end;

(* 
  Print the matrix C.
  The matrix has dimension N×N and is stored with row stride 1024.
*)
procedure printMatrix(n; C: IDX; C_x, C_y);
var i, j;
begin
    for i := 0 to n - 1 do
    begin
        for j := 0 to n - 2 do
            printf('%d ', M[C, C_x + i, C_y + j]);
        printf('%d'#10, M[C, C_x + i, C_y + n - 1])
    end
end;


(*
  Copies the nxn submatrix of A starting at index (A_x, A_y)
  to the result in the nxn submatrix of C starting at (C_x, C_y).
*)
procedure mov(
    n; A, C: IDX; A_x, A_y, C_x, C_y
);
var i, j;
begin
    for i := 0 to n - 1 do
        for j := 0 to n - 1 do
            M[C, C_x + i, C_y + j] := M[A, A_x + i, A_y + j]
end;

(*
  Adds the nxn submatrix of A starting at index (A_x, A_y)
  to the nxn submatrix of B starting at index (B_x, B_y),
  and stores the result in the nxn submatrix of C starting at (C_x, C_y).
*)
procedure add(
    n; A, B, C: IDX; A_x, A_y, B_x, B_y, C_x, C_y
);
var i, j;
begin
    for i := 0 to n - 1 do
        for j := 0 to n - 1 do
            M[C, C_x + i, C_y + j] :=
                M[A, A_x + i, A_y + j] + M[B, B_x + i, B_y + j]
end;

(*
  Subtracts the nxn submatrix of B starting at index B_base
  to the nxn submatrix of A starting at index A_base,
  and stores the result in the nxn submatrix of C starting at C_base.
*)
procedure sub(
    n; A, B, C: IDX; A_x, A_y, B_x, B_y, C_x, C_y
);
var i, j;
begin
    for i := 0 to n - 1 do
        for j := 0 to n - 1 do
            M[C, C_x + i, C_y + j] :=
                M[A, A_x + i, A_y + j] - M[B, B_x + i, B_y + j]
end;

(*
  Multiplies the nxn submatrix of the global array a starting at index (n, 0),
  with the nxn submatrix of the global array b starting at index (n, 0),
  and stores the result in the nxn submatrix of the global array c starting at index (n, 0),
  using the recursive Strassen multiplication algorithm.
*)
procedure strassen(n);
var d1x, d1y, d2x, d2y, dtx, dty;
begin
    if n = 1 then
        M[C, 1, 0] := M[A, 1, 0] * M[B, 1, 0]
    else
    begin
        (* Base indices for four quadrants *)
        d1x := n;
        d1y := 0;
        d2y := n div 2;
        d2x := n + (n div 2);

        (* Base index for temp matrices
           (to avoid overlap within recursive stack) *)
        dtx := n div 2;
        dty := 0;

        (* T1 = A11 + A22 *)
        add(n div 2, A, A, T1, d1x, d1y, d2x, d2y, dtx, dty);

        (* T2 = B11 + B22 *)
        add(n div 2, B, B, T2, d1x, d1y, d2x, d2y, dtx, dty);

        (* M1 = T1 * T2 *)
        mov(n div 2, T1, A, dtx, dty, dtx, dty);
        mov(n div 2, T2, B, dtx, dty, dtx, dty);
        strassen(n div 2);
        mov(n div 2, C, M1, dtx, dty, dtx, dty);

        (* T3 = A21 + A22 *)
        add(n div 2, A, A, T1, d2x, d1y, d2x, d2y, dtx, dty);

        (* M2 = T3 * B11 *)
        mov(n div 2, T1, A, dtx, dty, dtx, dty);
        mov(n div 2, B, B, d1x, d1y, dtx, dty);
        strassen(n div 2);
        mov(n div 2, C, M2, dtx, dty, dtx, dty);

        (* T4 = B12 - B22 *)
        sub(n div 2, B, B, T1, d1x, d2y, d2x, d2y, dtx, dty);

        (* M3 = A11 * T4 *)
        mov(n div 2, A, A, d1x, d1y, dtx, dty);
        mov(n div 2, T1, B, dtx, dty, dtx, dty);
        strassen(n div 2);
        mov(n div 2, C, M3, dtx, dty, dtx, dty);

        (* T5 = B21 - B11 *)
        sub(n div 2, B, B, T1, d2x, d1y, d1x, d1y, dtx, dty);

        (* M4 = A22 * T5 *)
        mov(n div 2, A, A, d2x, d2y, dtx, dty);
        mov(n div 2, T1, B, dtx, dty, dtx, dty);
        strassen(n div 2);
        mov(n div 2, C, M4, dtx, dty, dtx, dty);

        (* T6 = A11 + A12 *)
        add(n div 2, A, A, T1, d1x, d1y, d1x, d2y, dtx, dty);

        (* M5 = T6 * B22 *)
        mov(n div 2, T1, A, dtx, dty, dtx, dty);
        mov(n div 2, B, B, d2x, d2y, dtx, dty);
        strassen(n div 2);
        mov(n div 2, C, M5, dtx, dty, dtx, dty);

        (* T7 = A21 - A11 *)
        sub(n div 2, A, A, T1, d2x, d1y, d1x, d1y, dtx, dty);

        (* T8 = B11 + B12 *)
        add(n div 2, B, B, T2, d1x, d1y, d1x, d2y, dtx, dty);

        (* M6 = T7 * T8 *)
        mov(n div 2, T1, A, dtx, dty, dtx, dty);
        mov(n div 2, T2, B, dtx, dty, dtx, dty);
        strassen(n div 2);
        mov(n div 2, C, M6, dtx, dty, dtx, dty);

        (* T9 = A12 - A22 *)
        sub(n div 2, A, A, T1, d1x, d2y, d2x, d2y, dtx, dty);

        (* T10 = B21 + B22 *)
        add(n div 2, B, B, T2, d2x, d1y, d2x, d2y, dtx, dty);

        (* M7 = T9 * T10 *)
        mov(n div 2, T1, A, dtx, dty, dtx, dty);
        mov(n div 2, T2, B, dtx, dty, dtx, dty);
        strassen(n div 2);
        mov(n div 2, C, M7, dtx, dty, dtx, dty);

        (*
            C11 = M1 + M4 - M5 + M7
            C12 = M3 + M5
            C21 = M2 + M4
            C22 = M1 - M2 + M3 + M6
        *)
        add(n div 2, M1, M4, T1, dtx, dty, dtx, dty, dtx, dty);
        sub(n div 2, M5, M7, T2, dtx, dty, dtx, dty, dtx, dty);
        sub(n div 2, T1, T2, C, dtx, dty, dtx, dty, d1x, d1y);
        add(n div 2, M3, M5, C, dtx, dty, dtx, dty, d1x, d2y);
        add(n div 2, M2, M4, C, dtx, dty, dtx, dty, d2x, d1y);
        sub(n div 2, M1, M2, T1, dtx, dty, dtx, dty, dtx, dty);
        add(n div 2, M3, M6, T2, dtx, dty, dtx, dty, dtx, dty);
        add(n div 2, T1, T2, C, dtx, dty, dtx, dty, d2x, d2y)
    end
end;

begin
    N := readInt;
    if N <= 1024 then
    begin
        for i := 0 to N - 1 do
            for j := 0 to N - 1 do
                M[A, N + i, j] := readInt;

        for i := 0 to N - 1 do
            for j := 0 to N - 1 do
                M[B, N + i, j] := readInt;

        strassen(N);
        printMatrix(N, C, N, 0)
    end
end.
