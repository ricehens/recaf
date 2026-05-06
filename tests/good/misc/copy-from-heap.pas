program copy_heap_array_to_global;

type
  IntArray = array[0..3] of integer;
  PIntArray = ^IntArray;

var
  globalValues: IntArray;
  heapValues: PIntArray;

begin
  New(heapValues);

  heapValues^[0] := 3;
  heapValues^[1] := 6;
  heapValues^[2] := 9;
  heapValues^[3] := 12;

  globalValues := heapValues^;

  heapValues^[0] := 100;
  heapValues^[3] := 400;

  writeln(globalValues[0]);  { expect 3 }
  writeln(globalValues[1]);  { expect 6 }
  writeln(globalValues[2]);  { expect 9 }
  writeln(globalValues[3]);  { expect 12 }

  Dispose(heapValues);
end.
