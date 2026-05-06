PROGRAM OPTIMISTIC_SSCP;

PROCEDURE RUN;
VAR X, I, J;
BEGIN
    X := 17;
    I := 0;
    J := 0;
    WHILE I < X DO BEGIN
        I := I + 1;
        X := X + J
    END;
    WRITELN(X, ' ', I, ' ', J)
END;

BEGIN RUN END.
