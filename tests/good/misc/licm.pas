PROGRAM LICM;

FUNCTION GET(X);
BEGIN
    GET := X
END;

VAR N, I;
BEGIN
    N := GET(3);
    FOR I := 0 TO 4 DO 
    BEGIN
        WRITELN(2 * N);
        WRITELN(GET(2 * N));
    END
END.
