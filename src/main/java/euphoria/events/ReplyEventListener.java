package euphoria.events;

import java.util.EventListener;

public interface ReplyEventListener extends EventListener {
  public void onReplyEvent(PacketEvent evt);
  public void onReplySuccess(PacketEvent evt);
  public void onReplyFail(PacketEvent evt);
}
