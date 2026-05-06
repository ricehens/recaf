program VoidTailCall;

var fact;

procedure ComputeFact(n);
begin
    if n > 0 then begin
        fact := fact * n;
        ComputeFact(n - 1)
    end
end;

begin
    fact := 1;
    ComputeFact(8);
    WriteLn(fact)
end.
