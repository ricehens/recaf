program array_read;

var
    a: array[0..999, 0..999];
    stdin: array[0..0];
    M, N, i, j, k;
begin
    ReadLn(M, N);

    for i := 0 to M - 1 do
        for j := 0 to N - 1 do 
            Read(A[i, j]);
    
    WriteLn(A[m - 1, n - 1]);
end.

