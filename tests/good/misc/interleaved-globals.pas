program interleaved_globals;

type
    Small = integer;

var
    x: Small;

const
    OFFSET = 4;

type
    Big = int64;

function add_offset(n: Big): Big; forward;

function add_offset(n: Big): Big;
begin
    add_offset := n + OFFSET
end;

begin
    x := 3;
    WriteLn(add_offset(x))
end.
