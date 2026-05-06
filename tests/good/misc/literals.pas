program literals;

procedure printf(...); external;

VaR
    a, b, c: integer;
    d, e: int64;
    f: boolean;

begin
    a := -360;
    b := $7fff;
    c := 120;
    d := -3000;
    e := -$7ffff;
    f := false;
    printf('Hello, %s! %d %d %c %lld %lld %d'#10, 'world', a, b, c, d, e, f)
end.
