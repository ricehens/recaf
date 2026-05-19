program StrassenMatMul;

{
    INPUT: the dimension N (a power of 2 and <= 256),
    followed by N^2 entries
    for the first matrix (row-major order), 
    then N^2 entries for the second matrix.
}

type
    PMatrix = ^TMatrix;
    TMatrix = record
        dim: Int32;
        arr: array[0..255, 0..255] of Int32;
    end;

function ReadMatrix(dim: Int32): PMatrix;
var i, j: Int32;
begin
    New(ReadMatrix);
    ReadMatrix^.dim := dim;
    for i := 0 to dim - 1 do
        for j := 0 to dim - 1 do
            Read(ReadMatrix^.arr[i, j])
end;

procedure PrintMatrix(M: PMatrix);
var i, j: Int32;
begin
    for i := 0 to M^.dim - 1 do
    begin
        for j := 0 to M^.dim - 2 do
            Write(M^.arr[i, j], ' ');
        WriteLn(M^.arr[i, M^.dim - 1])
    end
end;

function Add(A, B: PMatrix): PMatrix;
var i, j: Int32;
begin
    New(Add);
    Add^.dim := A^.dim;
    for i := 0 to Add^.dim - 1 do
        for j := 0 to Add^.dim - 1 do
            Add^.arr[i, j] := A^.arr[i, j] + B^.arr[i, j]
end;

function Sub(A, B: PMatrix): PMatrix;
var i, j: Int32;
begin
    New(Sub);
    Sub^.dim := A^.dim;
    for i := 0 to Sub^.dim - 1 do
        for j := 0 to Sub^.dim - 1 do
            Sub^.arr[i, j] := A^.arr[i, j] - B^.arr[i, j]
end;

function Quadrant(M: PMatrix; x, y: Int32): PMatrix;
var N, i, j: Int32;
begin
    New(Quadrant);
    N := M^.dim div 2;
    Quadrant^.dim := N;
    for i := 0 to Quadrant^.dim - 1 do
        for j := 0 to Quadrant^.dim - 1 do
            Quadrant^.arr[i, j] := M^.arr[x * N + i, y * N + j]
end;

function Merge(Q11, Q12, Q21, Q22: PMatrix): PMatrix;
var N, i, j: Int32;
begin
    New(Merge);
    N := Q11^.dim;
    Merge^.dim := N * 2;
    for i := 0 to N - 1 do
        for j := 0 to N - 1 do begin
            Merge^.arr[i, j] := Q11^.arr[i, j];
            Merge^.arr[i, j + N] := Q12^.arr[i, j];
            Merge^.arr[i + N, j] := Q21^.arr[i, j];
            Merge^.arr[i + N, j + N] := Q22^.arr[i, j]
        end
end;

function Strassen(A, B: PMatrix): PMatrix;
var A11, A12, A21, A22, B11, B12, B21, B22,
    M1, M2, M3, M4, M5, M6, M7,
    T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14,
    C11, C12, C21, C22: PMatrix;
begin
    if A^.dim = 1 then begin
        New(Strassen);
        Strassen^.dim := 1;
        Strassen^.arr[0, 0] := A^.arr[0, 0] * B^.arr[0, 0];
        Exit
    end;

    A11 := Quadrant(A, 0, 0);
    A12 := Quadrant(A, 0, 1);
    A21 := Quadrant(A, 1, 0);
    A22 := Quadrant(A, 1, 1);

    B11 := Quadrant(B, 0, 0);
    B12 := Quadrant(B, 0, 1);
    B21 := Quadrant(B, 1, 0);
    B22 := Quadrant(B, 1, 1);

    T1 := Add(A11, A22);
    T2 := Add(B11, B22);
    M1 := Strassen(T1, T2);
    Dispose(T1); Dispose(T2);

    T3 := Add(A21, A22);
    M2 := Strassen(T3, B11);
    Dispose(T3);

    T4 := Sub(B12, B22);
    M3 := Strassen(A11, T4);
    Dispose(T4);

    T5 := Sub(B21, B11);
    M4 := Strassen(A22, T5);
    Dispose(T5);

    T6 := Add(A11, A12);
    M5 := Strassen(T6, B22);
    Dispose(T6);

    T7 := Sub(A21, A11);
    T8 := Add(B11, B12);
    M6 := Strassen(T7, T8);
    Dispose(T7); Dispose(T8);

    T9 := Sub(A12, A22);
    T10 := Add(B21, B22);
    M7 := Strassen(T9, T10);
    Dispose(T9); Dispose(T10);

    Dispose(A11); Dispose(A12); Dispose(A21); Dispose(A22);
    Dispose(B11); Dispose(B12); Dispose(B21); Dispose(B22);

    T11 := Add(M1, M4);
    T12 := Sub(T11, M5);
    C11 := Add(T12, M7);
    Dispose(T11); Dispose(T12);

    T13 := Sub(M1, M2);
    T14 := Add(T13, M3);
    C22 := Add(T14, M6);
    Dispose(T13); Dispose(T14);

    C12 := Add(M3, M5);
    C21 := Add(M2, M4);

    Dispose(M1); Dispose(M2); Dispose(M3); Dispose(M4);
    Dispose(M5); Dispose(M6); Dispose(M7);

    Strassen := Merge(C11, C12, C21, C22);
    Dispose(C11); Dispose(C12); Dispose(C21); Dispose(C22)
end;

var
    N: Int32;
    A, B, C: PMatrix;
begin
    Read(N);
    A := ReadMatrix(N);
    B := ReadMatrix(N);
    C := Strassen(A, B);
    PrintMatrix(C);
    Dispose(A);
    Dispose(B);
    Dispose(C)
end.

