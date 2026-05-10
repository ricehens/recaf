program Leetcode1143;

var
    a, b: Array[0..1000] of Int64;
    len1, len2, i: Integer;

function LongestCommonSubsequence;
var
    dp: Array[0..1000] of Integer;
    prev, t, i, j: Integer;
begin
    prev := 0;
    for i := 0 to 1000 do dp[i] := 0;

    for i := 0 to len1 - 1 do begin
        prev := dp[0];
        dp[0] := 0;
        for j := 0 to len2 - 1 do begin
            t := prev;
            prev := dp[j + 1];
            if a[i] = b[j] then dp[j + 1] := t + 1
            else if dp[j] > dp[j + 1] then dp[j + 1] := dp[j]
        end
    end;

    LongestCommonSubsequence := dp[len2]
end;

begin
    Read(len1, len2);

    for i := 0 to len1 - 1 do Read(a[i]);
    for i := 0 to len2 - 1 do Read(b[i]);

    WriteLn(LongestCommonSubsequence)
end.
