program RecordGlobal;

type
  Point = record
    x: integer;
    y: integer;
  end;

procedure run;
var
  ok: integer;
  p: Point;
begin
  ok := 0;

  p.x := 10;
  p.y := 20;

  if p.x = 10 then ok := ok + 1;
  if p.y = 20 then ok := ok + 1;

  writeln(ok);  
end;

begin run end.
