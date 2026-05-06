program pointer_heap_order_book;

type
  Order = record
    id: integer;
    quantity: integer;
    price: integer;
  end;

  OrderBook = array[0..2] of Order;
  POrderBook = ^OrderBook;

var
  activeBook: POrderBook;
  backupBook: POrderBook;
  selectedBook: POrderBook;

procedure initOrder(book: POrderBook; index, id, quantity, price: integer);
begin
  book^[index].id := id;
  book^[index].quantity := quantity;
  book^[index].price := price;
end;

function totalValue(book: POrderBook): integer;
begin
  totalValue :=
    book^[0].quantity * book^[0].price +
    book^[1].quantity * book^[1].price +
    book^[2].quantity * book^[2].price;
end;

function largerBook(a, b: POrderBook): POrderBook;
begin
  if totalValue(a) >= totalValue(b) then
    largerBook := a
  else
    largerBook := b;
end;

begin
  New(activeBook);
  New(backupBook);

  initOrder(activeBook, 0, 101, 2, 10);
  initOrder(activeBook, 1, 102, 3, 20);
  initOrder(activeBook, 2, 103, 4, 30);

  initOrder(backupBook, 0, 201, 1, 5);
  initOrder(backupBook, 1, 202, 1, 6);
  initOrder(backupBook, 2, 203, 1, 7);

  selectedBook := largerBook(activeBook, backupBook);

  writeln(totalValue(activeBook));  { expect 200 }
  writeln(totalValue(backupBook));  { expect 18 }
  writeln(selectedBook^[2].id);     { expect 103 }

  selectedBook^[2].quantity := selectedBook^[2].quantity + 1;

  writeln(activeBook^[2].quantity); { expect 5 }
  writeln(totalValue(activeBook));  { expect 230 }

  Dispose(backupBook);
  Dispose(activeBook);
end.
