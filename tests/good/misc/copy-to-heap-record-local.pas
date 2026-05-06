program copy_local_record_to_heap;

type
  Player = record
    id: integer;
    health: integer;
    score: integer;
  end;

  PPlayer = ^Player;

procedure runLocalRecordCopy;
var
  localPlayer: Player;
  heapPlayer: PPlayer;
begin
  New(heapPlayer);

  localPlayer.id := 7;
  localPlayer.health := 80;
  localPlayer.score := 1200;

  heapPlayer^ := localPlayer;

  localPlayer.health := 0;
  localPlayer.score := 0;

  writeln(heapPlayer^.id);      { expect 7 }
  writeln(heapPlayer^.health);  { expect 80 }
  writeln(heapPlayer^.score);   { expect 1200 }

  Dispose(heapPlayer);
end;

begin
  runLocalRecordCopy;
end.
