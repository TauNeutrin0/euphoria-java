package euphoria;

import euphoria.ConnectionEvent;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.swing.event.EventListenerList;

public abstract class Bot {
  
  List<RoomConnection> connections = new ArrayList<RoomConnection>();
  private EventListenerList roomListeners = new EventListenerList();
  Console console;
  String botName;
  
  public Bot(String botName) {
    this.botName = botName;
  }
  
  public void addListener(PacketEventListener eL) {
    roomListeners.add(PacketEventListener.class,eL);
    for(int i=0;i<connections.size();i++) {
      connections.get(i).addPacketEventListener(eL);
    }
  }
  
  public void removeListener(PacketEventListener eL) {
    roomListeners.remove(PacketEventListener.class,eL);
    for(int i=0;i<connections.size();i++) {
      connections.get(i).removePacketEventListener(eL);
    }
  }
  
  public void connectRoom(String roomName) {
    RoomConnection connection = new RoomConnection(roomName,roomListeners);
    new Thread(connection).start();
    connection.addConnectionEventListener(new ConnectionEventListener(){
        @Override
        public void onConnect(ConnectionEvent evt) {
          Bot.this.connections.add(evt.getRoomConnection());
        }
        @Override
        public void onDisconnect(ConnectionEvent evt) {
          Bot.this.connections.remove(evt.getRoomConnection());
        }
        @Override
        public void onConnectionError(ConnectionEvent arg0) {
        }
    });
  }
  
  public void closeAll() {
    for(int i=0;i<connections.size();i++) {
      connections.get(i).closeConnection("Program exiting...");
    }
  }
  
  public void disconnectRoom(String roomName) {
    for(int i=0;i<connections.size();i++) {
      if(connections.get(i).getRoom().equals(roomName)){
        connections.get(i).closeConnection("Bot disconnecting...");
        connections.remove(i);
      }
    }
  }
  
  public void initConsole() {
    console = new euphoria.Console(botName);
    console.addWindowListener(new WindowListener(){
        @Override
        public void windowActivated(WindowEvent arg0) {}
        @Override
        public void windowClosed(WindowEvent arg0) {}
        @Override
        public void windowClosing(WindowEvent arg0) {
          System.out.println("Closing connections...");
          closeAll();
          System.exit(0);
        }
        @Override
        public void windowDeactivated(WindowEvent arg0) {}
        @Override
        public void windowDeiconified(WindowEvent arg0) {}
        @Override
        public void windowIconified(WindowEvent arg0) {}
        @Override
        public void windowOpened(WindowEvent arg0) {}
    });
  }
}
