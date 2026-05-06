program ImmediateWhile;

function run(x);
begin
    while x < 10 do x := x + 1;
    run := x
end;

begin
    WriteLn(run(5))
end.
