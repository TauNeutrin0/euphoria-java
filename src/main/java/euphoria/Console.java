package euphoria;

import euphoria.events.ConsoleEventListener;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class Console extends JFrame{
  
  private JTextField textField = new JTextField(50);
  private JTextArea textArea = new JTextArea(50,38);
  private JScrollPane scrollPane = new JScrollPane(textArea);
  private EventListenerList listeners = new EventListenerList();
  private final static String newline = "\n";
  
  public Console() {
    super("Euphoria Bot console");
    setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
    textArea.setEditable(false);
    this.add(scrollPane);
    textField.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        Console.this.submitCommand();
      }
    });
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.add(textField);
    this.pack();
    if(this.getHeight()>GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getHeight())
      this.setSize(this.getWidth(), (int) GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getHeight());
    redirectSystemStreams();
    this.setVisible(true);
  }
  
  private void updateTextArea(final String text) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Document doc = textArea.getDocument();
        try {
          doc.insertString(doc.getLength(), text, null);
        } catch (BadLocationException e) {
          throw new RuntimeException(e);
        }
        textArea.setCaretPosition(doc.getLength() - 1);
      }
    });
  }
  
  public void submitCommand() {
    updateTextArea(textField.getText()+newline);
    Object[] lns = listeners.getListenerList();
    for (int i = 0; i < lns.length; i = i+2) {
      if (lns[i] == ConsoleEventListener.class) {
        ((ConsoleEventListener) lns[i+1]).onCommand(textField.getText());
      }
    }
    textField.setText("");
  }
  
   private void redirectSystemStreams() {
    OutputStream out = new OutputStream() {
      @Override
      public void write(final int b) throws IOException {
        updateTextArea(String.valueOf((char) b));
      }

      @Override
      public void write(byte[] b, int off, int len) throws IOException {
        updateTextArea(new String(b, off, len));
      }

      @Override
      public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
      }
    };

    System.setOut(new PrintStream(out, true));
    System.setErr(new PrintStream(out, true));
  }
  
  public void addListener(ConsoleEventListener evtLst) {
    listeners.add(ConsoleEventListener.class, evtLst);
  }
  public void removeListener(ConsoleEventListener evtLst) {
    listeners.remove(ConsoleEventListener.class, evtLst);
  }
}
