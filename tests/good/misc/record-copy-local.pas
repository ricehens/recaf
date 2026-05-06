program record_copy;
type
  Rec = record
    x: integer;
    y: boolean;
    z: array[0..1] of integer;
  end;

procedure run;
var
  a, b: Rec;
begin
  a.x := 7;
  a.y := true;
  a.z[0] := 4;
  a.z[1] := 5;

  b := a;

  a.x := 1;
  a.y := false;
  a.z[0] := 9;
  a.z[1] := 10;

  writeln(b.x);
  writeln(b.y);
  writeln(b.z[0]);
  writeln(b.z[1]);
end;

begin run end.
