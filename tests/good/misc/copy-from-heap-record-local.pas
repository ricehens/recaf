program copy_heap_record_to_local;

type
  Player = record
    id: integer;
    health: integer;
    score: integer;
  end;

  PPlayer = ^Player;

procedure runHeapRecordCopy;
var
  localPlayer: Player;
  heapPlayer: PPlayer;
begin
  New(heapPlayer);

  heapPlayer^.id := 8;
  heapPlayer^.health := 65;
  heapPlayer^.score := 2400;

  localPlayer := heapPlayer^;

  heapPlayer^.id := 1000;
  heapPlayer^.health := 0;
  heapPlayer^.score := 0;

  writeln(localPlayer.id);      { expect 8 }
  writeln(localPlayer.health);  { expect 65 }
  writeln(localPlayer.score);   { expect 2400 }

  Dispose(heapPlayer);
end;

begin
  runHeapRecordCopy;
end.
