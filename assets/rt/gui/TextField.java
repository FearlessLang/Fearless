package rt.gui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import base.Bool_0;
import base.False_0;
import base.MF_1;
import base.MF_2;
import base.True_0;
import base.Void_0;
import base.gui.TextField_0;

public class TextField implements TextField_0 {
  
  private final JTextField textField;
  private GuiBuilderState state;
  
  public TextField(String text, GuiBuilderState state) { this(text, 15, state); }
           
  public TextField(String text, int col, GuiBuilderState state) {
      this.textField = new JTextField(text, col);
      this.state = state;
  }
  @Override
  public Void_0 addActionListener$mut(MF_1 listener_m$) {
    textField.addActionListener(listener -> state.submitModelTask(listener_m$::$hash$mut));
    return Void_0.$self;
  }

  @Override
  public Void_0 enable$mut(Bool_0 b_m$) {
    textField.setEnabled(b_m$ ==True_0.$self);
    return Void_0.$self;
  }

  @Override
  public Bool_0 enable$read() {
    return textField.isEnabled()? True_0.$self: False_0.$self;
  }
  
  public JComponent getImpl() {
    return textField;
  }

  @Override
  public TextField_0 addEvents$mut(MF_2 events_m$) {
    TextEvents textEvents = new TextEvents();
    events_m$.$hash$mut(textEvents);
    if (textEvents.textChangeHandler != null) {
      textField.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
          textEvents.textChangeHandler.$hash$mut(textField.getText());
        }
                 
        @Override
        public void removeUpdate(DocumentEvent e) {
          textEvents.textChangeHandler.$hash$mut(textField.getText());
        }
      
        @Override
        public void changedUpdate(DocumentEvent e) {
          textEvents.textChangeHandler.$hash$mut(textField.getText());
        }
      });
      }
    if (textEvents.keyPressHandler != null) {
      textField.addKeyListener(new KeyListener() {
        @Override
        public void keyTyped(KeyEvent e) {
        }
                 
        @Override
        public void keyPressed(KeyEvent e) {textEvents.keyPressHandler.$hash$mut(e.getKeyChar());}
                 
        @Override
        public void keyReleased(KeyEvent e) {}
      });
    }
    if (textEvents.focusGainedHandler != null || textEvents.focusLostHandler != null) {
      textField.addFocusListener(new FocusListener() {
        @Override
        public void focusGained(FocusEvent e) {
          if (textEvents.focusGainedHandler != null) {
            textEvents.focusGainedHandler.$hash$mut();}
          }
        @Override
        public void focusLost(FocusEvent e) {
          if (textEvents.focusLostHandler != null) {
            textEvents.focusLostHandler.$hash$mut();
          }
        }});
    }
    return this;
  }

  @Override
  public Bool_0 getFocus$read() {
    return textField.isFocusOwner()? True_0.$self: False_0.$self;
  }

  @Override
  public Void_0 setFocus$mut(Bool_0 focus_m$) {
    if(focus_m$ ==True_0.$self){textField.requestFocusInWindow();}
    else {
      JPanel topPanel =(JPanel)state.topPanel;
      topPanel.requestFocusInWindow();}
    return Void_0.$self;
  }

}
