package euphoria.events;

import euphoria.RoomConnection;
import euphoria.packets.StandardPacket;

import java.util.EventObject;

public class PacketEvent extends EventObject {
  protected StandardPacket packet;
  protected RoomConnection room;
  
  public PacketEvent(RoomConnection roomCon,StandardPacket pckt) {
    super(roomCon);
    packet=pckt;
    room=roomCon;
  }
  public StandardPacket getPacket() {
    return packet;
  }
  public RoomConnection getRoomConnection() {
    return room;
  }
}