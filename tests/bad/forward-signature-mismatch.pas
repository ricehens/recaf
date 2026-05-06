program forward_signature_mismatch;

procedure p(x); forward;

procedure p(x, y);
begin
    WriteLn(x + y)
end;

begin
    p(1, 2)
end.
