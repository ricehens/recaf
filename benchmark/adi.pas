program PolybenchAdi;

{ https://github.com/MatthiasJReisinger/PolyBenchC-4.2.1/blob/master/stencils/adi/adi.c }

const
    N = 400;
    TSTEPS = 200;
    SCALE = $100000;
var
    u, v, p, q: Array[0..N-1, 0..N-1] of Int64;

procedure init_array;
var i, j: Integer;
begin
    for i := 0 to N - 1 do
        for j := 0 to N - 1 do
            u[i, j] := SCALE * (i + N - j) div N;
end;

procedure print_array;
var i, j: Integer;
begin
    for i := 0 to N - 1 do
        for j := 0 to N - 1 do begin
            Write(u[i, j], ' ');
            if (i * N + j) mod 20 = 19 then
                WriteLn
        end
end;

procedure kernel_adi;
var
    t, i, j: Integer;
    DX, DY, DT: Int64;
    B1, B2: Int64;
    mul1, mul2: Int64;
    a, b, c, d, e, f: Int64;
begin
    DX := SCALE div N;
    DY := SCALE div N;
    DT := SCALE div TSTEPS;
    B1 := SCALE * 2;
    B2 := SCALE;
    mul1 := SCALE * B1 * DT div (DX * DX);
    mul2 := SCALE * B2 * DT div (DY * DY);

    a := -mul1 div 2;
    b := SCALE + mul1;
    c := a;
    d := -mul2 div 2;
    e := SCALE + mul2;
    f := d;

    for t := 1 to TSTEPS do begin
        { Column Sweep }
        for i := 1 to N - 2 do begin
            v[0, i] := SCALE;
            p[i, 0] := 0;
            q[i, 0] := v[0, i];
            for j := 1 to N - 2 do begin
                p[i, j] := SCALE * (-c) div (a * p[i, j - 1] div SCALE + b);
                q[i, j] := SCALE * (-d * u[j, i - 1] div SCALE +
                  (SCALE + 2 * d) * u[j, i] div SCALE
                  - f * u[j, i + 1] div SCALE - a * q[i, j - 1] div SCALE)
                  div (a * p[i, j - 1] div SCALE + b)
            end;

            v[N - 1, i] := SCALE;
            for j := N - 2 downto 1 do
                v[j, i] := p[i, j] * v[j + 1, i] div SCALE + q[i, j];
        end;

        { Row Sweep }
        for i := 1 to N - 2 do begin
            u[i, 0] := SCALE;
            p[i, 0] := 0;
            q[i, 0] := u[i, 0];
            for j := 1 to N - 2 do begin
                p[i, j] := SCALE * (-f) div (d * p[i, j - 1] div SCALE + e);
                q[i, j] := SCALE * (-a * v[i - 1, j] div SCALE +
                  (SCALE + 2 * a) * v[i, j] div SCALE
                  - c * v[i + 1, j] div SCALE - d * q[i, j - 1] div SCALE)
                  div (d * p[i, j - 1] div SCALE + e)
            end;

            u[i, N - 1] := SCALE;
            for j := N - 2 downto 1 do
                u[i, j] := p[i, j] * u[i, j + 1] div SCALE + q[i, j]
        end
    end
end;

begin
    init_array;
    kernel_adi;
    print_array
end.
