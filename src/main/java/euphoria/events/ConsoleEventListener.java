package euphoria.events;

import java.util.EventListener;

public interface ConsoleEventListener extends EventListener{
  public void onCommand(String command);
}
