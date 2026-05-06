program copy_global_array_of_records_to_heap;

type
  Item = record
    sku: integer;
    quantity: integer;
  end;

  ItemArray = array[0..2] of Item;
  PItemArray = ^ItemArray;

var
  globalItems: ItemArray;
  heapItems: PItemArray;

begin
  New(heapItems);

  globalItems[0].sku := 201;
  globalItems[0].quantity := 50;

  globalItems[1].sku := 202;
  globalItems[1].quantity := 60;

  globalItems[2].sku := 203;
  globalItems[2].quantity := 70;

  heapItems^ := globalItems;

  globalItems[0].sku := 999;
  globalItems[1].quantity := 999;
  globalItems[2].sku := 999;

  writeln(heapItems^[0].sku);       { expect 201 }
  writeln(heapItems^[0].quantity);  { expect 50 }
  writeln(heapItems^[1].sku);       { expect 202 }
  writeln(heapItems^[1].quantity);  { expect 60 }
  writeln(heapItems^[2].sku);       { expect 203 }
  writeln(heapItems^[2].quantity);  { expect 70 }

  Dispose(heapItems);
end.
