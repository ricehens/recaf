program StrLib;

const MAX_LEN = 255;
type
    String = ^TString;
    TString = record
        arr: Array[0..MAX_LEN] of Integer;
    end;

function Len(s: String): Integer;
begin
    Len := s^.arr[0];
end;

function Equal(s1, s2: String): Boolean;
var i: Integer;
begin
    if Len(s1) <> Len(s2) then Equal := False
    else begin
        Equal := True;
        for i := 1 to Len(s1) do
            if s1^.arr[i] <> s2^.arr[i] then Equal := False;
    end;
end;

procedure ReadStr(s: String);
begin
    ReadLn(s^.arr)
end;

var s1, s2: String;
begin
    ReadStr(s1);
    ReadStr(s2);
    WriteLn(Len(s1), ' ', Len(s2), ' ', Equal(s1, s2))
end.
