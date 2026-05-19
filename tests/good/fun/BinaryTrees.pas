program BinaryTrees;

const
  MaxDepth = 14;
  Iterations = 2000;

type
  PNode = ^TNode;
  TNode = record
    left: PNode;
    right: PNode;
    value: Integer;
  end;

var
  alloc_count: Integer;
  free_count: Integer;
  failed: Boolean;

function BuildTree(depth: Integer; seed: Integer): PNode;
var
  n: PNode;
begin
  New(n);
  alloc_count := alloc_count + 1;

  n^.value := seed;

  if depth = 0 then begin
    n^.left := nil;
    n^.right := nil;
  end else begin
    n^.left := BuildTree(depth - 1, seed * 2);
    n^.right := BuildTree(depth - 1, seed * 2 + 1);
  end;

  BuildTree := n;
end;

function CountNodes(n: PNode): Integer;
begin
  if n = nil then
    CountNodes := 0
  else
    CountNodes := 1 + CountNodes(n^.left) + CountNodes(n^.right);
end;

function SumValues(n: PNode): Integer;
begin
  if n = nil then
    SumValues := 0
  else
    SumValues := n^.value + SumValues(n^.left) + SumValues(n^.right);
end;

function CheckShape(n: PNode; depth: Integer): Boolean;
begin
  if depth = 0 then
    CheckShape := (n <> nil) and (n^.left = nil) and (n^.right = nil)
  else
    CheckShape :=
      (n <> nil) and
      (n^.left <> nil) and
      (n^.right <> nil) and
      CheckShape(n^.left, depth - 1) and
      CheckShape(n^.right, depth - 1);
end;

procedure FreeTree(n: PNode);
begin
  if n <> nil then begin
    FreeTree(n^.left);
    FreeTree(n^.right);

    n^.left := nil;
    n^.right := nil;
    n^.value := -999999;

    Dispose(n);
    free_count := free_count + 1;
  end;
end;

function ExpectedNodes(depth: Integer): Integer;
var
  i: Integer;
  r: Integer;
begin
  r := 1;
  for i := 1 to depth do
    r := r * 2 + 1;
  ExpectedNodes := r;
end;

procedure RunOne(depth: Integer; seed: Integer);
var
  root: PNode;
  nodes: Integer;
  expected: Integer;
  sum: Integer;
begin
  root := BuildTree(depth, seed);

  expected := ExpectedNodes(depth);
  nodes := CountNodes(root);
  sum := SumValues(root);

  if nodes <> expected then begin
    WriteLn('FAIL: node count mismatch');
    WriteLn('depth=', depth, ' expected=', expected, ' got=', nodes);
    failed := true;
  end;

  if not CheckShape(root, depth) then begin
    WriteLn('FAIL: tree shape mismatch');
    WriteLn('depth=', depth);
    failed := true;
  end;

  if seed mod 500 = 0 then begin
    WriteLn('iter=', seed, ' depth=', depth, ' nodes=', nodes, ' sum=', sum);
  end;

  FreeTree(root);
end;

var
  i: Integer;
  depth: Integer;
  expected_total: Integer;

begin
  alloc_count := 0;
  free_count := 0;
  failed := false;

  for i := 1 to Iterations do begin
    depth := MaxDepth - (i mod 4);
    if not failed then
      RunOne(depth, i);
  end;

  if alloc_count <> free_count then begin
    WriteLn('FAIL: allocation/free count mismatch');
    WriteLn('alloc_count=', alloc_count, ' free_count=', free_count);
    failed := true;
  end;

  expected_total := 0;
  for i := 1 to Iterations do begin
    depth := MaxDepth - (i mod 4);
    expected_total := expected_total + ExpectedNodes(depth);
  end;

  if alloc_count <> expected_total then begin
    WriteLn('FAIL: total allocation count mismatch');
    WriteLn('expected_total=', expected_total, ' alloc_count=', alloc_count);
    failed := true;
  end;

  if failed then
    WriteLn('FAIL')
  else begin
    WriteLn('PASS');
    WriteLn('alloc_count=', alloc_count);
    WriteLn('free_count=', free_count);
  end;
end.
