program write_intrinsic;

var
    a;
    b: int64;
    ok: boolean;

begin
    a := 7;
    b := 9000000000;
    ok := true;

    Write('a=', a, ', b=', b, ', ok=');
    WriteLn(ok);
    WriteLn();
    WriteLn('done')
end.
