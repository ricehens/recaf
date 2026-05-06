program quicksort;

procedure printf(...); external;
procedure scanf(...); external;

function get(x);
var get; 
begin
    N := 2;
    get := x
end;

function readInt;
var input: array[0..0] of integer;
begin
    scanf('%d', input);
    readInt := input[0]
end;

var N;
begin
    printf('%d'#10, get(N));
    N := readInt;
    printf('%d'#10, get(N))
end.
