program StrassenMatMul;

{
    INPUT: the dimension N (a power of 2 and <= 512),
    followed by N^2 entries
    for the first matrix (row-major order), 
    then N^2 entries for the second matrix.
}

type
    PMatrix = ^TMatrix;
    TMatrix = Array[0..511, 0..511] of Integer;

function ReadMatrix(dim: Integer): PMatrix;
var i, j: Integer;
begin
    New(ReadMatrix);
    for i := 0 to dim - 1 do
        for j := 0 to dim - 1 do
            Read(ReadMatrix^[i, j])
end;

procedure PrintMatrix(M: PMatrix; dim: Integer);
var i, j: Integer;
begin
    for i := 0 to dim - 1 do
    begin
        for j := 0 to dim - 2 do
            Write(M^[i, j], ' ');
        WriteLn(M^[i, dim - 1])
    end
end;

function Add(A, B: PMatrix; dim: Integer): PMatrix;
var i, j: Integer;
begin
    New(Add);
    for i := 0 to dim - 1 do
        for j := 0 to dim - 1 do
            Add^[i, j] := A^[i, j] + B^[i, j]
end;

function Sub(A, B: PMatrix; dim: Integer): PMatrix;
var i, j: Integer;
begin
    New(Sub);
    for i := 0 to dim - 1 do
        for j := 0 to dim - 1 do
            Sub^[i, j] := A^[i, j] - B^[i, j]
end;

function Quadrant(M: PMatrix; x, y: Integer; dim: Integer): PMatrix;
var N, i, j: Integer;
begin
    New(Quadrant);
    N := dim div 2;
    for i := 0 to N - 1 do
        for j := 0 to N - 1 do
            Quadrant^[i, j] := M^[x * N + i, y * N + j]
end;

function Merge(Q11, Q12, Q21, Q22: PMatrix; dim: Integer): PMatrix;
var i, j: Integer;
begin
    New(Merge);
    for i := 0 to dim - 1 do
        for j := 0 to dim - 1 do begin
            Merge^[i, j] := Q11^[i, j];
            Merge^[i, j + dim] := Q12^[i, j];
            Merge^[i + dim, j] := Q21^[i, j];
            Merge^[i + dim, j + dim] := Q22^[i, j]
        end
end;

function Strassen(A, B: PMatrix; dim: Integer): PMatrix;
var i, j, k: Integer;
    A11, A12, A21, A22, B11, B12, B21, B22,
    M1, M2, M3, M4, M5, M6, M7,
    T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14,
    C11, C12, C21, C22: PMatrix;
begin
    if dim <= 8 then begin
        New(Strassen);
        for i := 0 to dim - 1 do
            for j := 0 to dim - 1 do begin
                Strassen^[i, j] := 0;
                for k := 0 to dim - 1 do
                    Strassen^[i, j] := Strassen^[i, j] + A^[i, k] * B^[k, j]
            end;
        Exit
    end;

    A11 := Quadrant(A, 0, 0, dim);
    A12 := Quadrant(A, 0, 1, dim);
    A21 := Quadrant(A, 1, 0, dim);
    A22 := Quadrant(A, 1, 1, dim);

    B11 := Quadrant(B, 0, 0, dim);
    B12 := Quadrant(B, 0, 1, dim);
    B21 := Quadrant(B, 1, 0, dim);
    B22 := Quadrant(B, 1, 1, dim);

    T1 := Add(A11, A22, dim div 2);
    T2 := Add(B11, B22, dim div 2);
    M1 := Strassen(T1, T2, dim div 2);
    Dispose(T1); Dispose(T2);

    T3 := Add(A21, A22, dim div 2);
    M2 := Strassen(T3, B11, dim div 2);
    Dispose(T3);

    T4 := Sub(B12, B22, dim div 2);
    M3 := Strassen(A11, T4, dim div 2);
    Dispose(T4);

    T5 := Sub(B21, B11, dim div 2);
    M4 := Strassen(A22, T5, dim div 2);
    Dispose(T5);

    T6 := Add(A11, A12, dim div 2);
    M5 := Strassen(T6, B22, dim div 2);
    Dispose(T6);

    T7 := Sub(A21, A11, dim div 2);
    T8 := Add(B11, B12, dim div 2);
    M6 := Strassen(T7, T8, dim div 2);
    Dispose(T7); Dispose(T8);

    T9 := Sub(A12, A22, dim div 2);
    T10 := Add(B21, B22, dim div 2);
    M7 := Strassen(T9, T10, dim div 2);
    Dispose(T9); Dispose(T10);

    Dispose(A11); Dispose(A12); Dispose(A21); Dispose(A22);
    Dispose(B11); Dispose(B12); Dispose(B21); Dispose(B22);

    T11 := Add(M1, M4, dim div 2);
    T12 := Sub(T11, M5, dim div 2);
    C11 := Add(T12, M7, dim div 2);
    Dispose(T11); Dispose(T12);

    T13 := Sub(M1, M2, dim div 2);
    T14 := Add(T13, M3, dim div 2);
    C22 := Add(T14, M6, dim div 2);
    Dispose(T13); Dispose(T14);

    C12 := Add(M3, M5, dim div 2);
    C21 := Add(M2, M4, dim div 2);

    Dispose(M1); Dispose(M2); Dispose(M3); Dispose(M4);
    Dispose(M5); Dispose(M6); Dispose(M7);

    Strassen := Merge(C11, C12, C21, C22, dim div 2);
    Dispose(C11); Dispose(C12); Dispose(C21); Dispose(C22)
end;

var
    N: Integer;
    A, B, C: PMatrix;
begin
    Read(N);
    A := ReadMatrix(N);
    B := ReadMatrix(N);
    C := Strassen(A, B, N);
    PrintMatrix(C, N);
    Dispose(A);
    Dispose(B);
    Dispose(C)
end.

