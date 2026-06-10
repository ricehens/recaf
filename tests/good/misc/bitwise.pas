program Bitwise;

uses Bits;

begin
    WriteLn(andq(3, 5));
    WriteLn(orq(3, 5));
    WriteLn(xorq(3, 5));
    WriteLn(shlq(1, 20));
    WriteLn(shrq(30, 3));
    WriteLn(shrl(-30, 3));
    WriteLn(sarq(-30, 3))
end.
