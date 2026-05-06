program local_identifier_redeclare;

function f;
const
    x = 1;
var
    x;
begin
    f := x
end;

begin
end.
