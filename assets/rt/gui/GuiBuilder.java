package rt.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import base.List_1;
import base.MF_1;
import base.MF_2;
import base.Void_0;
import base.gui.AnimationLogic_0;
import base.gui.GuiBuilder_0;
import rt.ListK;
import rt.Str;

public class GuiBuilder implements GuiBuilder_0{

  private final JPanel panel;
  @SuppressWarnings("unused")
  private final LayoutManager layout;
  private final GuiBuilderState state;
//  private long currentTime;
  @SuppressWarnings("unused")
  private long lastRenderTime;
  private AnimationLogic_0 modelLogic;
  private volatile int lastProcessedPing = 0;
  private final List<Future<?>> taskList = Collections.synchronizedList(new ArrayList<Future<?>>());
  private int attemptedPings=0;
  private volatile long startTime;
  private volatile int pingIntervalMs;
  
  public GuiBuilder(LayoutManager layout, GuiBuilderState state) {
    Objects.requireNonNull(layout);
    this.panel = new JPanel();
    this.layout = layout;
    this.panel.setLayout(layout);   
    this.state = state;
  }
  
  public GuiBuilder(GuiBuilderState state) {
    this(new GridLayout(1, 0), state);
    state.topPanel = panel;
    this.panel.setFocusable(true);// may be only need to happen if user set the timer
    this.panel.requestFocusInWindow();
    this.panel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        panel.requestFocusInWindow();
      }
    });
    this.panel.addKeyListener(new java.awt.event.KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        System.out.println("Key Pressed: " + KeyEvent.getKeyText(e.getKeyCode()));
        state.keyPressedList.add("KeyPressed:"+KeyEvent.getKeyText(e.getKeyCode()));
      }

      @Override
      public void keyReleased(KeyEvent e) {
        state.keyReleasedList.add("KeyReleased:"+KeyEvent.getKeyText(e.getKeyCode()));
      }

    });

  }
  
  @Override
  public GuiBuilder_0 flex$mut(MF_2 gb_m$) {
    GuiBuilder flowBuilder = new GuiBuilder(new FlowLayout(),state);
    gb_m$.$hash$mut(flowBuilder);
    panel.add(flowBuilder.panel);
    return this;
  }

  @Override
  public GuiBuilder_0 canvas$mut(long width_m$, long height_m$, MF_2 slot_m$) {
    Canvas c =new Canvas(Math.toIntExact(width_m$),Math.toIntExact(height_m$));
    slot_m$.$hash$mut(c);
    panel.add(c.getImpl());
    return this;
  }

  @Override
  public Void_0 build$mut(Str title_m$) {
    JFrame frame = new JFrame(Str.toJavaStr(title_m$.utf8()));
    buildEvent(frame);
    return Void_0.$self;
  }
  
  private void buildEvent(JFrame frame) {
    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        frame.dispose();
        state.complete();
        super.windowClosing(e);
      }
    });
    SwingUtilities.invokeLater(()->_build(frame));
    state.waitForCompletion();
  }
  
  private void _build(JFrame frame) {
    Objects.requireNonNull(frame);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(panel);
    frame.pack();
    frame.setVisible(true);
  }

  @Override
  public GuiBuilder_0 button$mut(Str label_m$, MF_1 f_m$, MF_2 slot_m$) {
    Button b =new Button(Str.toJavaStr(label_m$.utf8()),state);
    b.addActionListener$mut(f_m$);
    slot_m$.$hash$mut(b);
    panel.add(b.getImpl());
    return this;
  }

  @Override
  public GuiBuilder_0 label$mut(Str label_m$, MF_2 slot_m$) {
    Label l = new Label(Str.toJavaStr(label_m$.utf8()));
    slot_m$.$hash$mut(l);
    panel.add(l.getImpl());
    return this;
  }

  @Override
  public GuiBuilder_0 checkBox$mut(Str label_m$, MF_1 f_m$, MF_2 slot_m$) {
    CheckBox c =new CheckBox(Str.toJavaStr(label_m$.utf8()),state);
    c.addActionListener$mut(f_m$);
    slot_m$.$hash$mut(c);
    panel.add(c.getImpl());
    return this;
  }

  @Override
  public GuiBuilder_0 comboBox$mut(MF_1 f_m$, MF_2 slot_m$, MF_2 gb_m$) {
    GuiBuilder builder = new GuiBuilder(state);
    gb_m$.$hash$mut(builder);
    Component[] components = builder.panel.getComponents();
    assert components.length > 0 : "No items added to the ComboBox";
    Object[] items = Arrays.stream(components)
        .map(component -> component instanceof JLabel ? ((JLabel) component).getText() : component).toArray();
    ComboBox comboBox = new ComboBox(items, state);
    comboBox.addActionListener$mut(f_m$);
    slot_m$.$hash$mut(comboBox);
    panel.add(comboBox.getImpl());
    return this;
  }

  @Override
  public GuiBuilder_0 passwordField$mut(Str defaultPw_m$, MF_1 f_m$, MF_2 slot_m$) {
    PasswordField p =new PasswordField(Str.toJavaStr(defaultPw_m$.utf8()),state);
    p.addActionListener$mut(f_m$);
    slot_m$.$hash$mut(p);
    panel.add(p.getImpl());
    return this;
  }

  @Override
  public GuiBuilder_0 radioButton$mut(Str label_m$, MF_1 f_m$, MF_2 slot_m$) {
    RadioButton r =new RadioButton(Str.toJavaStr(label_m$.utf8()));
    r.addActionListener$mut(f_m$);
    slot_m$.$hash$mut(r);
    panel.add(r.getImpl());
    return this;
  }

  @Override
  public GuiBuilder_0 textField$mut(Str text_m$, MF_2 events_m$, MF_2 slot_m$) {
    TextField tf =new TextField(Str.toJavaStr(text_m$.utf8()),state);
    tf.addEvents$mut(events_m$);
    slot_m$.$hash$mut(tf);
    panel.add(tf.getImpl());
    return this;
  }

  @Override
  public GuiBuilder_0 textArea$mut(Str text_m$, MF_2 events_m$, MF_2 slot_m$) {
    TextArea ta =new TextArea(Str.toJavaStr(text_m$.utf8()),state);
    ta.addEvents$mut(events_m$);
    slot_m$.$hash$mut(ta);
    panel.add(ta.getImpl());
    return this;
  }

  @Override
  public GuiBuilder_0 vseparator$mut(long x_m$, long y_m$, MF_2 slot_m$) {
    Vseparator v = new Vseparator(Math.toIntExact(x_m$),Math.toIntExact(y_m$));
    slot_m$.$hash$mut(v);
    panel.add(v.getImpl());
    return this;
  }

  @Override
  public GuiBuilder_0 hseparator$mut(long x_m$, long y_m$, MF_2 slot_m$) {
    Hseparator h = new Hseparator(Math.toIntExact(x_m$),Math.toIntExact(y_m$));
    slot_m$.$hash$mut(h);
    panel.add(h.getImpl());
    return this;
  }

  @Override
  public GuiBuilder_0 hbox$mut(MF_2 gb_m$) {
    Objects.requireNonNull(gb_m$);
    GuiBuilder boxBuilder = new GuiBuilder(new FlowLayout(),state);
    boxBuilder.panel.setLayout(new BoxLayout(boxBuilder.panel, BoxLayout.X_AXIS));
    gb_m$.$hash$mut(boxBuilder);
    panel.add(boxBuilder.panel);
    return this;
  }

  @Override
  public GuiBuilder_0 vbox$mut(MF_2 gb_m$) {
    Objects.requireNonNull(gb_m$);
    GuiBuilder boxBuilder = new GuiBuilder(new FlowLayout(), state);
    boxBuilder.panel.setLayout(new BoxLayout(boxBuilder.panel, BoxLayout.Y_AXIS));
    gb_m$.$hash$mut(boxBuilder);
    panel.add(boxBuilder.panel);
    return this;
  }

  @Override
  public GuiBuilder_0 grid$mut(long rows_m$, long columns_m$, MF_2 gb_m$) {
    assert rows_m$ > 0;
    assert columns_m$ > 0;
    GuiBuilder gridBuilder = new GuiBuilder(new GridLayout(Math.toIntExact(rows_m$), Math.toIntExact(columns_m$)),state);
    gb_m$.$hash$mut(gridBuilder);
    panel.add(gridBuilder.panel);
    return this;
  }

  public JPanel panel() {return this.panel;  }

  @Override
  public GuiBuilder_0 hsplit$mut(MF_2 gb_m$) {
    HSplitBuilder splitBuilder = new HSplitBuilder(state);
    gb_m$.$hash$mut(splitBuilder);
    JSplitPane splitPane = new JSplitPane();
    splitPane.setLeftComponent(splitBuilder.leftPanel());
    splitPane.setRightComponent(splitBuilder.rightPanel());
    this.panel.add(splitPane);
    return this;
  }

  @Override
  public GuiBuilder_0 vsplit$mut(MF_2 gb_m$) {
    VSplitBuilder splitBuilder = new VSplitBuilder(state);
    gb_m$.$hash$mut(splitBuilder);
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitPane.setTopComponent(splitBuilder.topPanel());
    splitPane.setBottomComponent(splitBuilder.bottomPanel());
    this.panel.add(splitPane);
    return this;
  }

  @Override
  public GuiBuilder_0 tabs$mut(MF_2 gb_m$) {
    TabBuilder tb = new TabBuilder(state);
    gb_m$.$hash$mut(tb);
    return tabs(tb.build(), tb);
  }
  
  private GuiBuilder tabs(Tab t, TabBuilder tb) {
    var local = new JPanel();
    local.setLayout(new BorderLayout());
    var tabPane = new JTabbedPane();
    t.getTabs().stream()
    .map((Tab.TabEntry entry) -> Map.entry(entry.title(), entry.getContent()))
    .filter(entry -> entry.getValue() instanceof GuiBuilder)
    .forEach(entry -> tabPane.addTab(entry.getKey(), ((GuiBuilder) entry.getValue()).panel));

    local.add(tabPane, BorderLayout.CENTER);
    this.panel.add(local);
    return this;
  }

  @Override
  public GuiBuilder_0 gridBag$mut(MF_2 gbb_m$, MF_2 gb_m$) {
    Objects.requireNonNull(gbb_m$, "Constraint configurator cannot be null");
    Objects.requireNonNull(gb_m$, "Component configurator cannot be null");
    var local = new JPanel();
    local.setLayout(new GridBagLayout());
    GridBagBuilder gridBagBuilder = new GridBagBuilder();
    gbb_m$.$hash$mut(gridBagBuilder);
    List<GridBagConstraints> constraints = gridBagBuilder.constriants();
    GuiBuilder componentBuilder = new GuiBuilder(new GridBagLayout(),state);
    gb_m$.$hash$mut(componentBuilder);
    Component[] components = componentBuilder.panel.getComponents();
    if (components.length != constraints.size()) {
      throw new IllegalArgumentException("Number of constraints must match the number of components");
    }
    IntStream.range(0, components.length).forEach(i -> {
      Component component = components[i];
      GridBagConstraints gbc = constraints.get(i);
      gbc.fill = GridBagConstraints.BOTH; // Ensure components expand to fill their cells
      gbc.weightx = 1.0; // Allow horizontal resizing
      gbc.weighty = 1.0; // Allow vertical resizing
      local.add(component, gbc); // Add component with constraints
    });
    this.panel.add(local);
    return this;

  }

  @Override
  public GuiBuilder_0 zone$mut(MF_2 gb_m$) {
    ZoneBuilder zb = new ZoneBuilder();
    gb_m$.$hash$mut(zb);
    return zone(zb.build());
  }
  
  private GuiBuilder zone(Zone b) {
    var local = new JPanel();
    local.setLayout(new BorderLayout());
    addZone(local, b.center(new GuiBuilder(state)), BorderLayout.CENTER);
    addZone(local, b.north(new GuiBuilder(state)), BorderLayout.NORTH);
    addZone(local, b.east(new GuiBuilder(state)), BorderLayout.EAST);
    addZone(local, b.south(new GuiBuilder(state)), BorderLayout.SOUTH);
    addZone(local, b.west(new GuiBuilder(state)), BorderLayout.WEST);
    this.panel.add(local);
    return this;
  }
  
  private void addZone(JPanel localPanel, GuiBuilder zone, String where) {
    GuiBuilder lzone = (GuiBuilder) zone;
    if (lzone.panel.getComponentCount() == 0) {
      return;
    }
    localPanel.add(lzone.panel, where);
  }

  @Override
  public GuiBuilder_0 menuBar$mut(MF_2 mb_m$, MF_2 slot_m$) {
    Objects.requireNonNull(slot_m$);
    MenuBar menuBar = new MenuBar();
    MenuBuilder menuBuilder = new MenuBuilder(menuBar, state);
    mb_m$.$hash$mut(menuBuilder);
    slot_m$.$hash$mut(menuBar);
    panel.add(menuBar.getImpl());
    return this;
  }

  @Override
  public GuiBuilder_0 animatedCanvas$mut(long width_m$, long height_m$, MF_2 slot_m$) {
    AnimatedCanvas animatedCanvas = new AnimatedCanvas();
    animatedCanvas.setPreferredSize(new Dimension(Math.toIntExact(width_m$),Math.toIntExact(height_m$)));
    slot_m$.$hash$mut(animatedCanvas);
    state.animatedCanvases.add(animatedCanvas);
    state.commitables.add(animatedCanvas);
    panel.add(animatedCanvas.getImpl());
    return this;
  }

  @Override
  public Void_0 build$mut(Str title_m$, long frameIntervalMs_m$, long dataUpdateIntervalMs_m$,
      AnimationLogic_0 logic_m$) {
    JFrame frame = new JFrame(Str.toJavaStr(title_m$.utf8()));
    this.modelLogic = logic_m$;
    startTime = System.currentTimeMillis();
    pingIntervalMs = Math.toIntExact(dataUpdateIntervalMs_m$);
    pingModel(Math.toIntExact(dataUpdateIntervalMs_m$));
    repaintAnimatedCanvas(Math.toIntExact(frameIntervalMs_m$)); 
    buildEvent(frame);
    return Void_0.$self;
  }
  
  private void repaintAnimatedCanvas(int frameIntervalMs ) {
    Timer renderTimer = new Timer(frameIntervalMs, e -> {
      calculateTime();
      panel.repaint();});
    renderTimer.start();
  }
  
  private void calculateTime() {
    long currentTime = System.currentTimeMillis();
    double elapsed= currentTime - startTime;
    double startTurnTime= (lastProcessedPing-1) * pingIntervalMs;
    double interpolatedTime=(elapsed - startTurnTime) / pingIntervalMs;
    lastRenderTime = currentTime;
    state.animatedCanvases.forEach(ac -> {
      ac.setTime(currentTime, interpolatedTime);
    });
  }
  
  private void pingModel( int pingIntervalMs) {
    state.dataUpdateExecutor.scheduleAtFixedRate(this::fixedRatePing,
        0, pingIntervalMs, TimeUnit.MILLISECONDS);
  }
  
  private void fixedRatePing() {
    attemptedPings +=1;
    if(!taskList.isEmpty()) {
      if(!taskList.get(0).isDone()) {return;}
      taskList.clear();
    }
    Future<?> task = state.submitModelTask(this::busyTask);
    taskList.add(task);
  }
  
  private void busyTask() {
      List<String> immutableCopy;
      synchronized (state.keyPressedList) {
        immutableCopy=List.copyOf(state.keyPressedList);
        state.keyPressedList.clear();
      }
      List<Str> temp = new ArrayList<Str>();
          immutableCopy.stream().forEach(s->temp.add(Str.fromJavaStr(s)));
      List_1 list = new ListK.ListImpl<Str>(temp);
      modelLogic.run$mut(lastProcessedPing, attemptedPings, System.currentTimeMillis(),list);
      SwingUtilities.invokeLater(()->{
        state.commitables.forEach(Commitable::commit);
        lastProcessedPing += 1;
        calculateTime();
      });
  }
}
