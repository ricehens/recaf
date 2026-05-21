program CowBASIC;

{ https://usaco.org/index.php?page=viewproblem2&cpid=746 }

const
    p = 1000000007;
    MAX = 100;
    MAX_LINE = 350;

type
    PTrie = ^TTrie;
    TTrie = record
        idx: Integer;
        next: array[97..122] of PTrie;
    end;

var
    cnt: Integer;
    env: PTrie;

type
    PVec = ^TVec;
    TVec = Array[0..MAX] of Int64;
    PMat = ^TMat;
    TMat = Array[0..MAX] of TVec;

type 
    String = Array[0..MAX_LINE] of Integer;

var
    buf: String;
    idx: Integer;

function ParseLit: Integer;
var val: Integer;
begin
    val := 0;
    while (idx <= buf[0]) and (buf[idx] >= Char('0')) and (buf[idx] <= Char('9')) do begin
        val := (val * 10) + (buf[idx] - Char('0'));
        idx := idx + 1
    end;
    ParseLit := val
end;

function ParseVar: Integer;
var now: PTrie;
begin
    now := env;
    while (idx <= buf[0]) and (buf[idx] >= Char('a')) and (buf[idx] <= Char('z')) do begin
        if now^.next[buf[idx]] = Nil then begin
            New(now^.next[buf[idx]]);
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
    i: Integer;
    lit: Int64;
    left, right: PVec;
begin
    New(ParseExpr);
    for i := 0 to 100 do ParseExpr^[i] := 0;
    if (buf[idx] >= Char('0')) and (buf[idx] <= Char('9')) then 
        ParseExpr^[0] := ParseLit
    else if (buf[idx] >= Char('a')) and (buf[idx] <= Char('z')) then 
        ParseExpr^[ParseVar] := 1
    else if buf[idx] = Char('(') then begin
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

function ElemVec(i: Integer): PVec;
var j: Integer;
begin
    New(ElemVec);
    for j := 0 to 100 do ElemVec^[j] := 0;
    ElemVec^[i] := 1
end;

function MatMul(a, b: PMat): PMat;
var i, j, k: Integer;
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

function MatPow(m: PMat; e: Integer): PMat;
begin
    if e = 1 then MatPow := m
    else if e mod 2 = 0 then MatPow := MatPow(MatMul(m, m), e div 2)
    else MatPow := MatMul(m, MatPow(m, e - 1))
end;

function MatPowDispose(m: PMat; e: Integer): PMat;
begin
    MatPowDispose := MatPow(m, e);
    Dispose(m)
end;

function Assign(dest: Integer; src: PVec): PMat;
var i, j: Integer;
begin
    New(Assign);
    for i := 0 to 100 do
        for j := 0 to 100 do
            if i = dest then Assign^[i][j] := src^[j]
            else if i = j then Assign^[i][j] := 1
            else Assign^[i][j] := 0;
    Dispose(src)
end;

function Eval: PMat;
var
    i, j, dest, ret, loop: Integer;
    src: PVec;
    body: PMat;
begin
    Eval := Assign(-1, nil);

    while not Eof do begin
        ReadLn(buf);
        idx := 1;
        while buf[idx] = Char(' ') do idx := idx + 1;
        if buf[idx] = Char('}') then Exit
        else if (buf[idx] >= Char('a')) and (buf[idx] <= Char('z')) then begin
            dest := ParseVar;
            idx := idx + 3;
            src := ParseExpr;
            Eval := MatMulDispose(Assign(dest, src), Eval)
        end else if (buf[idx] >= Char('0')) and (buf[idx] <= Char('9')) then begin
            loop := ParseLit;
            body := Eval();
            Eval := MatMulDispose(MatPowDispose(body, loop), Eval)
        end else if buf[idx] = Char('R') then begin
            idx := idx + 7;
            Eval := MatMulDispose(Assign(0, ElemVec(ParseVar)), Eval);
            Exit
        end else WriteLn('fatal error')
    end
end;

var prog: PMat;
begin
    New(env);
    env^.idx := 0;
    cnt := 1;

    prog := Eval;
    WriteLn(prog^[0][0]);

    { prog and env disposed by OS }
end.
