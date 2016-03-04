package euphoria.events;

import java.util.EventListener;

public interface PacketEventListener extends EventListener {
  public void onSendEvent(MessageEvent evt);
  public void onSnapshotEvent(PacketEvent evt);
  public void onHelloEvent(PacketEvent evt);
  public void onNickEvent(PacketEvent evt);
  public void onJoinEvent(PacketEvent evt);
  public void onPartEvent(PacketEvent evt);
  public void onBounceEvent(PacketEvent evt);
}
