program Quicksort;

{
    INPUT: the size of the Array N,
    followed by the elements of the Array, 
    all whitespace-separated
}
procedure scanf(...); external;

const MAX_SIZE = 10000000;
var
    a: Array[0..MAX_SIZE];
    N;

procedure Swap(i, j);
var t;
begin
    t := a[i];
    a[i] := a[j];
    a[j] := t
end;

function Partition(lo, hi);
var pivot, i, j;
begin
    pivot := a[lo];
    i := lo - 1;
    j := hi + 1;

    while True do
    begin
        repeat
            i := i + 1
        until a[i] >= pivot;

        repeat
            j := j - 1
        until a[j] <= pivot;

        if i >= j then
        begin
            Partition := j;
            Break
        end;

        Swap(i, j)
    end
end;

procedure QuicksortRange(lo, hi);
var p;
begin
    if lo < hi then
    begin
        p := Partition(lo, hi);
        QuicksortRange(lo, p);
        QuicksortRange(p + 1, hi)
    end
end;

function ReadInt;
var stdin: Array[0..0];
begin
    scanf('%d', stdin);
    ReadInt := stdin[0]
end;

procedure PrintArray;
var i;
begin
    Write('[');
    for i := 1 to N - 1 do
        Write(a[i], ', ');
    WriteLn(a[N], ']')
end;

var i;
begin
    N := ReadInt;
    if N > MAX_SIZE then exit;

    for i := 1 to N do
        a[i] := ReadInt;

    QuicksortRange(1, N);
    PrintArray
end.
