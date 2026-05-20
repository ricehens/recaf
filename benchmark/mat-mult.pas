program MatrixMultiply;

{
    INPUT: the dimension of the array N,
    followed by N^2 entries that fill the first matrix
    (filling the first row left-to-right, then the second row, and so on),
    and then N^2 entries that fill the second matrix likewise.
}

var
    N: Integer;
    a, b, c: Array[0..1023, 0..1023] of Int64;

procedure MatMult;
var i, j, k: Integer;
begin
    for i := 0 to N - 1 do
        for k := 0 to N - 1 do
            for j := 0 to N - 1 do
                c[i, j] := c[i, j] + a[i, k] * b[k, j]
end;

procedure PrintMatrix;
var i, j: Integer;
begin
    for i := 0 to N - 1 do
    begin
        for j := 0 to N - 2 do
            Write(c[i, j], ' ');
        WriteLn(c[i, N - 1])
    end
end;

var i, j: Integer;
begin
    ReadLn(N);
    if N <= 1024 then
    begin
        for i := 0 to N - 1 do
            for j := 0 to N - 1 do
                Read(a[i, j]);
        for i := 0 to N - 1 do
            for j := 0 to N - 1 do
                Read(b[i, j]);

        MatMult;
        PrintMatrix
    end
end.
