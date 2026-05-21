program CowBASIC;

{ https://usaco.org/index.php?page=viewproblem2&cpid=746 }

const
    p = 1000000007;
    MAX = 100;

type
    PTrie = ^TTrie;
    TTrie = record
        idx: Int32;
        next: array['a'..'z'] of PTrie;
    end;

var
    cnt: Int32;
    env: PTrie;

type
    PVec = ^TVec;
    TVec = Array[0..MAX] of Int64;
    PMat = ^TMat;
    TMat = Array[0..MAX] of TVec;

var
    buf: AnsiString;
    idx: Int32;

function ParseLit: Int32;
var val: Int32;
begin
    val := 0;
    while (idx <= Length(buf)) and (buf[idx] >= '0') and (buf[idx] <= '9') do begin
        val := (val * 10) + Pos(buf[idx], '0123456789') - 1;
        idx := idx + 1
    end;
    ParseLit := val
end;

function ParseVar: Int32;
var now: PTrie;
begin
    now := env;
    while (idx <= Length(buf)) and (buf[idx] >= 'a') and (buf[idx] <= 'z') do begin
        if now^.next[buf[idx]] = Nil then begin
            New(now^.next[buf[idx]]);
            FillChar(now^.next[buf[idx]]^, SizeOf(TTrie), 0);
            now^.next[buf[idx]]^.idx := -1;
        end;
        now := now^.next[buf[idx]];
        idx := idx + 1
    end;

    if now^.idx = -1 then begin
        now^.idx := cnt;
        cnt := cnt + 1
    end;
    ParseVar := now^.idx
end;

function ParseExpr: PVec;
var
    i: Int32;
    left, right: PVec;
begin
    New(ParseExpr);
    for i := 0 to 100 do ParseExpr^[i] := 0;
    if (buf[idx] >= '0') and (buf[idx] <= '9') then
        ParseExpr^[0] := ParseLit
    else if (buf[idx] >= 'a') and (buf[idx] <= 'z') then
        ParseExpr^[ParseVar] := 1
    else if buf[idx] = '(' then begin
        idx := idx + 2;
        left := ParseExpr();
        idx := idx + 7;
        right := ParseExpr();
        idx := idx + 2;
        for i := 0 to 100 do ParseExpr^[i] := (left^[i] + right^[i]) mod p;
        Dispose(left);
        Dispose(right)
    end else WriteLn('fatal error')
end;

function ElemVec(i: Int32): PVec;
var j: Int32;
begin
    New(ElemVec);
    for j := 0 to 100 do ElemVec^[j] := 0;
    ElemVec^[i] := 1
end;

function MatMul(a, b: PMat): PMat;
var i, j, k: Int32;
begin
    New(MatMul);
    for i := 0 to 100 do
        for j := 0 to 100 do
            MatMul^[i][j] := 0;
    for i := 0 to 100 do
        for k := 0 to 100 do
            for j := 0 to 100 do
                MatMul^[i][j] := (MatMul^[i][j] + (a^[i][k] * b^[k][j]) mod p) mod p;
end;

function MatMulDispose(a, b: PMat): PMat;
begin
    MatMulDispose := MatMul(a, b);
    Dispose(a);
    Dispose(b)
end;

function MatPow(m: PMat; e: Int32): PMat;
begin
    if e = 1 then MatPow := m
    else if e mod 2 = 0 then MatPow := MatPow(MatMul(m, m), e div 2)
    else MatPow := MatMul(m, MatPow(m, e - 1))
end;

function MatPowDispose(m: PMat; e: Int32): PMat;
begin
    MatPowDispose := MatPow(m, e);
    Dispose(m)
end;

function Assign(dest: Int32; src: PVec): PMat;
var i, j: Int32;
begin
    New(Assign);
    for i := 0 to 100 do
        for j := 0 to 100 do
            if i = dest then Assign^[i][j] := src^[j]
            else if i = j then Assign^[i][j] := 1
            else Assign^[i][j] := 0;
    if src <> Nil then Dispose(src)
end;

function Eval: PMat;
var
    dest, loop: Int32;
    src: PVec;
    body: PMat;
begin
    Eval := Assign(-1, nil);

    while not Eof do begin
        ReadLn(buf);
        idx := 1;
        while buf[idx] = ' ' do idx := idx + 1;
        if buf[idx] = '}' then Exit
        else if (buf[idx] >= 'a') and (buf[idx] <= 'z') then begin
            dest := ParseVar;
            idx := idx + 3;
            src := ParseExpr;
            Eval := MatMulDispose(Assign(dest, src), Eval)
        end else if (buf[idx] >= '0') and (buf[idx] <= '9') then begin
            loop := ParseLit;
            body := Eval();
            Eval := MatMulDispose(MatPowDispose(body, loop), Eval)
        end else if buf[idx] = 'R' then begin
            idx := idx + 7;
            Eval := MatMulDispose(Assign(0, ElemVec(ParseVar)), Eval);
            Exit
        end else WriteLn('fatal error')
    end
end;

var prog: PMat;
begin
    New(env);
    FillChar(env^, SizeOf(TTrie), 0);
    env^.idx := 0;
    cnt := 1;

    prog := Eval;
    WriteLn(prog^[0][0]);

    { prog and env disposed by OS }
end.
