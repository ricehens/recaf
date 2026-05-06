program Leetcode0041;

procedure scanf(...); external;

var n, i, t;
    nums: array[0..100000];
begin
    scanf('%d', nums); n := nums[0];
    if n > 100000 then Exit;

    for i := 1 to n do begin
        scanf('%d', nums); nums[i] := nums[0]
    end;

    for i := 1 to n do
        while (nums[i] > 0) and (nums[i] <= n) 
            and (i <> nums[i]) and (nums[i] <> nums[nums[i]]) do
        begin
            t := nums[nums[i]];
            nums[nums[i]] := nums[i];
            nums[i] := t
        end;

    for i := 1 to n do
        if nums[i] <> i then begin
            WriteLn(i);
            Exit
        end;

    WriteLn(n + 1)
end.




