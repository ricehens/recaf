program pointer_document_undo;

type
  PDocument = ^Document;
  Document = record
    cursorLine: integer;
    cursorColumn: integer;
    length: integer;
    modified: boolean;
  end;

var
  current: PDocument;
  undo: PDocument;
  chosen: PDocument;

procedure copyDocument(dst, src: PDocument);
begin
  dst^.cursorLine := src^.cursorLine;
  dst^.cursorColumn := src^.cursorColumn;
  dst^.length := src^.length;
  dst^.modified := src^.modified;
end;

procedure editDocument(doc: PDocument; addedChars: integer);
begin
  doc^.length := doc^.length + addedChars;
  doc^.cursorLine := doc^.cursorLine + 2;
  doc^.cursorColumn := 1;
  doc^.modified := true;
end;

function chooseState(a, b: PDocument; useUndo: boolean): PDocument;
begin
  if useUndo then
    chooseState := b
  else
    chooseState := a;
end;

begin
  New(current);
  New(undo);

  current^.cursorLine := 1;
  current^.cursorColumn := 1;
  current^.length := 100;
  current^.modified := false;

  copyDocument(undo, current);

  editDocument(current, 25);

  chosen := chooseState(current, undo, true);
  copyDocument(current, chosen);

  writeln(current^.cursorLine);    { expect 1 }
  writeln(current^.cursorColumn);  { expect 1 }
  writeln(current^.length);        { expect 100 }
  writeln(current^.modified);      { expect FALSE }

  Dispose(undo);
  Dispose(current);
end.
