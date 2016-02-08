package euphoria;

import java.util.EventListener;

public interface ConnectionEventListener extends EventListener {
  public void onConnect(ConnectionEvent evt);
  public void onDisconnect(ConnectionEvent evt);
  public void onConnectionError(ConnectionEvent evt);
}
