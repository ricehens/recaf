program copy_global_array_to_heap;

type
  IntArray = array[0..3] of integer;
  PIntArray = ^IntArray;

var
  globalValues: IntArray;
  copiedBack: IntArray;
  heapValues: PIntArray;
  heapValues2: PIntArray;

function sumArray(a: PIntArray): integer;
begin
  sumArray := a^[0] + a^[1] + a^[2] + a^[3];
end;

begin
  New(heapValues);
  New(heapValues2);

  globalValues[0] := 10;
  globalValues[1] := 20;
  globalValues[2] := 30;
  globalValues[3] := 40;

  heapValues^ := globalValues;

  globalValues[0] := 1000;
  globalValues[1] := 2000;

  copiedBack := heapValues^;
  heapValues2^ := copiedBack;

  writeln(heapValues^[0]);    { expect 10 }
  writeln(heapValues^[1]);    { expect 20 }
  writeln(sumArray(heapValues)); { expect 100 }
  writeln(copiedBack[0]);     { expect 10 }
  writeln(copiedBack[1]);     { expect 20 }
  writeln(sumArray(heapValues2)); { expect 100 }

  Dispose(heapValues2);
  Dispose(heapValues);
end.
