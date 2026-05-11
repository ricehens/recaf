program StrLitCopy;

var a: array[1..13] of integer;

begin
    a := 'Hello, world!';
    WriteLn(a[0], a[1], a[6], a[8], a[13]);
end.
