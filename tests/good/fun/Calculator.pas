program Calculator;

(* AST *)
type
    ExprKind = (ExprLit, ExprOp, ExprError);
    BinOp = (OpAdd, OpSub, OpMul, OpDiv);
    PExpr = ^TExpr;
    TExpr = record
        Kind: ExprKind;
        Value: Integer;
        Op: BinOp;
        Left, Right: PExpr;
    end;

function Lit(v: Integer): PExpr;
begin
    New(Lit);
    Lit^.Kind := ExprLit;
    Lit^.Value := v;
end;

function BinExpr(op: BinOp; l, r: PExpr): PExpr;
begin
    New(BinExpr);
    BinExpr^.Kind := ExprOp;
    BinExpr^.Op := op;
    BinExpr^.Left := l;
    BinExpr^.Right := r;
end;

function Add(l, r: PExpr): PExpr;
begin
    Add := BinExpr(OpAdd, l, r);
end;

function Sub(l, r: PExpr): PExpr;
begin
    Sub := BinExpr(OpSub, l, r);
end;

function Mul(l, r: PExpr): PExpr;
begin
    Mul := BinExpr(OpMul, l, r);
end;

function Divide(l, r: PExpr): PExpr;
begin
    Divide := BinExpr(OpDiv, l, r);
end;

function Eval(e: PExpr): Integer;
begin
    if e^.Kind = ExprLit then Eval := e^.Value
    else if e^.Op = OpAdd then Eval := Eval(e^.Left) + Eval(e^.Right)
    else if e^.Op = OpSub then Eval := Eval(e^.Left) - Eval(e^.Right)
    else if e^.Op = OpMul then Eval := Eval(e^.Left) * Eval(e^.Right)
    else if e^.Op = OpDiv then Eval := Eval(e^.Left) div Eval(e^.Right)
end;

procedure _PrintExpr(e: PExpr);
begin
    if e^.Kind = ExprLit then Write(e^.Value, '')
    else if e^.Kind = ExprError then Write('error')
    else begin
        Write('(');
        _PrintExpr(e^.Left);
        if e^.Op = OpAdd then Write(' + ')
        else if e^.Op = OpSub then Write(' - ')
        else if e^.Op = OpMul then Write(' * ')
        else if e^.Op = OpDiv then Write(' / ');
        _PrintExpr(e^.Right);
        Write(')')
    end
end;

procedure PrintExpr(e: PExpr);
begin
    _PrintExpr(e);
    WriteLn
end;

procedure DisposeExpr(e: PExpr);
begin
    if e^.Kind = ExprOp then begin
        DisposeExpr(e^.Left);
        DisposeExpr(e^.Right);
    end;
    Dispose(e);
end;

(* stdio *)
const MAX_LEN = 255;
type
    PBuffer = ^TBuffer;
    TBuffer = record
        Buf: array[0..MAX_LEN] of Integer;
        Len: Integer;
    end;

function getchar: Integer; external;
function ReadInput: PBuffer;
var i, c: Integer;
begin
    New(ReadInput);
    for i := 0 to MAX_LEN do begin
        c := getchar;
        if c = 10 then begin
            ReadInput^.Len := i;
            Exit
        end;
        if c = -1 then begin
            ReadInput^.Len := -1;
            Exit
        end;
        ReadInput^.Buf[i] := c
    end;
    ReadInput^.Len := MAX_LEN + 1
end;

procedure PrintBuffer(b: PBuffer);
var i: Integer;
begin
    for i := 0 to b^.Len - 1 do Write(b^.Buf[i], ' ');
    WriteLn
end;

(* lexer *)
type
    TokenKind = (TokenLit, TokenPlus, TokenMinus, TokenTimes,
        TokenSlash, TokenLParen, TokenRParen, TokenError);
    TToken = record
        Kind: TokenKind;
        Value: Integer;
    end;
    PTokenList = ^TTokenList;
    TTokenList = record
        Token: TToken;
        Next: PTokenList;
    end;
var
    Null: PTokenList;

function Lex(s: PBuffer; idx: Integer): PTokenList;
var c, v: Integer;
begin
    Lex := Null;
    while (idx < s^.len) and (s^.Buf[idx] = 32) do idx := idx + 1;
    if idx < s^.len then begin
        New(Lex);
        Lex^.Token.Value := idx;
        c := s^.Buf[idx];
        if c = 43 then Lex^.Token.Kind := TokenPlus
        else if c = 45 then Lex^.Token.Kind := TokenMinus
        else if c = 42 then Lex^.Token.Kind := TokenTimes
        else if c = 47 then Lex^.Token.Kind := TokenSlash
        else if c = 40 then Lex^.Token.Kind := TokenLParen
        else if c = 41 then Lex^.Token.Kind := TokenRParen
        else if (c >= 48) and (c <= 57) then begin
            Lex^.Token.Kind := TokenLit;
            v := 0;
            while (idx < s^.len) and (s^.Buf[idx] >= 48) and (s^.Buf[idx] <= 57) do begin
                v := v * 10 + s^.Buf[idx] - 48;
                idx := idx + 1
            end;
            Lex^.Token.Value := v;
            idx := idx - 1
        end else begin
            Lex^.Token.Kind := TokenError;
            Lex^.Token.Value := idx
        end;

        Lex^.Next := Lex(s, idx + 1);
        if (Lex^.Next <> Null) and (Lex^.Next^.Token.Kind = TokenError) then Lex^.Token := Lex^.Next^.Token
    end
end;

