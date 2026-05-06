program ObjectPool;

type
  PSlot = ^Slot;
  Slot = record
    id: integer;
    used: boolean;
    value: integer;
  end;

var
  pool: array[0..3] of PSlot;
  chosen: PSlot;

procedure initSlot(slot: PSlot; id: integer);
begin
  slot^.id := id;
  slot^.used := false;
  slot^.value := 0;
end;

procedure allocate(slot: PSlot; value: integer);
begin
  slot^.used := true;
  slot^.value := value;
end;

procedure release(slot: PSlot);
begin
  slot^.used := false;
  slot^.value := 0;
end;

function firstFree(a, b, c, d: PSlot): PSlot;
begin
  if not a^.used then
    firstFree := a
  else
    if not b^.used then
      firstFree := b
    else
      if not c^.used then
        firstFree := c
      else
        firstFree := d;
end;

begin
  New(pool[0]);
  New(pool[1]);
  New(pool[2]);
  New(pool[3]);

  initSlot(pool[0], 0);
  initSlot(pool[1], 1);
  initSlot(pool[2], 2);
  initSlot(pool[3], 3);

  allocate(pool[0], 100);
  allocate(pool[1], 200);
  allocate(pool[2], 300);

  chosen := firstFree(pool[0], pool[1], pool[2], pool[3]);
  allocate(chosen, 400);

  writeln(chosen^.id);     { expect 3 }
  writeln(pool[3]^.value); { expect 400 }

  release(pool[1]);

  chosen := firstFree(pool[0], pool[1], pool[2], pool[3]);

  writeln(chosen^.id);     { expect 1 }

  Dispose(pool[3]);
  Dispose(pool[2]);
  Dispose(pool[1]);
  Dispose(pool[0]);
end.
