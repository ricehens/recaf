program SideEffects;

var a, b, c;

function Bar: Boolean;
begin
    b := b + 1;
    Bar := True;
end;

function Moo: Boolean;
begin
    c := c + 1;
    Moo := False;
end;

function Foo: Boolean;
begin
    a := a + 1;
    WriteLn('a = ', a);
    Moo;
    Foo := True;
end;

begin
    if Foo and Bar or Moo then
        WriteLn('a=', a, ', b=', b, ', c=', c)
    else WriteLn('else');

    if not Moo or Bar or not Foo then
        WriteLn('a=', a, ', b=', b, ', c=', c)
    else WriteLn('else')
end.
