program Codeforces1486D;

procedure scanf(...); external;

var
    N, K;
    a: array[0..200000];

function min(a, b);
begin
    if a < b then min := a else min := b
end;

function check(t): boolean;
var
    pre: array[0..200000];
    i, min_pre;
begin
    pre[0] := 0;
    for i := 1 to N do begin
        if a[i] >= t then pre[i] := pre[i - 1] + 1
        else pre[i] := pre[i - 1] - 1
    end;

    min_pre := 0;
    check := false;
    for i := K to N do begin
        min_pre := min(min_pre, pre[i - K]);
        if pre[i] >= min_pre + 1 then check := true
    end
end;

var L, R, i, mid;
begin
    scanf('%d', a); N := a[0];
    scanf('%d', a); K := a[0];
    for i := 1 to N do begin
        scanf('%d', a); a[i] := a[0]
    end;

    L := 1;
    R := N;
    while L < R do begin
        mid := (L + R + 1) div 2;
        if check(mid) then L := mid else R := mid - 1
    end;
    WriteLn(L)
end.
