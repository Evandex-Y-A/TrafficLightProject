package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

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
    
    public TrafficPanel() {
        setBackground(Color.WHITE);
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
    
    public void updateLightState(String stateData) {
        String[] parts = stateData.split(",");
        if (parts.length < 4) return;
        
        char label = parts[0].charAt(0);
        boolean red = "1".equals(parts[1]);
        boolean yellow = "1".equals(parts[2]);
        boolean green = "1".equals(parts[3]);
        
        for (TrafficLight light : lights) {
            if (light.getLabel() == label) {
                light.setState(red, yellow, green);
                break;
            }
        }
        repaint();
    }
    
    public void updateOrder(String sequence) {
        String[] labels = sequence.split(",");
        Map<Character, TrafficLight> map = new HashMap<>();
        for (TrafficLight light : lights) {
            map.put(light.getLabel(), light);
        }
        
        java.util.List<TrafficLight> reorderedNodes = new ArrayList<>();
        for (String label : labels) {
            label = label.trim();
            if (!label.isEmpty() && map.containsKey(label.charAt(0))) {
                reorderedNodes.add(map.get(label.charAt(0)));
            }
        }
        
        // Links A -> B -> C -> D -> E -> A
        for (int i = 0; i < reorderedNodes.size(); i++) {
            TrafficLight current = reorderedNodes.get(i);
            TrafficLight next = reorderedNodes.get((i + 1) % reorderedNodes.size());
            current.setNext(next);
        }
        
        repaint();
    }
    
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        if (!initialized && width > 0 && height > 0) {
            initNodes();
            initialized = true;
        }
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        if (!initialized && width > 0 && height > 0) {
            initNodes();
            initialized = true;
        }
    }
    
    public void initNodes() {
        if (getWidth() <= 0 || getHeight() <= 0) return;
        lights.clear();
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        int r = (int)(Math.min(cx, cy) * 0.8);
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
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx*dx + dy*dy);
    
        if (length == 0) return;
    
        double ux = dx / length;
        double uy = dy / length;
        
        int radiusX = 34;
        int radiusY = 70;
    
        int startX = (int)(x1 + ux * radiusX);
        int startY = (int)(y1 + uy * radiusY);
        int endX = (int)(x2 - ux * radiusX);
        int endY = (int)(y2 - uy * radiusY);
    
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(startX, startY, endX, endY);
    
        double adjDx = endX - startX;
        double adjDy = endY - startY;
        double adjLength = Math.sqrt(adjDx*adjDx + adjDy*adjDy);
    
        if (adjLength == 0) return; 
    
        double angle = Math.atan2(adjDy, adjDx);
    
        int arrowSize = 10;
        int ax = endX - (int)(arrowSize * Math.cos(angle - Math.PI/6));
        int ay = endY - (int)(arrowSize * Math.sin(angle - Math.PI/6));
        int bx = endX - (int)(arrowSize * Math.cos(angle + Math.PI/6));
        int by = endY - (int)(arrowSize * Math.sin(angle + Math.PI/6));
    
        g2.drawLine(endX, endY, ax, ay);
        g2.drawLine(endX, endY, bx, by);
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
