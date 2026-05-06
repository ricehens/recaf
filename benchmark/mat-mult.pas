program MatrixMultiply;

{
    INPUT: the dimension of the array N,
    followed by N^2 entries that fill the first matrix
    (filling the first row left-to-right, then the second row, and so on),
    and then N^2 entries that fill the second matrix likewise.
}
procedure scanf(...); external;

var
    N;
    a, b, c: array[0..1023, 0..1023];

procedure MatMult;
var i, j, k;
begin
    for i := 0 to N - 1 do
        for j := 0 to N - 1 do
        begin
            c[i, j] := 0;
            for k := 0 to N - 1 do
                c[i, j] := c[i, j] + a[i, k] * b[k, j]
        end
end;

function ReadInt;
var stdin: array[0..0];
begin
    scanf('%d', stdin);
    ReadInt := stdin[0]
end;

procedure PrintMatrix;
var i, j;
begin
    for i := 0 to N - 1 do
    begin
        for j := 0 to N - 2 do
            Write(c[i, j], ' ');
        WriteLn(c[i, N - 1])
    end
end;

var i, j;
begin
    N := ReadInt;
    if N <= 1024 then
    begin
        for i := 0 to N - 1 do
            for j := 0 to N - 1 do
                a[i, j] := ReadInt;
        for i := 0 to N - 1 do
            for j := 0 to N - 1 do
                b[i, j] := ReadInt;

        MatMult;
        PrintMatrix
    end
end.
