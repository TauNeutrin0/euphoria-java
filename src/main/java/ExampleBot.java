
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import euphoria.*;
import euphoria.PacketEvent;
import euphoria.WebsocketJSON.*;

public class ExampleBot extends Bot{
  Console console;
  
  public ExampleBot() {
    initConsole();
    connectRoom("xkcd");
    addListener(new StandardEventListener("TauBot","I'm a test bot made by @TauNeutrin0. Hi!"));
    addListener(new MessageEventListener(){
        @Override
        public void SendEvent(PacketEvent arg0) {
          SendEvent data = ((SendEvent)arg0.getPacket().getData());
          if(data.getSession().getName().equals("TauNeutrin0")&&Math.random()>0.9){
            arg0.getRoomConnection().sendServerMessage(data.createReply("@TauNeutrin0 has spoken!"));
          }
        }
    });
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
          ExampleBot.this.closeAll();
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
  
  public static void main(String[] args) {
    new ExampleBot();
  }
}
