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

    public TrafficLight(char label, String path, int x, int y) {
        this.label = label;
        URL imgUrl = getClass().getClassLoader().getResource("Assets/" + path);
        if (imgUrl == null) {
            System.err.println("Failed to load resource: Assets/" + path);
            throw new IllegalArgumentException("Resource not found: " + path);
        }
        this.icon = new ImageIcon(imgUrl);
        this.x = x;
        this.y = y;
        bounds = new Rectangle(x, y, icon.getIconWidth(), icon.getIconHeight());
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
    
    public void setIcon(String imagePath) {
        this.icon = new ImageIcon(
        Objects.requireNonNull(
            getClass().getClassLoader().getResource(imagePath)
            )
        );
        this.bounds.setSize(icon.getIconWidth(), icon.getIconHeight());
    }
    
}
