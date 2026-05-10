program FibonacciRecursive;

function Fib(n);
begin
    if n <= 1 then Fib := n
    else Fib := Fib(n - 1) + Fib(n - 2)
end;

var
    N;
begin
    N := 10;
    WriteLn('The ', N, 'th Fibonacci number is ', Fib(N), '.')
end.
