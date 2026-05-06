PROGRAM LONG_ARRAY;

PROCEDURE F;
VAR PAD; I: INT64; X: ARRAY[0..14] of INT64;
BEGIN
    PAD := $BEEF;
    FOR I := 0 TO 14 DO
    BEGIN
        WRITELN('i: ', I, ', int(i): ', INTEGER(I));
        X[INTEGER(I)] := I * 7
    END
END;

BEGIN F END.
