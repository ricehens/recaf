program cast;

var
    arr: array[0..9] of int64;
    i, j;

begin
    for i := 0 to 9 do
        arr[i] := int64(i);

    for j := 9 downto 0 do
        writeln(arr[integer(j)])
end.
