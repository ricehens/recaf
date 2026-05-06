program copy_record_with_array_local_heap_roundtrip;

type
  SensorPacket = record
    sensorId: integer;
    readings: array[0..3] of integer;
    checksum: integer;
  end;

  PSensorPacket = ^SensorPacket;

procedure computeChecksum(packet: PSensorPacket);
begin
  packet^.checksum :=
    packet^.sensorId +
    packet^.readings[0] +
    packet^.readings[1] +
    packet^.readings[2] +
    packet^.readings[3];
end;

procedure runPacketCopy;
var
  localPacket: SensorPacket;
  copiedBack: SensorPacket;
  heapPacket: PSensorPacket;
begin
  New(heapPacket);

  localPacket.sensorId := 5;
  localPacket.readings[0] := 10;
  localPacket.readings[1] := 20;
  localPacket.readings[2] := 30;
  localPacket.readings[3] := 40;
  localPacket.checksum := 105;

  heapPacket^ := localPacket;

  localPacket.sensorId := 0;
  localPacket.readings[0] := 0;
  localPacket.readings[1] := 0;
  localPacket.readings[2] := 0;
  localPacket.readings[3] := 0;
  localPacket.checksum := 0;

  heapPacket^.readings[2] := heapPacket^.readings[2] + 7;
  computeChecksum(heapPacket);

  copiedBack := heapPacket^;

  heapPacket^.sensorId := 999;
  heapPacket^.readings[2] := 999;
  heapPacket^.checksum := 999;

  writeln(copiedBack.sensorId);     { expect 5 }
  writeln(copiedBack.readings[0]);  { expect 10 }
  writeln(copiedBack.readings[1]);  { expect 20 }
  writeln(copiedBack.readings[2]);  { expect 37 }
  writeln(copiedBack.readings[3]);  { expect 40 }
  writeln(copiedBack.checksum);     { expect 112 }

  Dispose(heapPacket);
end;

begin
  runPacketCopy;
end.
