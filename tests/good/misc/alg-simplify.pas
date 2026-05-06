PROGRAM ALG_SIMPLIFY;

FUNCTION GET(X);
BEGIN
    GET := X
END;

VAR
    X, Y;
BEGIN
    X := GET(3);
    Y := 1 * (0 + X);
    WRITELN(Y, ' ', FALSE <> (Y = X))
end.
