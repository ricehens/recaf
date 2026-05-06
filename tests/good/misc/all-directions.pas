program pointer_array_copy_all_directions;

type
  Data = array[0..2] of integer;
  PData = ^Data;

var
  globalA: Data;
  globalB: Data;
  heapA: PData;

procedure localCopies;
var
  localA: Data;
  localB: Data;
begin
  localA[0] := 1;
  localA[1] := 2;
  localA[2] := 3;

  heapA^ := localA;      { local stack array to heap array }

  localA[0] := 100;

  localB := heapA^;      { heap array to local stack array }

  heapA^[1] := 200;

  writeln(localB[0]);    { expect 1 }
  writeln(localB[1]);    { expect 2 }
  writeln(localB[2]);    { expect 3 }
end;

begin
  New(heapA);

  globalA[0] := 10;
  globalA[1] := 20;
  globalA[2] := 30;

  heapA^ := globalA;     { global array to heap array }

  globalA[0] := 999;

  globalB := heapA^;     { heap array to global array }

  heapA^[2] := 777;

  writeln(globalB[0]);   { expect 10 }
  writeln(globalB[1]);   { expect 20 }
  writeln(globalB[2]);   { expect 30 }

  localCopies;

  Dispose(heapA);
end.
