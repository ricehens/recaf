PROGRAM CAST_LONG;

VAR
    A: INTEGER;
    B: INT64;
BEGIN
    A := -17;
    B := INT64(A);
    WRITELN(B);

    A := -1;
    B := INT64(A);
    WRITELN(B);
END.
