program pointer_new_dispose;
type
  PNode = ^TNode;
  TNode = record
    value: integer;
    next: PNode;
  end;
  PInt = ^integer;
var
  head, tail: PNode;
  total: PInt;
begin
  New(head);
  New(tail);
  head^.value := 10;
  head^.next := tail;
  tail^.value := 20;
  tail^.next := head;

  New(total);
  total^ := head^.value + head^.next^.value;
  writeln(total^);

  Dispose(total);
  Dispose(tail);
  Dispose(head);
end.
