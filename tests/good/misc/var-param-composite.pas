program VarParamComposite;

type
    IntVec = Array[0..2] of Integer;
    Pair = record
        x: Integer;
        y: Int64;
    end;
    Ledger = record
        vals: IntVec;
        pair: Pair;
    end;

procedure Bump(var x: Integer; delta: Integer);
begin
    x := x + delta
end;

procedure AddLong(var x: Int64; delta: Int64);
begin
    x := x + delta
end;

procedure Rotate(var a: IntVec);
var
    tmp: Integer;
begin
    tmp := a[0];
    a[0] := a[1];
    a[1] := a[2];
    a[2] := tmp;
    Bump(a[0], 10)
end;

procedure TouchPair(var p: Pair);
begin
    Bump(p.x, 3);
    AddLong(p.y, Int64(p.x));
    AddLong(p.y, 7)
end;

procedure TouchLedger(var l: Ledger);
begin
    Rotate(l.vals);
    TouchPair(l.pair);
    Bump(l.vals[2], l.pair.x)
end;

procedure TouchCopy(l: Ledger);
begin
    TouchLedger(l);
    WriteLn(l.vals[0], ' ', l.vals[1], ' ', l.vals[2], ' ', l.pair.x, ' ', l.pair.y)
end;

var
    book: Ledger;
begin
    book.vals[0] := 1;
    book.vals[1] := 2;
    book.vals[2] := 3;
    book.pair.x := 4;
    book.pair.y := 20;

    TouchCopy(book);
    WriteLn(book.vals[0], ' ', book.vals[1], ' ', book.vals[2], ' ', book.pair.x, ' ', book.pair.y);

    TouchLedger(book);
    WriteLn(book.vals[0], ' ', book.vals[1], ' ', book.vals[2], ' ', book.pair.x, ' ', book.pair.y);

    Bump(book.pair.x, book.vals[1]);
    AddLong(book.pair.y, Int64(book.vals[0]));
    WriteLn(book.vals[0], ' ', book.vals[1], ' ', book.vals[2], ' ', book.pair.x, ' ', book.pair.y)
end.
