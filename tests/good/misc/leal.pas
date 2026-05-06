program leal;

procedure printf(...); external;
procedure scanf(...); external;

var
    n: integer;
    stdin: array[0..0] of integer;

begin
    scanf('%d', stdin); n := stdin[0];
    printf('%d'#10, n*2);
    printf('%d'#10, n*3);
    printf('%d'#10, n*4);
    printf('%d'#10, n*5);
    printf('%d'#10, n*8);
    printf('%d'#10, n*9);
    printf('%d'#10, n*(-2));
    printf('%d'#10, n*(-3));
    printf('%d'#10, n*(-4));
    printf('%d'#10, n*(-5));
    printf('%d'#10, n*(-8));
    printf('%d'#10, n*(-9))
end.

