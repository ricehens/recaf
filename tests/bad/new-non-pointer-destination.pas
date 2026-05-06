program new_non_pointer_destination;
type
  PInt = ^integer;
var
  p: PInt;
begin
  New(p^);
end.
