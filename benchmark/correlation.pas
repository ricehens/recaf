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
function fl_I2f(x); external;
function fl_Mul(x, y); external;
function fl_Div(x, y); external;
function fl_Sqrt(x); external;
procedure fl_PrintPrecision(x, digits); external;

var
    A: array[0..999, 0..999] of integer;
    EV: array[0..999] of integer;
    Variance: array[0..999] of integer;
    Cov: array[0..999, 0..999] of integer;

var M, N, i, j, k;
begin
    Read(M, N);

    if (M > 1000) or (N > 1000) then
        WriteLn('M and N must be <= 1000')
    else
    begin
        for i := 0 to M - 1 do
            for j := 0 to N - 1 do
                Read(A[i, j]);

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
            end;

        for i := 0 to M - 1 do
        begin
            for j := 0 to M - 1 do
            begin
                fl_PrintPrecision(
                    fl_Div(
                        fl_I2f(Cov[i, j]),
                        fl_Sqrt(fl_Mul(fl_I2f(Variance[i]), fl_I2f(Variance[j])))
                    ),
                    2
                );
                Write(' ')
            end;
            WriteLn
        end
    end;
end.
