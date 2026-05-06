program RectangleGeometry2;

type
  PRect = ^Rect;
  Rect = record
    x: integer;
    y: integer;
    width: integer;
    height: integer;
  end;

var
  a: PRect;
  b: PRect;
  larger: PRect;

function area(r: PRect): integer;
begin
  area := r^.width * r^.height;
end;

function perimeter(r: PRect): integer;
begin
  perimeter := 2 * (r^.width + r^.height);
end;

procedure moveRect(r: PRect; dx, dy: integer);
begin
  r^.x := r^.x + dx;
  r^.y := r^.y + dy;
end;

function largerRect(r1, r2: PRect): PRect;
begin
  if area(r1) >= area(r2) then
    largerRect := r1
  else
    largerRect := r2;
end;

begin
  New(a);
  New(b);

  a^.x := 0;
  a^.y := 0;
  a^.width := 10;
  a^.height := 4;

  b^.x := 5;
  b^.y := 5;
  b^.width := 6;
  b^.height := 8;

  moveRect(a, 2, 3);
  moveRect(b, -1, 2);

  larger := largerRect(a, b);

  writeln(area(a));       { expect 40 }
  writeln(area(b));       { expect 48 }
  writeln(perimeter(a));  { expect 28 }
  writeln(perimeter(b));  { expect 28 }

  writeln(a^.x);          { expect 2 }
  writeln(a^.y);          { expect 3 }
  writeln(b^.x);          { expect 4 }
  writeln(b^.y);          { expect 7 }

  writeln(larger^.width); { expect 6 }

  Dispose(b);
  Dispose(a);
end.
