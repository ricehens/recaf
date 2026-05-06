program LocalRecordSimulation;

type
  Point = record
    x: integer;
    y: integer;
  end;

var
  finalX: integer;
  finalY: integer;

procedure simulate;
var
  p: Point;
  old: Point;
begin
  p.x := 10;
  p.y := 10;

  old := p;

  p.x := p.x + 4;
  p.y := p.y - 3;

  finalX := p.x + old.x;
  finalY := p.y + old.y;
end;

begin
  simulate;

  writeln(finalX);  { expect 24 }
  writeln(finalY);  { expect 17 }
end.
