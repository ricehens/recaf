program copy_heap_array_to_local;

type
  IntArray = array[0..4] of integer;
  PIntArray = ^IntArray;

procedure runHeapToLocalCopy;
var
  localValues: IntArray;
  heapValues: PIntArray;
  total: integer;
begin
  New(heapValues);

  heapValues^[0] := 11;
  heapValues^[1] := 22;
  heapValues^[2] := 33;
  heapValues^[3] := 44;
  heapValues^[4] := 55;

  localValues := heapValues^;

  heapValues^[0] := 1000;
  heapValues^[4] := 5000;

  total :=
    localValues[0] +
    localValues[1] +
    localValues[2] +
    localValues[3] +
    localValues[4];

  writeln(total);  { expect 165 }

  Dispose(heapValues);
end;

begin
  runHeapToLocalCopy;
end.
