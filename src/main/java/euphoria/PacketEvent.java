package euphoria;

import euphoria.WebsocketJSON.StandardPacket;

import java.util.EventListener;
import java.util.EventObject;

public class PacketEvent extends EventObject {
  private StandardPacket packet;
  private RoomConnection room;
  
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