program heap_copy_mismatch;

type
  A3 = array[0..2] of integer;
  A4 = array[0..3] of integer;
  PA3 = ^A3;

var
  p: PA3;
  a: A4;

begin
  New(p);
  p^ := a;
  Dispose(p);
end.
