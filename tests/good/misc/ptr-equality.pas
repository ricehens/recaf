program PtrEquality;

type
    PInteger = ^Integer;

var a, b, c: PInteger;

begin
    New(c);
    WriteLn(a = b);
    WriteLn(a = c);
    Dispose(c);
end.
