package euphoria;

import euphoria.RoomConnection;
import euphoria.WebsocketJSON.StandardPacket;

public class MessageEvent extends PacketEvent{
  public MessageEvent(RoomConnection roomCon, StandardPacket pckt) {
    super(roomCon, pckt);
  }
  public void reply(String message) {
    room.sendServerMessage(((euphoria.WebsocketJSON.SendEvent)packet.getData()).createReply(message));
  }
  public String getSender() {
    return ((euphoria.WebsocketJSON.SendEvent)packet.getData()).getSession().getName();
  }
  public String getMessage() {
    return ((euphoria.WebsocketJSON.SendEvent)packet.getData()).getMessage();
  }
}