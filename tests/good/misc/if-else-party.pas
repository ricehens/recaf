program IfElseParty;

var
    a: integer;
begin
    if False then WriteLn('then') else WriteLn('else');

    Write('Enter your age: ');
    ReadLn(a);

    if a > 65 then
        WriteLn('You''re old!')
    else if a > 18 then
        WriteLn('You''re an adult!')
    else if a >= 13 then
        WriteLn('You''re a teen!')
    else
        WriteLn('You''re tiny!')
end.
