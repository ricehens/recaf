program CompositeValueParams;

type
    IntVec = Array[0..2] of Integer;
    Pair = record
        x: Integer;
        y: Int64;
    end;

function SumVec(a: IntVec): Integer;
begin
    a[0] := a[0] + 10;
    WriteLn(a[0], ' ', a[1], ' ', a[2]);
    SumVec := a[0] + a[1] + a[2]
end;

function RecordScore(p: Pair): Int64;
begin
    p.x := p.x + 1;
    p.y := Integer(p.x) + p.y;
    WriteLn(p.x, ' ', p.y);
    RecordScore := p.y
end;

procedure PrintPairCopy(p: Pair);
begin
    p.x := p.x + 100;
    p.y := p.y + 1000;
    WriteLn(p.x, ' ', p.y)
end;

var
    a: IntVec;
    p: Pair;
    s: Integer;
    t: Int64;
begin
    a[0] := 1;
    a[1] := 2;
    a[2] := 3;
    p.x := 4;
    p.y := 5;

    s := SumVec(a);
    WriteLn(s);
    WriteLn(a[0], ' ', a[1], ' ', a[2]);

    t := RecordScore(p);
    WriteLn(t);
    PrintPairCopy(p);
    WriteLn(p.x, ' ', p.y)
end.
