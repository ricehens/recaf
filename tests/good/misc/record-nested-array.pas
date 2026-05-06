program record_nested_array;
type
  Pair = record
    x: integer;
    y: boolean;
  end;
  Holder = record
    p: array[0..1] of Pair;
    z: integer;
  end;
var
  h: Holder;
  hs, hs2: array[1..2] of Holder;
begin
  h.p[1].x := 7;
  h.p[1].y := true;
  h.z := 19;

  hs[2].p[0].x := 42;
  hs[2].p[0].y := false;
  hs[2].z := 5;

  hs2 := hs;

  writeln(h.p[1].x);
  writeln(h.p[1].y);
  writeln(h.z);
  writeln(hs2[2].p[0].x);
  writeln(hs2[2].p[0].y);
  writeln(hs2[2].z);
end.
