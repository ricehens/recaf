PROGRAM LONG_LOOP;

VAR
    J: INT64;

BEGIN
    J := 0;
    WHILE J < 2147483648 DO BEGIN
        J := J + 1000;
        WRITELN(J);
        J := J + 100000000
    END
END.
