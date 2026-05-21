program Quicksort;

{
    INPUT: the size of the Array N,
    followed by the elements of the Array, 
    all whitespace-separated
}

const MAX_SIZE = 10000000;
var
    a: Array[0..MAX_SIZE] of Int32;
    N: Int32;

procedure Swap(i, j: Int32);
var t: Int32;
begin
    t := a[i];
    a[i] := a[j];
    a[j] := t
end;

function Partition(lo, hi: Int32): Int32;
var pivot, i, j: Int32;
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

procedure QuicksortRange(lo, hi: Int32);
var p: Int32;
begin
    if lo < hi then
    begin
        p := Partition(lo, hi);
        QuicksortRange(lo, p);
        QuicksortRange(p + 1, hi)
    end
end;

procedure ReadArray;
var i: Int32;
begin
    for i := 1 to N do
        Read(a[i])
end;

procedure PrintArray;
var i: Int32;
begin
    Write('[');
    for i := 1 to N - 1 do
        Write(a[i], ', ');
    WriteLn(a[N], ']')
end;

begin
    ReadLn(N);
    if N <= MAX_SIZE then 
    begin
        ReadArray;
        QuicksortRange(1, N);
        PrintArray
    end
end.
