package euphoria.events;

public abstract class MessageEventListener implements PacketEventListener{

    @Override
    public void onHelloEvent(PacketEvent arg0) {}

    @Override
    public void onJoinEvent(PacketEvent arg0) {}

    @Override
    public void onNickEvent(PacketEvent arg0) {}

    @Override
    public void onPartEvent(PacketEvent arg0) {}

    @Override
    public abstract void onSendEvent(MessageEvent evt);

    @Override
    public void onSnapshotEvent(PacketEvent arg0) {}
  
}
