program array_bound_expr;

const
    N = 4;
    SHIFT = 1;

var
    a: array[0..(N * N - 1)] of integer;
    b: array[(-N)..(N + SHIFT)] of integer;

begin
    a[N * N - 1] := 42;
    b[-N] := 7;
    b[N + SHIFT] := 9;
    WriteLn(a[15]);
    WriteLn(b[-4]);
    WriteLn(b[5])
end.
