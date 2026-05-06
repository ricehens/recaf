program FancyArray;

type
    Color = (Red, Green, Blue);

var
    next: array[Red..Blue] of Color;
    inv: array[False..True] of Boolean;

begin
    next[Red] := Green;
    next[Green] := Blue;
    next[Blue] := Red;
    inv[True] := False;
    inv[False] := True;

    WriteLn(next[Red] = Green);
    WriteLn(inv[False])
end.
