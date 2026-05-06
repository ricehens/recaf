program IfElseParty;

procedure scanf(...); external;
procedure printf(...); external;

var
    a: array[0..0] of integer;
begin
    if False then printf('then'#10) else printf('else'#10);

    printf('Enter your age: ');
    scanf('%d', a);

    if a[0] > 65 then
        printf('You''re old!'#10)
    else if a[0] > 18 then
        printf('You''re an adult!'#10)
    else if a[0] >= 13 then
        printf('You''re a teen!'#10)
    else
        printf('You''re tiny!'#10);
end.
