program InventorySystem;

type
  PItem = ^Item;
  Item = record
    sku: integer;
    quantity: integer;
    reorderLevel: integer;
  end;

var
  items: array[0..2] of PItem;
  low: PItem;

procedure initItem(item: PItem; sku, quantity, reorderLevel: integer);
begin
  item^.sku := sku;
  item^.quantity := quantity;
  item^.reorderLevel := reorderLevel;
end;

procedure sell(item: PItem; amount: integer);
begin
  item^.quantity := item^.quantity - amount;
end;

function lowerStock(a, b: PItem): PItem;
begin
  if a^.quantity <= b^.quantity then
    lowerStock := a
  else
    lowerStock := b;
end;

function needsReorder(item: PItem): boolean;
begin
  needsReorder := item^.quantity <= item^.reorderLevel 
end;

begin
  New(items[0]);
  New(items[1]);
  New(items[2]);

  initItem(items[0], 101, 50, 10);
  initItem(items[1], 102, 20, 15);
  initItem(items[2], 103, 80, 25);

  sell(items[0], 5);
  sell(items[1], 10);
  sell(items[2], 60);

  low := lowerStock(items[0], items[1]);
  low := lowerStock(low, items[2]);

  writeln(items[0]^.quantity);  { expect 45 }
  writeln(items[1]^.quantity);  { expect 10 }
  writeln(items[2]^.quantity);  { expect 20 }

  writeln(low^.sku);            { expect 102 }
  writeln(needsReorder(low));   { expect TRUE }

  Dispose(items[2]);
  Dispose(items[1]);
  Dispose(items[0]);
end.
