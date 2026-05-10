program FibonacciSlidingWindow;

var
    N, A, B, i;

begin
    N := 10;
    A := 0;
    B := 1;

    for i := 1 to N - 1 do
    begin
        B := A + B;
        A := B - A
    end;

    WriteLn('The ', N, 'th Fibonacci number is ', B, '.')
end.