procedure PrintTokens(tokens: PTokenList);
begin
    while tokens <> Null do begin
        if tokens^.Token.Kind = TokenLit then Write(tokens^.Token.Value)
        else if tokens^.Token.Kind = TokenPlus then Write('+')
        else if tokens^.Token.Kind = TokenMinus then Write('-')
        else if tokens^.Token.Kind = TokenTimes then Write('*')
        else if tokens^.Token.Kind = TokenSlash then Write('/')
        else if tokens^.Token.Kind = TokenLParen then Write('(')
        else if tokens^.Token.Kind = TokenRParen then Write(')')
        else if tokens^.Token.Kind = TokenError then begin
            WriteLn('error');
            Exit
        end;
        Write(' ');
        tokens := tokens^.Next
    end;
    WriteLn
end;

procedure DisposeTokens(tokens: PTokenList);
begin
    if tokens <> Null then begin
        DisposeTokens(tokens^.Next);
        Dispose(tokens)
    end
end;

(* parser *)
type
    PParserState = ^TParserState;
    TParserState = record
        tokens: PTokenList;
    end;

function ParseAddSub(state: PParserState): PExpr; forward;
function ParseMulDiv(state: PParserState): PExpr; forward;
function ParseUnaryMinus(state: PParserState): PExpr; forward;
function ParsePrimary(state: PParserState): PExpr; forward;

procedure NextToken(state: PParserState);
begin
    state^.tokens := state^.tokens^.Next
end;

function ErrorToken(state: PParserState): PExpr;
begin
    New(ErrorToken);
    ErrorToken^.Kind := ExprError;
    if state^.tokens = Null then ErrorToken^.Value := -1
    else begin
        ErrorToken^.Value := state^.tokens^.Token.Value;
        NextToken(state)
    end
end;

function Parse(tokens: PTokenList): PExpr;
var state: PParserState;
begin
    New(state);
    state^.tokens := tokens;
    Parse := ParseAddSub(state);

    if state^.tokens <> Null then begin
        DisposeExpr(Parse);
        Parse := ErrorToken(state)
    end;
    Dispose(state)
end;

function GetTokenKind(state: PParserState): TokenKind;
begin
    if state^.tokens = Null then GetTokenKind := TokenError
    else GetTokenKind := state^.tokens^.Token.Kind
end;

function ParseAddSub(state: PParserState): PExpr;
var right: PExpr;
begin
    ParseAddSub := ParseMulDiv(state);
    if ParseAddSub^.Kind = ExprError then Exit;
    while True do 
        if GetTokenKind(state) = TokenPlus then begin
            NextToken(state);
            right := ParseMulDiv(state);
            if right^.Kind = ExprError then begin
                DisposeExpr(ParseAddSub);
                ParseAddSub := right;
                Exit
            end;
            ParseAddSub := Add(ParseAddSub, right)
        end else if GetTokenKind(state) = TokenMinus then begin
            NextToken(state);
            right := ParseMulDiv(state);
            if right^.Kind = ExprError then begin
                DisposeExpr(ParseAddSub);
                ParseAddSub := right;
                Exit
            end;
            ParseAddSub := Sub(ParseAddSub, right)
        end else Break
end;

function ParseMulDiv(state: PParserState): PExpr;
var right: PExpr;
begin
    ParseMulDiv := ParseUnaryMinus(state);
    if ParseMulDiv^.Kind = ExprError then Exit;
    while True do 
        if GetTokenKind(state) = TokenTimes then begin
            NextToken(state);
            right := ParseUnaryMinus(state);
            if right^.Kind = ExprError then begin
                DisposeExpr(ParseMulDiv);
                ParseMulDiv := right;
                Exit
            end;
            ParseMulDiv := Mul(ParseMulDiv, right)
        end else if GetTokenKind(state) = TokenSlash then begin
            NextToken(state);
            right := ParseUnaryMinus(state);
            if right^.Kind = ExprError then begin
                DisposeExpr(ParseMulDiv);
                ParseMulDiv := right;
                Exit
            end;
            ParseMulDiv := Divide(ParseMulDiv, right)
        end else Break
end;

function ParseUnaryMinus(state: PParserState): PExpr;
var right: PExpr;
begin
    if GetTokenKind(state) = TokenMinus then begin
        NextToken(state);
        right := ParsePrimary(state);
        if right^.Kind = ExprError then ParseUnaryMinus := right
        else ParseUnaryMinus := Sub(Lit(0), right)
    end else ParseUnaryMinus := ParsePrimary(state)
end;

function ParsePrimary(state: PParserState): PExpr;
begin
    if GetTokenKind(state) = TokenLit then begin
        New(ParsePrimary);
        ParsePrimary^.Kind := ExprLit;
        ParsePrimary^.Value := state^.tokens^.Token.Value;
        NextToken(state)
    end else if GetTokenKind(state) = TokenLParen then begin
        NextToken(state);
        ParsePrimary := ParseAddSub(state);
        if GetTokenKind(state) = TokenRParen then NextToken(state)
        else begin
            DisposeExpr(ParsePrimary);
            ParsePrimary := ErrorToken(state);
        end
    end else begin
        ParsePrimary := ErrorToken(state)
    end
end;

(* main *)
var Buf: PBuffer;
    Tokens: PTokenList;
    Expr: PExpr;
begin
    while True do begin
        Write('calc> ');
        Buf := ReadInput;
        if Buf^.Len = -1 then begin
            WriteLn;
            Dispose(Buf);
            Exit
        end;
        { PrintBuffer(Buf); }

        Tokens := Lex(Buf, 0);
        Dispose(Buf);
        { PrintTokens(Tokens); }

        Expr := Parse(Tokens);
        DisposeTokens(Tokens);
        { PrintExpr(Expr); }

        if Expr^.Kind = ExprError then WriteLn('error')
        else WriteLn(Eval(Expr));
        DisposeExpr(Expr);
    end
end.

