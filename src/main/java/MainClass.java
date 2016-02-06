
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import euphoria.*;
import euphoria.PacketEvent;
import euphoria.WebsocketJSON.*;
public class MainClass {
  List<RoomConnection> connections = new ArrayList<RoomConnection>();
  Console console;
  public MainClass() {
    initConsole();
    RoomConnection connection = new RoomConnection();
    connection.createConnection("xkcd");
    connection.addPacketEventListener(new StandardEventListener("TauBot","I'm a test bot made by @TauNeutrin0. Hi!"));
    connection.addPacketEventListener(new PacketEventListener(){
        @Override
        public void HelloEvent(PacketEvent arg0) {}
        @Override
        public void JoinEvent(PacketEvent arg0) {}
        @Override
        public void NickEvent(PacketEvent arg0) {}
        @Override
        public void PartEvent(PacketEvent arg0) {}
        @Override
        public void SendEvent(PacketEvent arg0) {
          StandardPacket packet = arg0.getPacket();
          if(((SendEvent)packet.getData()).getSession().getName().equals("TauNeutrin0")&&Math.random()>0.9){
            arg0.getRoomConnection().sendServerMessage(((SendEvent)packet.getData()).createReply("@TauNeutrin0 has spoken!"));
          }
        }
        @Override
        public void SnapshotEvent(PacketEvent arg0) {}
    });
    connections.add(connection);
  }
  
  public void initConsole() {
    console = new euphoria.Console();
    console.addWindowListener(new WindowListener(){

        @Override
        public void windowActivated(WindowEvent arg0) {}
        @Override
        public void windowClosed(WindowEvent arg0) {}
        @Override
        public void windowClosing(WindowEvent arg0) {
          System.out.println("Closing connections...");
          MainClass.this.closeAll();
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
  
  public void closeAll() {
    for(int i=0;i<connections.size();i++) {
      connections.get(i).closeConnection("Program exiting...");
    }
  }
  
  public static void main(String[] args) {
    new MainClass();
  }
}
