package euphoria.events;

import euphoria.RoomConnection;
import euphoria.packets.StandardPacket;
import euphoria.packets.events.SendEvent;

public class MessageEvent extends PacketEvent{
  public MessageEvent(RoomConnection roomCon, StandardPacket pckt) {
    super(roomCon, pckt);
  }
  public void reply(String message) {
    room.sendPacket(((SendEvent)packet.getData()).createReply(message));
  }
  public void replyTracked(String message,ReplyEventListener evtLst) {
    room.sendPacket(((SendEvent)packet.getData()).createReply(message),evtLst);
  }
  public String getSender() {
    return ((SendEvent)packet.getData()).getSender().getName();
  }
  public String getMessage() {
    return ((SendEvent)packet.getData()).getContent();
  }
  public String getId() {
    return ((SendEvent)packet.getData()).getId();
  }
}