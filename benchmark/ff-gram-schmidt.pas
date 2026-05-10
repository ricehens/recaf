program FiniteFieldGramSchmidt;

{
    Finite field Gram-Schmidt: given n vectors of dimension d in F_p,
    finds an orthogonal basis of the subspace spanned by these n vectors.
    Here, p=1009.

    INPUT:
        line 1: space-separated d <= 1000, n <= 1000
        line 2..n+1: d space-separated residues modulo p

    OUTPUT:
        lines 1..n: d space-separated residues 0,..,p-1
        pairwise orthogonal, spanning the original subspace
        (some might be all-zeros if original vectors are not spanning)
}
const p = 1009; (* characteristic *)
var
    n, (* num of vectors *)
    d; (* dimension *)
    Mat: Array[0..999, 0..999] of integer;

(* compute a^e mod p *)
function Pow(a, e);
var b;
begin
    if e = 0 then
        Pow := 1
    else if e mod 2 = 1 then
        Pow := (a * Pow(a, e - 1)) mod p
    else
    begin
        b := Pow(a, e div 2);
        Pow := (b * b) mod p
    end
end;

(* compute dot product of ith and jth row, mod p *)
function Dot(i, j);
var k;
begin
    dot := 0;
    for k := 0 to d - 1 do
        dot := (dot + Mat[i, k] * Mat[j, k]) mod p;
end;

(* adds ith row to jth row, mod p *)
procedure Add(i, j);
var k;
begin
    for k := 0 to d - 1 do
        Mat[j, k] := (Mat[j, k] + Mat[i, k]) mod p;
end;

(* swap ith and jth row *)
procedure Swap(i, j);
var k, t;
begin
    for k := 0 to d - 1 do
    begin
        t := Mat[i, k];
        Mat[i, k] := Mat[j, k];
        Mat[j, k] := t
    end
end;

(*
    subtracts from jth vector the projection
    of the jth vector on to the ith vector,
    mod p
*)
procedure Orthogonalize(i, j);
var proj, k;
begin
    proj := (Dot(i, j) * Pow(Dot(i, i), p - 2)) mod p;
    for k := 0 to d - 1 do
        Mat[j, k] := (Mat[j, k] - ((proj * Mat[i, k]) mod p) + p) mod p;
end;

var k, i, j;
begin
    ReadLn(d, n);

    for k := 0 to n - 1 do
        for i := 0 to d - 1 do
            Read(Mat[k, i]);

    for k := 0 to n - 1 do
    begin
        i := k;
        while i < n do
        begin
            if Dot(i, i) <> 0 then
                break;
            i := i + 1
        end;

        if i < n then
            Swap(k, i)
        else
        begin
            i := k;
            while i < n do
            begin
                j := i + 1;
                while j < n do
                begin
                    if Dot(i, j) <> 0 then
                        break;
                    j := j + 1
                end;

                if j < n then
                    break;
                i := i + 1
            end;

            if i < n then
            begin
                Add(j, i);
                Swap(k, i)
            end
        end;

        if Dot(k, k) <> 0 then
        begin
            i := k + 1;
            while i < n do
            begin
                Orthogonalize(k, i);
                i := i + 1
            end
        end
    end;

    for k := 0 to n - 1 do
    begin
        for i := 0 to d - 1 do
            Write(Mat[k, i], ' ');
        WriteLn()
    end
end.

