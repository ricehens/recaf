program btree;

type
    PNode = ^TNode;
    TNode = record
        left, right: PNode;
    end;

function checksum(node: PNode): Int32;
begin
    if node^.left = nil then checksum := 1
    else checksum := 1 + checksum(node^.left) + checksum(node^.right);
end;

function make_tree(depth: Int32): PNode;
begin
    New(make_tree);
    if depth = 0 then begin
        make_tree^.left := nil;
        make_tree^.right := nil
    end else begin
        make_tree^.left := make_tree(depth - 1);
        make_tree^.right := make_tree(depth - 1)
    end;
end;

const
    min_depth = 4;
    max_depth = 21;
    init_iter = $200000;
var
    stretch_depth, check, depth, iter, i: Int32;
    stretch_tree, long_lived_tree, current_tree: PNode;
begin
    stretch_depth := max_depth + 1;
    stretch_tree := make_tree(stretch_depth);
    check := checksum(stretch_tree);
    stretch_tree := nil;

    WriteLn('stretch tree of depth ', stretch_depth, #9' check: ', check);

    long_lived_tree := make_tree(max_depth);
    depth := min_depth;
    iter := init_iter;
    while depth <= max_depth do begin
        check := 0;

        for i := 1 to iter do begin
            current_tree := make_tree(depth);
            check := check + checksum(current_tree);
        end;
        WriteLn(iter, #9' trees of depth ', depth, #9' check: ', check);

        depth := depth + 2;
        iter := iter div 4
    end;

    WriteLn('long lived tree of depth ', max_depth, #9' check: ', checksum(long_lived_tree));
end.
