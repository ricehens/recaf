program Scenery;

type
  Point = record
    x: integer;
    y: integer;
  end;

  Segment = record
    endpoints: array[1..2] of Point;
    weights: array[1..3] of integer;
  end;

  Shape = record
    id: integer;
    segments: array[1..2] of Segment;
    origin: Point;
  end;

  Scene = record
    shapes: array[1..2] of Shape;
    checksum: integer;
  end;

var
  globalScene: Scene;
  globalCopy: Scene;
  globalResult: integer;

procedure testLocalNested;
var
  localScene: Scene;
  localCopy: Scene;
  localResult: integer;
begin
  localScene.shapes[1].id := 10;
  localScene.shapes[1].origin.x := 1;
  localScene.shapes[1].origin.y := 2;

  localScene.shapes[1].segments[1].endpoints[1].x := 3;
  localScene.shapes[1].segments[1].endpoints[1].y := 4;
  localScene.shapes[1].segments[1].endpoints[2].x := 5;
  localScene.shapes[1].segments[1].endpoints[2].y := 6;

  localScene.shapes[1].segments[1].weights[1] := 7;
  localScene.shapes[1].segments[1].weights[2] := 8;
  localScene.shapes[1].segments[1].weights[3] := 9;

  localScene.shapes[2].id := 20;
  localScene.shapes[2].origin.x := 11;
  localScene.shapes[2].origin.y := 12;

  localScene.shapes[2].segments[2].endpoints[1].x := 13;
  localScene.shapes[2].segments[2].endpoints[1].y := 14;
  localScene.shapes[2].segments[2].endpoints[2].x := 15;
  localScene.shapes[2].segments[2].endpoints[2].y := 16;

  localScene.shapes[2].segments[2].weights[1] := 17;
  localScene.shapes[2].segments[2].weights[2] := 18;
  localScene.shapes[2].segments[2].weights[3] := 19;

  localScene.checksum := 999;

  localCopy := localScene;

  localScene.shapes[1].id := 1000;
  localScene.shapes[1].segments[1].endpoints[1].x := 1001;
  localScene.shapes[2].segments[2].weights[3] := 1002;
  localScene.checksum := 1003;

  localResult :=
    localCopy.shapes[1].id +
    localCopy.shapes[1].origin.x +
    localCopy.shapes[1].origin.y +
    localCopy.shapes[1].segments[1].endpoints[1].x +
    localCopy.shapes[1].segments[1].endpoints[1].y +
    localCopy.shapes[1].segments[1].endpoints[2].x +
    localCopy.shapes[1].segments[1].endpoints[2].y +
    localCopy.shapes[1].segments[1].weights[1] +
    localCopy.shapes[1].segments[1].weights[2] +
    localCopy.shapes[1].segments[1].weights[3] +
    localCopy.shapes[2].id +
    localCopy.shapes[2].origin.x +
    localCopy.shapes[2].origin.y +
    localCopy.shapes[2].segments[2].endpoints[1].x +
    localCopy.shapes[2].segments[2].endpoints[1].y +
    localCopy.shapes[2].segments[2].endpoints[2].x +
    localCopy.shapes[2].segments[2].endpoints[2].y +
    localCopy.shapes[2].segments[2].weights[1] +
    localCopy.shapes[2].segments[2].weights[2] +
    localCopy.shapes[2].segments[2].weights[3] +
    localCopy.checksum;

  writeln(localResult);  
end;

begin
  globalScene.shapes[1].id := 30;
  globalScene.shapes[1].origin.x := 21;
  globalScene.shapes[1].origin.y := 22;

  globalScene.shapes[1].segments[1].endpoints[1].x := 23;
  globalScene.shapes[1].segments[1].endpoints[1].y := 24;
  globalScene.shapes[1].segments[1].endpoints[2].x := 25;
  globalScene.shapes[1].segments[1].endpoints[2].y := 26;

  globalScene.shapes[1].segments[1].weights[1] := 27;
  globalScene.shapes[1].segments[1].weights[2] := 28;
  globalScene.shapes[1].segments[1].weights[3] := 29;

  globalScene.shapes[2].id := 40;
  globalScene.shapes[2].origin.x := 31;
  globalScene.shapes[2].origin.y := 32;

  globalScene.shapes[2].segments[2].endpoints[1].x := 33;
  globalScene.shapes[2].segments[2].endpoints[1].y := 34;
  globalScene.shapes[2].segments[2].endpoints[2].x := 35;
  globalScene.shapes[2].segments[2].endpoints[2].y := 36;

  globalScene.shapes[2].segments[2].weights[1] := 37;
  globalScene.shapes[2].segments[2].weights[2] := 38;
  globalScene.shapes[2].segments[2].weights[3] := 39;

  globalScene.checksum := 888;

  globalCopy := globalScene;

  globalScene.shapes[1].id := 2000;
  globalScene.shapes[1].segments[1].endpoints[1].x := 2001;
  globalScene.shapes[2].segments[2].weights[3] := 2002;
  globalScene.checksum := 2003;

  globalResult :=
    globalCopy.shapes[1].id +
    globalCopy.shapes[1].origin.x +
    globalCopy.shapes[1].origin.y +
    globalCopy.shapes[1].segments[1].endpoints[1].x +
    globalCopy.shapes[1].segments[1].endpoints[1].y +
    globalCopy.shapes[1].segments[1].endpoints[2].x +
    globalCopy.shapes[1].segments[1].endpoints[2].y +
    globalCopy.shapes[1].segments[1].weights[1] +
    globalCopy.shapes[1].segments[1].weights[2] +
    globalCopy.shapes[1].segments[1].weights[3] +
    globalCopy.shapes[2].id +
    globalCopy.shapes[2].origin.x +
    globalCopy.shapes[2].origin.y +
    globalCopy.shapes[2].segments[2].endpoints[1].x +
    globalCopy.shapes[2].segments[2].endpoints[1].y +
    globalCopy.shapes[2].segments[2].endpoints[2].x +
    globalCopy.shapes[2].segments[2].endpoints[2].y +
    globalCopy.shapes[2].segments[2].weights[1] +
    globalCopy.shapes[2].segments[2].weights[2] +
    globalCopy.shapes[2].segments[2].weights[3] +
    globalCopy.checksum;

  writeln(globalResult);  

  testLocalNested;
end.
