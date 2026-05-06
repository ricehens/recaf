program Container;

var
    Height: Array[0..3000];
    HeightSize: Integer;

procedure PrintArray;
var i;
begin
    Write('height: [');
    for i := 0 to 8 do Write(Height[i], ', ');
    WriteLn(Height[8], ']')
end;

function MaxArea;
var Start, Finish;
var H1, H2, MinHeight, NewArea;
begin
    Start := 0;
    Finish := HeightSize - 1;
    MaxArea := 0;

    while Start < Finish do begin
        H1 := Height[Start];
        H2 := Height[Finish];
        MinHeight := H1;
        if H2 < H1 then MinHeight := H2;
        NewArea := (Finish - Start) * MinHeight;
        if NewArea > MaxArea then MaxArea := NewArea;
        if H1 < H2 then Start := Start + 1 else Finish := Finish - 1
    end
end;

begin
    HeightSize := 9;
    Height[0] := 1;
    Height[1] := 8;
    Height[2] := 6;
    Height[3] := 2;
    Height[4] := 5;
    Height[5] := 4;
    Height[6] := 8;
    Height[7] := 3;
    Height[8] := 7;
    WriteLn(MaxArea);
end.



