program copy_record_with_array_global_heap_roundtrip;

type
  GradeBook = record
    studentId: integer;
    scores: array[0..2] of integer;
    total: integer;
  end;

  PGradeBook = ^GradeBook;

var
  globalBook: GradeBook;
  restoredBook: GradeBook;
  heapBook: PGradeBook;

procedure computeTotal(book: PGradeBook);
begin
  book^.total :=
    book^.scores[0] +
    book^.scores[1] +
    book^.scores[2];
end;

begin
  New(heapBook);

  globalBook.studentId := 77;
  globalBook.scores[0] := 70;
  globalBook.scores[1] := 80;
  globalBook.scores[2] := 90;
  globalBook.total := 240;

  heapBook^ := globalBook;

  globalBook.studentId := 0;
  globalBook.scores[0] := 0;
  globalBook.scores[1] := 0;
  globalBook.scores[2] := 0;
  globalBook.total := 0;

  heapBook^.scores[1] := heapBook^.scores[1] + 5;
  computeTotal(heapBook);

  restoredBook := heapBook^;

  heapBook^.studentId := 999;
  heapBook^.scores[0] := 999;
  heapBook^.total := 999;

  writeln(restoredBook.studentId);  { expect 77 }
  writeln(restoredBook.scores[0]);  { expect 70 }
  writeln(restoredBook.scores[1]);  { expect 85 }
  writeln(restoredBook.scores[2]);  { expect 90 }
  writeln(restoredBook.total);      { expect 245 }

  Dispose(heapBook);
end.
