PROGRAM DIVISION_LONG;

VAR
    I: INT64;

BEGIN
    FOR i := -10 to 9 DO
    BEGIn
        WRITELN(i, ' / 1 = ', i DIV 1);
        WRITELN(i, ' / 2 = ', i DIV 2);
        WRITELN(i, ' / 3 = ', i DIV 3);
        WRITELN(i, ' / 4 = ', i DIV 4);
        WRITELN(i, ' / 5 = ', i DIV 5);
        WRITELN(i, ' / 6 = ', i DIV 6);
        WRITELN(i, ' / 7 = ', i DIV 7);
        WRITELN(i, ' / 8 = ', i DIV 8);
        WRITELN(i, ' / 9 = ', i DIV 9);
    END
END.
