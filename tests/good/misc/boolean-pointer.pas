program BooleanPointer;

type
  PBoolean = ^boolean;

var
  isOpen: PBoolean;
  isLocked: PBoolean;

procedure setFlag(flag: PBoolean; value: boolean);
begin
  flag^ := value;
end;

procedure toggle(flag: PBoolean);
begin
  if flag^ then
    flag^ := false
  else
    flag^ := true;
end;

function chooseFlag(a, b: PBoolean; chooseA: boolean): PBoolean;
begin
  if chooseA then
    chooseFlag := a
  else
    chooseFlag := b;
end;

var
  chosen: PBoolean;

begin
  New(isOpen);
  New(isLocked);

  setFlag(isOpen, true);
  setFlag(isLocked, false);

  toggle(isLocked);

  chosen := chooseFlag(isOpen, isLocked, false);

  writeln(isOpen^);
  writeln(isLocked^);
  writeln(chosen^);

  Dispose(isLocked);
  Dispose(isOpen);
end.
