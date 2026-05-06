program record_basic;
type
  Rec = record
    a: integer;
    b: boolean;
    c: boolean;
    d: integer;
  end;
var
  r: Rec;
begin
  r.a := 10;
  r.b := true;
  r.c := false;
  r.d := 25;

  writeln(r.a);
  writeln(r.b);
  writeln(r.c);
  writeln(r.d);
end.
