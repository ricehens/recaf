program extern_args;

procedure printf(...); external;

var
    a, b: int64;
begin
    a := 4; b := 8;
    printf('%d, %d, %s, %lld, %d, %d, %d, %lld, %s'#10, 1, 2, '3', a, 5, 6, 7, b, '9');
end.
