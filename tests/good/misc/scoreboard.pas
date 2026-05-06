program Scoreboard;

type
  ScoreArray = array[0..4] of integer;
  PScoreArray = ^ScoreArray;

var
  scores: PScoreArray;
  backup: PScoreArray;
  chosen: PScoreArray;

procedure clearScores(a: PScoreArray);
begin
  a^[0] := 0;
  a^[1] := 0;
  a^[2] := 0;
  a^[3] := 0;
  a^[4] := 0;
end;

procedure addScore(a: PScoreArray; index, amount: integer);
begin
  a^[index] := a^[index] + amount;
end;

function sumScores(a: PScoreArray): integer;
begin
  sumScores := a^[0] + a^[1] + a^[2] + a^[3] + a^[4];
end;

function higherTotal(a, b: PScoreArray): PScoreArray;
begin
  if sumScores(a) >= sumScores(b) then
    higherTotal := a
  else
    higherTotal := b;
end;

begin
  New(scores);
  New(backup);

  clearScores(scores);
  clearScores(backup);

  addScore(scores, 0, 10);
  addScore(scores, 1, 20);
  addScore(scores, 2, 30);
  addScore(scores, 3, 40);
  addScore(scores, 4, 50);

  addScore(backup, 0, 5);
  addScore(backup, 1, 5);
  addScore(backup, 2, 5);
  addScore(backup, 3, 5);
  addScore(backup, 4, 5);

  chosen := higherTotal(scores, backup);

  writeln(sumScores(scores));  { expect 150 }
  writeln(sumScores(backup));  { expect 25 }
  writeln(sumScores(chosen));  { expect 150 }

  chosen^[2] := chosen^[2] + 7;

  writeln(scores^[2]);         { expect 37 }

  Dispose(backup);
  Dispose(scores);
end.
