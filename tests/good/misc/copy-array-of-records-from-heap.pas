program copy_heap_array_of_records_to_global;

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

  heapItems^[0].sku := 101;
  heapItems^[0].quantity := 5;

  heapItems^[1].sku := 102;
  heapItems^[1].quantity := 10;

  heapItems^[2].sku := 103;
  heapItems^[2].quantity := 15;

  globalItems := heapItems^;

  heapItems^[0].sku := 999;
  heapItems^[1].quantity := 999;
  heapItems^[2].sku := 999;

  writeln(globalItems[0].sku);       { expect 101 }
  writeln(globalItems[0].quantity);  { expect 5 }
  writeln(globalItems[1].sku);       { expect 102 }
  writeln(globalItems[1].quantity);  { expect 10 }
  writeln(globalItems[2].sku);       { expect 103 }
  writeln(globalItems[2].quantity);  { expect 15 }

  Dispose(heapItems);
end.
