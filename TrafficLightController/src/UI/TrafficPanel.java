package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.File;
import java.net.URL;

/**
 *
 * @author evandex
 */
public class TrafficPanel extends JPanel {
    private final java.util.List<TrafficLight> lights = new ArrayList<>();
    private boolean showArrows = true;
    private boolean showLabels = true;
    private boolean initialized = false;
    private TrafficLight draggedNode;
    private int offsetX, offsetY;
    private java.util.List<Point> relativePositions = new ArrayList<>();
    
    public TrafficPanel(int width, int height, Color color) {
        setBackground(color);
        setSize(width, height);
        initNodes();
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (!initialized && getWidth() > 0 && getHeight() > 0) {
                    initNodes();
                    initialized = true;
                } else if (initialized) {
                    repositionNodes();
                    repaint();
                }
            }
        });
        
        MouseAdapter dragHandler = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                for (TrafficLight l : lights) {
                    if (l.getBounds().contains(e.getPoint())) {
                        draggedNode = l;
                        offsetX = e.getX() - l.getX();
                        offsetY = e.getY() - l.getY();
                        break;
                    }
                }
            }
            @Override public void mouseReleased(MouseEvent e) {
                draggedNode = null;
            }
            @Override public void mouseDragged(MouseEvent e) {
                if (draggedNode != null) {
                    draggedNode.setX(e.getX() - offsetX);
                    draggedNode.setY(e.getY() - offsetY);
                    repaint();
                }
            }
        };
        addMouseListener(dragHandler);
        addMouseMotionListener(dragHandler);
    }
    
    // Add this method to initialize nodes when the panel is resized
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        if (!initialized && width > 0 && height > 0) {
            initNodes();
            initialized = true;
        }
    }

    // Add this method to handle component resize
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        if (!initialized && width > 0 && height > 0) {
            initNodes();
            initialized = true;
        }
    }
    
    private void initNodes() {
        lights.clear();
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        int r = (int)(Math.min(cx, cy) * 0.2);
        char[] labels = {'A', 'B', 'C', 'D', 'E'};
        for (int i = 0; i < labels.length; i++) {
            double angle = Math.toRadians(-90 + 360 * i / labels.length);
            int x = cx + (int)(r * Math.cos(angle));
            int y = cy + (int)(r * Math.sin(angle));
            TrafficLight l = new TrafficLight(labels[i], "Red_Light.png", x, y);
            lights.add(l);
            
            int relativeX = (x - cx) * 100 / getWidth();
            int relativeY = (y - cy) * 100 / getHeight();
            relativePositions.add(new Point(relativeX, relativeY));
        }
        setNextOnLists(lights);
    }
    
    public void setShowArrows(boolean show) {
        this.showArrows = show;
        repaint();
    }
    
    public void setShowLabels(boolean show) {
        this.showLabels = show;
        repaint();
    }
    
    public void updateOrder(String sequence) {
        String[] labels = sequence.split(",");
        for (int i = 0; i < labels.length; i++) {
            labels[i] = labels[i].trim();
        }
        
        Map<Character, TrafficLight> map = new HashMap();
        for (TrafficLight l : lights) map.put(l.getLabel(), l);
        
        java.util.List<TrafficLight> reorderedNodes = new ArrayList<>();
        for (String s : labels) {
            s = s.trim();
            if (s.length() == 1 && map.containsKey(s.charAt(0))) reorderedNodes.add(map.get(s.charAt(0)));
        }
        
        for (int i = 0; i < reorderedNodes.size() - 1; i++) {
            reorderedNodes.get(i).setNext(reorderedNodes.get(i+1));
        }
        setNextOnLists(lights);
        repaint();
    }
    
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        if (showArrows)
            for (TrafficLight n : lights) {
                TrafficLight m = n.getNext();
                if (m != null)
                drawArrow(g2, 
                    n.getX() + n.getIcon().getIconWidth()/2, 
                    n.getY() + n.getIcon().getIconHeight()/2,
                    m.getX() + m.getIcon().getIconWidth()/2,
                    m.getY() + m.getIcon().getIconHeight()/2);
        }
        
        for (TrafficLight n : lights) {
            n.getIcon().paintIcon(this, g2, n.getX(), n.getY());
            if (showLabels) {
                FontMetrics font = g2.getFontMetrics();
                String label = String.valueOf(n.getLabel());
                int tx = n.getX() + (n.getIcon().getIconWidth() - font.stringWidth(label)) / 2;
                int ty = n.getY() + n.getIcon().getIconHeight() + font.getAscent();
                g2.drawString(label, tx, ty);
            }
        }
    }
    
    private void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2) {
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(x1, y1, x2, y2);
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int length = 10;
        int ax = x2 - (int)(length * Math.cos(angle - Math.PI/6));
        int ay = y2 - (int)(length * Math.sin(angle - Math.PI/6));
        int bx = x2 - (int)(length * Math.cos(angle + Math.PI/6));
        int by = y2 - (int)(length * Math.sin(angle + Math.PI/6));
        g2.drawLine(x2, y2, ax, ay);
        g2.drawLine(x2, y2, bx, by);
    }
    
    public void repositionNodes() {
        if (relativePositions == null) return;
        
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        
        for (int i = 0; i < lights.size(); i++) {
            TrafficLight light = lights.get(i);
            Point relPos = relativePositions.get(i);
            int x = cx + (relPos.x * getWidth()) / 100;
            int y = cy + (relPos.y * getHeight()) / 100;
            light.setX(x);
            light.setY(y);
        }
    }
    
    public void setNextOnLists (java.util.List<TrafficLight> lights) {
        for (int i = 0; i < lights.size(); i++) {
            lights.get(i).setNext(lights.get((i+1) % lights.size()));
        }
    }
}
