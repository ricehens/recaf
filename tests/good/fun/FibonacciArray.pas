program FibonacciArray;

var
    N, i;
    Fib: array[0..100] of Integer;

begin
    N := 10;
    Fib[0] := 0;
    Fib[1] := 1;

    for i := 2 to N do
        Fib[i] := Fib[i - 1] + Fib[i - 2];

    WriteLn('The ', N, 'th Fibonacci number is ', Fib[N], '.')
end.
