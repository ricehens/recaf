program pointer_record_with_array_gradebook;

type
  TStudent = record
    id: integer;
    scores: array[0..3] of integer;
    total: integer;
  end;

  PStudent = ^TStudent;

var
  student: PStudent;

procedure computeTotal(s: PStudent);
begin
  s^.total :=
    s^.scores[0] +
    s^.scores[1] +
    s^.scores[2] +
    s^.scores[3];
end;

begin
  New(student);

  student^.id := 42;
  student^.scores[0] := 80;
  student^.scores[1] := 85;
  student^.scores[2] := 90;
  student^.scores[3] := 95;

  computeTotal(student);

  writeln(student^.id);     { expect 42 }
  writeln(student^.total);  { expect 350 }

  student^.scores[2] := student^.scores[2] + 5;

  computeTotal(student);

  writeln(student^.total);  { expect 355 }

  Dispose(student);
end.
