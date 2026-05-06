program pointer_array_of_records_inventory;

type
  Item = record
    sku: integer;
    quantity: integer;
  end;

  ItemArray = array[0..2] of Item;
  PItemArray = ^ItemArray;

var
  items: PItemArray;
  total: integer;

procedure initItem(items: PItemArray; index, sku, quantity: integer);
begin
  items^[index].sku := sku;
  items^[index].quantity := quantity;
end;

function totalQuantity(items: PItemArray): integer;
begin
  totalQuantity :=
    items^[0].quantity +
    items^[1].quantity +
    items^[2].quantity;
end;

begin
  New(items);

  initItem(items, 0, 101, 5);
  initItem(items, 1, 102, 10);
  initItem(items, 2, 103, 15);

  items^[1].quantity := items^[1].quantity + 7;

  total := totalQuantity(items);

  writeln(items^[0].sku);       { expect 101 }
  writeln(items^[1].quantity);  { expect 17 }
  writeln(total);               { expect 37 }

  Dispose(items);
end.
