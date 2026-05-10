program JacobiSymbol;

(* Computes Jacobi symbol (a/n)
   Assumes n is positive and odd *)
function Jacobi(a, n: int64): integer;
begin
    a := (a mod n + n) mod n;

    if a = 0 then begin
        if n = 1 then Jacobi := 1 else Jacobi := 0;
        Exit
    end;

    if a mod 2 = 0 then begin
        if (n mod 8 = 3) or (n mod 8 = 5) 
        then Jacobi := -Jacobi(a div 2, n)
        else Jacobi := Jacobi(a div 2, n);
        Exit
    end;

    if (a mod 4 = 3) and (n mod 4 = 3)
    then Jacobi := -Jacobi(n, a)
    else Jacobi := Jacobi(n, a)
end;

begin
    WriteLn('(1001/9907) = ', Jacobi(1001, 9907));
    WriteLn('(19/45) = ', Jacobi(19, 45));
    WriteLn('(8/21) = ', Jacobi(8, 21));
    WriteLn('(5/21) = ', Jacobi(5, 21))
end.
