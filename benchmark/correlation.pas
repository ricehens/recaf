program Correlation;

{
    Computes the correlation matrix for M variables with N observations each.
    INPUT:
    Line 1: space-separated M and N, both <= 1000
    Line 2..M+1: N space-separated integers between 0 and 100,
    each line giving N observations for one variable
    OUTPUT:
    correlation matrix, expressed as M lines each containing
    M space-separated floating point numbers to two decimal places.
}

uses Float64;

var
    M, N: Integer;
    A: Array[0..999, 0..999] of Int64;
    EV: Array[0..999] of Int64;
    Variance: Array[0..999] of Int64;
    Cov: Array[0..999, 0..999] of Int64;

procedure ReadMatrix;
var i, j: Integer;
begin
    for i := 0 to M - 1 do
        for j := 0 to N - 1 do
            Read(A[i, j])
end;

procedure PrintMatrix;
var i, j: Integer;
begin
    for i := 0 to M - 1 do
    begin
        for j := 0 to M - 1 do
        begin
            FPrintPrecision(FDiv(FFromInt(Cov[i, j]),
                FSqrt(FMul(FFromInt(Variance[i]), FFromInt(Variance[j])))),
                2);
            Write(' ')
        end;
        WriteLn
    end
end;

procedure ComputeCorrelation;
var i, j, k: Integer;
begin
    for i := 0 to M - 1 do
    begin
        EV[i] := 0;
        for j := 0 to N - 1 do
            EV[i] := EV[i] + A[i, j]
    end;

    for i := 0 to M - 1 do
    begin
        Variance[i] := -EV[i] * EV[i];
        for j := 0 to N - 1 do
            Variance[i] := Variance[i] + N * A[i, j] * A[i, j]
    end;

    for i := 0 to M - 1 do
        for j := 0 to M - 1 do
        begin
            Cov[i, j] := -EV[i] * EV[j];
            for k := 0 to N - 1 do
                Cov[i, j] := Cov[i, j] + N * A[i, k] * A[j, k]
        end
end;

begin
    Read(M, N);
    if (M <= 1000) and (N <= 1000) then 
    begin
        ReadMatrix;
        ComputeCorrelation;
        PrintMatrix
    end
end.
