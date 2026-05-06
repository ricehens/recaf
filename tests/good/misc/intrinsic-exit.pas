program intrinsic_exit;

var
    i;
    s;

procedure demo_loop;
begin
    s := 0;
    for i := 0 to 5 do
    begin
        if i = 2 then Continue();
        if i = 5 then Break();
        s := s + i
    end;
    WriteLn('sum = ', s)
end;

function first_over_three;
begin
    first_over_three := 0;
    for i := 0 to 10 do
    begin
        if i <= 3 then Continue;
        first_over_three := i;
        Exit()
    end;
    first_over_three := -1
end;

begin
    demo_loop();
    WriteLn('first = ', first_over_three())
end.
