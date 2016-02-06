
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
        public void onSendEvent(MessageEvent arg0) {
          if(arg0.getSender().equals("TauNeutrin0")&&Math.random()>0.9){
            arg0.reply("@TauNeutrin0 has spoken!");
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
