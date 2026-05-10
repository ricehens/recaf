program Leetcode0031;

var n;
    nums: array[0..1000];

procedure PrintArray;
var i;
begin
    Write('[');
    for i := 1 to n - 1 do
        Write(nums[i], ', ');
    WriteLn(nums[n], ']')
end;

procedure NextPermutation;
var i, j, k;
begin
    k := n - 1;
    while (k > 0) and (nums[k] >= nums[k + 1]) do k := k - 1;
    if k > 0 then begin
        j := k;
        for i := n - 1 downto k do
            if nums[i + 1] > nums[k] then begin
                j := i;
                Break
            end;
        nums[k] := nums[k] + nums[j + 1];
        nums[j + 1] := nums[k] - nums[j + 1];
        nums[k] := nums[k] - nums[j + 1]
    end;
    for i := k to (n + k - 1) div 2 do
        if i <> n + k - 1 - i then begin
            nums[i + 1] := nums[i + 1] + nums[n + k - i];
            nums[n + k - i] := nums[i + 1] - nums[n + k - i];
            nums[i + 1] := nums[i + 1] - nums[n + k - i]
        end
end;

var i;
begin
    Read(n);
    if n > 1000 then Exit;
    for i := 1 to n do Read(nums[i]);
    NextPermutation;
    PrintArray
end.
