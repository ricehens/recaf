program record_copy_mismatch;
type
  ARec = record
    x: integer;
  end;
  BRec = record
    x: integer;
    y: integer;
  end;
var
  a: ARec;
  b: BRec;
begin
  a := b;
end.
