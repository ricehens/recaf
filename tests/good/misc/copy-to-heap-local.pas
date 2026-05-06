program copy_local_array_to_heap;

type
  IntArray = array[0..4] of integer;
  PIntArray = ^IntArray;

var
  result: integer;

procedure runLocalArrayCopy;
var
  localValues: IntArray;
  heapValues: PIntArray;
begin
  New(heapValues);

  localValues[0] := 2;
  localValues[1] := 4;
  localValues[2] := 6;
  localValues[3] := 8;
  localValues[4] := 10;

  heapValues^ := localValues;

  localValues[0] := 1000;
  localValues[4] := 5000;

  result :=
    heapValues^[0] +
    heapValues^[1] +
    heapValues^[2] +
    heapValues^[3] +
    heapValues^[4];

  writeln(result);  { expect 30 }

  Dispose(heapValues);
end;

begin
  runLocalArrayCopy;
end.
