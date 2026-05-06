PROGRAM ARRAY_FUNC_INDEX;

VAR
    X: INTEGER;
    A: ARRAY[0..2] OF INTEGER;

FUNCTION GET_X(): INTEGER;
BEGIN
    X := X + 1;
    GET_X := X
END;

BEGIN
    A[0] := 1;
    A[1] := 2;
    A[2] := 3;
    X := 0;
    A[GET_X] := A[GET_X] + 1;
    WRITELN(A[0], ' ', A[1], ' ', A[2], ' ', X)
END.
