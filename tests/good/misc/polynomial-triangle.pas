PROGRAM POLYNOMIAL_TRIANGLE;

VAR
    CNT, I, J, K, SUM1, SUM2, SUM3;

FUNCTION GET(X);
BEGIN
    CNT := CNT + 1;
    GET := X + CNT
END;

BEGIN
    CNT := 0;
    SUM1 := 0;
    SUM2 := 0;
    SUM3 := 0;

    FOR I := 0 TO 19 DO
        FOR J := 21 - I TO 29 + I DO
            FOR K := J - I TO J + 39 - I DO 
                BEGIN
                    SUM1 := SUM1 + GET(2 + 3 * K - K * K);
                    SUM2 := SUM2 + GET(K * K - 9 * K + 14);
                    SUM3 := SUM3 + GET(SUM1) * GET(SUM2 - SUM1)
                END;

    WRITELN(SUM1);
    WRITELN(SUM2);
    WRITELN(SUM3)
END.

