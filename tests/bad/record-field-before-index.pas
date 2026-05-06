program record_field_before_index;
type
  R = record
    x: integer;
  end;
var
  rs: array[0..1] of R;
begin
  rs.x := 3;
end.
