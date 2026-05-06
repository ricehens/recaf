program pointer_assign_mismatch;
type
  PInt = ^integer;
  PBool = ^boolean;
var
  p: PInt;
  q: PBool;
begin
  p := q;
end.
