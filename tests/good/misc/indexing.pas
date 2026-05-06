program indexing;

procedure printf(...); external;

const
    start = -10;

var 
    a: array[start..10] of integer; 
    b: array[10..20] of integer; 

begin
    a[-10] := 1; 
    a[10] := 2;
    b[10] := 3;
    b[20] := 4;
    printf('%d %d %d %d'#10, a[-10] (* TODO -10 *), a[10], b[10], b[20])
end.
