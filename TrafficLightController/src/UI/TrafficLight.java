package UI;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.io.File;
import java.net.URL;
/**
 *
 * @author evandex
 */
public class TrafficLight {
    private final char label;
    private ImageIcon icon;
    private final Rectangle bounds;
    private int x, y;
    private TrafficLight next;
    private boolean redOn, yellowOn, greenOn;
    
    public TrafficLight(char label, String path, int x, int y) {
        this.label = label;
        this.icon = loadIcon(path);
        this.x = x;
        this.y = y;
        bounds = new Rectangle(x, y, icon.getIconWidth(), icon.getIconHeight());
    }
    
    private ImageIcon loadIcon(String path) {
        URL imgUrl = getClass().getClassLoader().getResource("Assets/" + path);
        if (imgUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + path);
        }
        return new ImageIcon(imgUrl);
    }
    
    private void updateIcon() {
        String imageName;
        if (redOn && !yellowOn && !greenOn) {
            imageName = "Red_Light.png";
        } else if (!redOn && yellowOn && !greenOn) {
            imageName = "Yellow_Light.png";
        } else if (!redOn && !yellowOn && greenOn) {
            imageName = "Green_Light.png";
        } else if (redOn && yellowOn && !greenOn) {
            imageName = "Red_Yellow_Light.png";
        } else {
            imageName = "Red_Light.png"; // defaults to Red_Light
        }
        setIcon(imageName);
    }
    
    public void setState(boolean red, boolean yellow, boolean green) {
        this.redOn = red;
        this.yellowOn = yellow;
        this.greenOn = green;
        updateIcon();
    }
    
    public char getLabel() { return label; }
    public ImageIcon getIcon() { return icon; }
    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; bounds.setLocation(x, y); }
    public void setY(int y) { this.y = y; bounds.setLocation(x, y); }
    public Rectangle getBounds() { return bounds; }

    public TrafficLight getNext() { return next; }
    public void setNext(TrafficLight next) { this.next = next; }
    
    public void setIcon(String path) {
        this.icon = loadIcon(path);
        this.bounds.setSize(icon.getIconWidth(), icon.getIconHeight());
    }
    
}
