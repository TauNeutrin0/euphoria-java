package euphoria;

import java.util.EventListener;

public interface PacketEventListener extends EventListener {
  public void SendEvent(PacketEvent evt);
  public void SnapshotEvent(PacketEvent evt);
  public void HelloEvent(PacketEvent evt);
  public void NickEvent(PacketEvent evt);
  public void JoinEvent(PacketEvent evt);
  public void PartEvent(PacketEvent evt);
}
