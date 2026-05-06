program PingPongCollatz;


type
    Dir = (Prev, Next);


function next_odd(n); forward;

function next_even(n); forward;

function next(n);
begin
    if n mod 2 = 0 then
        next := next_even(n)
    else
        next := next_odd(n)
end;

function next_odd(n);
begin
    next_odd := 3 * n + 1
end;

function next_even(n);
begin
    next_even := n div 2
end;

function collatz(n);
begin
    WriteLn(n);
    if n = 1 then
        Exit();
    collatz(next(n))
end;


begin
    collatz(27)
end.
