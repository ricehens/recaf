program RectangleGeometry;

type
  Rect = record
    x: integer;
    y: integer;
    width: integer;
    height: integer;
  end;

var
  r: Rect;
  moved: Rect;
  area: integer;
  perimeter: integer;

begin
  r.x := 10;
  r.y := 20;
  r.width := 7;
  r.height := 5;

  area := r.width * r.height;
  perimeter := 2 * (r.width + r.height);

  moved := r;
  moved.x := moved.x + 3;
  moved.y := moved.y - 4;

  writeln(area);       { expect 35 }
  writeln(perimeter);  { expect 24 }
  writeln(r.x);        { expect 10 }
  writeln(r.y);        { expect 20 }
  writeln(moved.x);    { expect 13 }
  writeln(moved.y);    { expect 16 }
end.
