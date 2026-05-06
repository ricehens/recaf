program pointer_game_entities;

type
  PEntity = ^Entity;
  Entity = record
    id: integer;
    x: integer;
    y: integer;
    health: integer;
    score: integer;
  end;

var
  player: PEntity;
  enemy: PEntity;
  target: PEntity;

procedure move(entity: PEntity; dx, dy: integer);
begin
  entity^.x := entity^.x + dx;
  entity^.y := entity^.y + dy;
end;

procedure damage(entity: PEntity; amount: integer);
begin
  entity^.health := entity^.health - amount;
end;

procedure addScore(entity: PEntity; amount: integer);
begin
  entity^.score := entity^.score + amount;
end;

function weaker(a, b: PEntity): PEntity;
begin
  if a^.health <= b^.health then
    weaker := a
  else
    weaker := b;
end;

begin
  New(player);
  New(enemy);

  player^.id := 1;
  player^.x := 0;
  player^.y := 0;
  player^.health := 100;
  player^.score := 0;

  enemy^.id := 2;
  enemy^.x := 10;
  enemy^.y := 5;
  enemy^.health := 60;
  enemy^.score := 0;

  move(player, 3, 4);
  move(enemy, -2, 1);

  damage(enemy, 25);
  addScore(player, 10);

  target := weaker(player, enemy);

  writeln(player^.x);       { expect 3 }
  writeln(player^.y);       { expect 4 }
  writeln(player^.score);   { expect 10 }

  writeln(enemy^.x);        { expect 8 }
  writeln(enemy^.y);        { expect 6 }
  writeln(enemy^.health);   { expect 35 }

  writeln(target^.id);      { expect 2 }

  Dispose(enemy);
  Dispose(player);
end.
