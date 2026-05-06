PROGRAM MODULO_LONG;

VAR
    I: INT64;

BEGIN
    FOR i := -10 to 9 DO
    BEGIn
        WRITELN(i, ' mod 1 = ', i MOD 1);
        WRITELN(i, ' mod 2 = ', i MOD 2);
        WRITELN(i, ' mod 3 = ', i MOD 3);
        WRITELN(i, ' mod 4 = ', i MOD 4);
        WRITELN(i, ' mod 5 = ', i MOD 5);
        WRITELN(i, ' mod 6 = ', i MOD 6);
        WRITELN(i, ' mod 7 = ', i MOD 7);
        WRITELN(i, ' mod 8 = ', i MOD 8);
        WRITELN(i, ' mod 9 = ', i MOD 9);
    END
END.
