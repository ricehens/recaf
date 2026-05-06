program record_default_field_type;
type
  Rec = record
    a;
    b: boolean;
    c;
  end;
var
  r: Rec;
begin
  r.a := 4;
  r.b := true;
  r.c := 9;
  writeln(r.a);
  writeln(r.b);
  writeln(r.c);
end.
