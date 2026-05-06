program Polygons;

type
  TPoint = record
    x: integer;
    y: integer;
  end;

  TPolygon = record
    id: integer;
    points: array[0..2] of TPoint;
    weights: array[0..2] of integer;
    checksum: integer;
  end;

  PTPolygon = ^TPolygon;

var
  globalPoly: TPolygon;
  globalCopy: TPolygon;
  heapPoly: PTPolygon;

procedure computeChecksum(poly: PTPolygon);
begin
  poly^.checksum :=
    poly^.id +

    poly^.points[0].x +
    poly^.points[0].y +
    poly^.points[1].x +
    poly^.points[1].y +
    poly^.points[2].x +
    poly^.points[2].y +

    poly^.weights[0] +
    poly^.weights[1] +
    poly^.weights[2];
end;

procedure translate(poly: PTPolygon; dx, dy: integer);
begin
  poly^.points[0].x := poly^.points[0].x + dx;
  poly^.points[0].y := poly^.points[0].y + dy;

  poly^.points[1].x := poly^.points[1].x + dx;
  poly^.points[1].y := poly^.points[1].y + dy;

  poly^.points[2].x := poly^.points[2].x + dx;
  poly^.points[2].y := poly^.points[2].y + dy;

  computeChecksum(poly);
end;

procedure localRoundTrip;
var
  localPoly: TPolygon;
  localCopy: TPolygon;
  localHeap: PTPolygon;
begin
  New(localHeap);

  localPoly.id := 10;

  localPoly.points[0].x := 1;
  localPoly.points[0].y := 2;
  localPoly.points[1].x := 3;
  localPoly.points[1].y := 4;
  localPoly.points[2].x := 5;
  localPoly.points[2].y := 6;

  localPoly.weights[0] := 7;
  localPoly.weights[1] := 8;
  localPoly.weights[2] := 9;
  localPoly.checksum := 55;

  localHeap^ := localPoly;

  translate(localHeap, 10, 20);

  localCopy := localHeap^;

  localHeap^.id := 999;
  localHeap^.points[0].x := 999;
  localHeap^.weights[2] := 999;
  localHeap^.checksum := 999;

  writeln(localCopy.id);           { expect 10 }
  writeln(localCopy.points[0].x);  { expect 11 }
  writeln(localCopy.points[0].y);  { expect 22 }
  writeln(localCopy.points[2].x);  { expect 15 }
  writeln(localCopy.points[2].y);  { expect 26 }
  writeln(localCopy.checksum);     { expect 145 }

  Dispose(localHeap);
end;

begin
  New(heapPoly);

  globalPoly.id := 20;

  globalPoly.points[0].x := 10;
  globalPoly.points[0].y := 11;
  globalPoly.points[1].x := 12;
  globalPoly.points[1].y := 13;
  globalPoly.points[2].x := 14;
  globalPoly.points[2].y := 15;

  globalPoly.weights[0] := 16;
  globalPoly.weights[1] := 17;
  globalPoly.weights[2] := 18;
  globalPoly.checksum := 146;

  heapPoly^ := globalPoly;

  translate(heapPoly, 1, 2);

  globalCopy := heapPoly^;

  heapPoly^.id := 999;
  heapPoly^.points[0].x := 999;
  heapPoly^.weights[2] := 999;
  heapPoly^.checksum := 999;

  writeln(globalCopy.id);           { expect 20 }
  writeln(globalCopy.points[0].x);  { expect 11 }
  writeln(globalCopy.points[0].y);  { expect 13 }
  writeln(globalCopy.points[2].x);  { expect 15 }
  writeln(globalCopy.points[2].y);  { expect 17 }
  writeln(globalCopy.checksum);     { expect 155 }

  localRoundTrip;

  Dispose(heapPoly);
end.
