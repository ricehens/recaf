program type_alias;

type
    Small = integer;
    Big = int64;
    Vec = array[1..3] of Small;

var
    v: Vec;
    x: Small;
    y: Big;

begin
    x := 2;
    y := 3;
    v[1] := x;
    v[2] := x + 1;
    v[3] := integer(y);
    WriteLn(v[1], ' ', v[2], ' ', v[3])
end.
