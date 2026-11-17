package salon.ui.technician;

import salon.ui.components.UiStyle;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

final class DayTimePicker extends JPanel {
    private final Wheel hours,minutes;
    private final JToggleButton am=button("AM"),pm=button("PM");
    DayTimePicker(int initial,String title,Runnable onChange){
        super(new BorderLayout(0,6));setOpaque(false);
        int t=Math.floorMod(initial,1440),h=t/60;
        hours=new Wheel(numbers(1,12),(h%12+11)%12,onChange);
        minutes=new Wheel(numbers(0,59),t%60,onChange);
        ButtonGroup group=new ButtonGroup();group.add(am);group.add(pm);
        am.setSelected(h<12);pm.setSelected(h>=12);updatePeriod();
        am.addActionListener(e->{updatePeriod();onChange.run();});
        pm.addActionListener(e->{updatePeriod();onChange.run();});
        MouseWheelListener listener=e->{
            if(!am.isEnabled()||!pm.isEnabled())return;
            JToggleButton target=e.getWheelRotation()>0?pm:am;
            if(!target.isSelected())target.doClick();e.consume();
        };
        JPanel periodButtons=new JPanel(new GridLayout(2,1,0,8));periodButtons.setOpaque(false);
        periodButtons.add(am);periodButtons.add(pm);
        JPanel period=new JPanel(new GridBagLayout());period.setOpaque(false);period.add(periodButtons);
        period.addMouseWheelListener(listener);am.addMouseWheelListener(listener);pm.addMouseWheelListener(listener);
        JPanel selectors=new JPanel(new GridLayout(1,3,5,0));selectors.setBackground(UiStyle.FIELD);
        selectors.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        selectors.add(hours);selectors.add(minutes);selectors.add(period);
        add(UiStyle.label(title,12,true,UiStyle.MUTED),BorderLayout.NORTH);add(selectors,BorderLayout.CENTER);
    }
    int value(boolean end){
        int h=(hours.index+1)%12+(pm.isSelected()?12:0),value=h*60+minutes.index;
        return end&&value==0?1440:value;
    }
    private void updatePeriod(){
        for(JToggleButton b:List.of(am,pm)){
            b.setBackground(b.isSelected()?UiStyle.BEIGE:UiStyle.FIELD);
            b.setFont(new Font("SansSerif",b.isSelected()?Font.BOLD:Font.PLAIN,14));
        }
    }
    private static JToggleButton button(String text){
        JToggleButton b=new JToggleButton(text);b.setPreferredSize(new Dimension(60,36));
        b.setForeground(UiStyle.INK);b.setFocusPainted(false);b.putClientProperty("JButton.arc",16);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;
    }
    private static List<String> numbers(int first,int last){
        List<String> result=new ArrayList<>();for(int i=first;i<=last;i++)
            result.add(String.format(java.util.Locale.ROOT,"%02d",i));return result;
    }
    private static class Wheel extends JComponent{
        final List<String> values;final Runnable change;
        int index,anchor,press;boolean dragged;
        Wheel(List<String> values,int initial,Runnable change){
            this.values=values;this.index=initial;this.change=change;
            setPreferredSize(new Dimension(65,150));setFocusable(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Scroll, glisare verticală sau click pe valoare");
            MouseAdapter mouse=new MouseAdapter(){
                @Override public void mouseWheelMoved(MouseWheelEvent e){if(!isEnabled())return;shift(e.getWheelRotation());e.consume();}
                @Override public void mousePressed(MouseEvent e){if(!isEnabled())return;requestFocusInWindow();anchor=press=e.getY();dragged=false;}
                @Override public void mouseDragged(MouseEvent e){
                    if(!isEnabled())return;if(Math.abs(e.getY()-press)>4)dragged=true;
                    int delta=(anchor-e.getY())/12;if(delta!=0){shift(delta);anchor-=delta*12;}
                }
                @Override public void mouseReleased(MouseEvent e){
                    if(!isEnabled()||dragged)return;
                    int row=Math.floorDiv(e.getY()-(getHeight()-150)/2,30)-2;if(row>=-2&&row<=2)shift(row);
                }
            };addMouseListener(mouse);addMouseMotionListener(mouse);addMouseWheelListener(mouse);
            bind(KeyEvent.VK_UP,-1);bind(KeyEvent.VK_DOWN,1);
        }
        private void bind(int key,int delta){
            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(key,0),"step"+delta);
            getActionMap().put("step"+delta,new AbstractAction(){
                @Override public void actionPerformed(ActionEvent e){if(Wheel.this.isEnabled())shift(delta);}
            });
        }
        private void shift(int delta){
            int next=Math.max(0,Math.min(values.size()-1,index+delta));if(next==index)return;
            index=next;repaint();change.run();
        }
        @Override protected void paintComponent(Graphics graphics){
            super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int top=(getHeight()-150)/2;g.setColor(UiStyle.BEIGE);g.fillRoundRect(1,top+60,getWidth()-2,30,12,12);
            for(int row=-2;row<=2;row++){
                int item=index+row;if(item<0||item>=values.size())continue;String text=values.get(item);
                g.setFont(new Font("SansSerif",row==0?Font.BOLD:Font.PLAIN,row==0?22:17));
                g.setColor(!isEnabled()?UiStyle.BORDER:row==0?UiStyle.INK:Math.abs(row)==2?new Color(180,174,165):UiStyle.MUTED);
                FontMetrics fm=g.getFontMetrics();g.drawString(text,(getWidth()-fm.stringWidth(text))/2,
                        top+(row+2)*30+(30-fm.getHeight())/2+fm.getAscent());
            }g.dispose();
        }
    }
}

