program record_no_such_field;
type
  Rec = record
    x: integer;
  end;
var
  r: Rec;
begin
  r.y := 1;
end.
