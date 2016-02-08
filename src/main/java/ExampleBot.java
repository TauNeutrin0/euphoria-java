
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import euphoria.*;

public class ExampleBot extends Bot{
  
  public ExampleBot() {
    super("TauBot");
    initConsole();
    connectRoom("bots");
    addListener(new StandardEventListener("TauBot","I'm a test bot made by @TauNeutrin0. Hi!"));
    addListener(new MessageEventListener(){
        @Override
        public void onSendEvent(MessageEvent arg0) {
          if(arg0.getSender().equals("TauNeutrin0")&&Math.random()>0.9){
            arg0.reply("@TauNeutrin0 has spoken!");
          }
          throw(new NullPointerException());
        }
    });
  }
  public static void main(String[] args) {
    new ExampleBot();
  }
}
