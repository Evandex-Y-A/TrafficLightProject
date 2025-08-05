package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.ArrayList;
import SerialComms.ArduinoComms;
/**
 *
 * @author evandex
 */
public class TrafficFrame extends JFrame {
    private final TrafficPanel canvas;
    private ArduinoComms arduinoComms;
    private JList<String> orderList;
    private DefaultListModel<String> listModel;
    private final java.util.List<DelayPanel> delayPanels = new ArrayList<>();
    /**
     * Creates new form TrafficFrame
     */
    public TrafficFrame() {
        arduinoComms = new ArduinoComms(this);
        initComponents();
        System.out.println("TrafficContainerPanel size: " + trafficContainerPanel.getWidth() + " " + trafficContainerPanel.getHeight());
        System.out.println("Frame size: " + getSize());
        canvas = new TrafficPanel();
        trafficContainerPanel.add(canvas);
        System.out.println("canvas size: " + canvas.getSize());
        trafficContainerPanel.setVisible(true);
        setPreferredSize(new Dimension(800, 600));
        setSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        SwingUtilities.invokeLater(() -> {
            trafficContainerPanel.validate();
        if (canvas.getWidth() > 0 && canvas.getHeight() > 0)
            canvas.initNodes();
        canvas.repositionNodes();
        canvas.repaint();
        });
        initDelayPanels();
        initSerialControls();
        initOrderList();
    }

    private void initDelayPanels() {
        JPanel delayContainer = new JPanel();
        delayContainer.setLayout(new GridLayout(5, 1, 5, 5)); 
        
        for (char label = 'A'; label <= 'E'; label++) {
            DelayPanel panel = new DelayPanel(String.valueOf(label));
            delayPanels.add(panel);
            delayContainer.add(panel);
        }
        
        serialPanel.setLayout(new BorderLayout(5, 5));
        serialPanel.add(delayContainer, BorderLayout.NORTH);
        System.out.println("delayContainer size: " + delayContainer.getSize());
    }
    
    private void initSerialControls() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> portCombo = new JComboBox<>(arduinoComms.getAvailablePorts());
        JButton refreshButton = new JButton("↻");
        refreshButton.setToolTipText("Refresh ports");
        refreshButton.addActionListener(e -> refreshPorts(portCombo));
        refreshPorts(portCombo);
        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener((ActionEvent e) -> {
            String port = (String) portCombo.getSelectedItem();
            if (port != null && arduinoComms.connect(port)) {
                JOptionPane.showMessageDialog(this, "Connected to " + port);
            } else {
                JOptionPane.showMessageDialog(this, "Connection failed");
            }
        });
        
        JButton disconnectButton = new JButton("Disconnect");
        disconnectButton.addActionListener((ActionEvent e) -> {
            arduinoComms.disconnect();
        });
        
        controlPanel.add(refreshButton);
        controlPanel.add(new JLabel("Port:"));
        controlPanel.add(portCombo);
        controlPanel.add(connectButton);
        controlPanel.add(disconnectButton);
        
        filePanel.add(controlPanel);
    }
    
    private void initOrderList() {
        JPanel orderContainer = new JPanel();
        orderContainer.setLayout(new BorderLayout(5, 5));
        
        serialPanel.setLayout(new BorderLayout());
        listModel = new DefaultListModel<>();
        listModel.addElement("A");
        listModel.addElement("B");
        listModel.addElement("C");
        listModel.addElement("D");
        listModel.addElement("E");
        
        orderList = new JList<>(listModel);
        orderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderList.setDragEnabled(true);
        orderList.setDropMode(DropMode.INSERT);
        
        orderList.setTransferHandler(new TransferHandler() {
            private int sourceIndex = -1;
            
            @Override
            public int getSourceActions(JComponent c) {
                return MOVE;
            }
            
            @Override
            protected Transferable createTransferable(JComponent c) {
                sourceIndex = orderList.getSelectedIndex();
                return new StringSelection(orderList.getSelectedValue());
            }
            
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }
            
            @Override
            public boolean importData(TransferSupport support) {
                try {
                    Transferable t = support.getTransferable();
                    String data = (String) t.getTransferData(DataFlavor.stringFlavor);
                    JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
                    boolean startedAtNegativeOne = false;
                    /* 
                    I am so sorry if anyone reads this part of the code 😭
                    this is what your program looks like after you try to fix vibe code 
                    but you don't know enough about the language to write decent code
                    */
                    int index = dl.getIndex() - 1;
                    if (index == -1) {
                        index = 0;
                        startedAtNegativeOne = true;
                    }
                    if (sourceIndex == index || (sourceIndex < index && sourceIndex != 1)) listModel.remove(sourceIndex);
                    else if (sourceIndex == index - 1) {
                        listModel.remove(sourceIndex);
                    } else if (sourceIndex > index && sourceIndex != 1) {
                        listModel.remove(sourceIndex);
                        if (!startedAtNegativeOne) index++;
                    }
                    listModel.add(index, data);
                    updateLightOrder();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
        
        orderList.setFixedCellHeight(30);
        orderList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        orderList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                    boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    label.setHorizontalAlignment(SwingConstants.CENTER); // Center text
            
                if (index == -1) label.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));
                else {
                    label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY), // Bottom border
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                    ));
                }
            
                return label;
            }
        });
        
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add(new JScrollPane(orderList), BorderLayout.CENTER);
        JLabel title = new JLabel("Drag to Reorder Traffic Lights");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        title.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        listPanel.add(title, BorderLayout.NORTH);
        orderContainer.add(listPanel, BorderLayout.CENTER);
        // orderContainer.add(setOrderBtn, BorderLayout.SOUTH);

        serialPanel.add(orderContainer, BorderLayout.CENTER);
        System.out.println(orderContainer.getSize());
    }
    
    private void refreshPorts(JComboBox<String> portCombo) {
        portCombo.removeAllItems();
        String[] ports = arduinoComms.getAvailablePorts();
    
        if (ports.length == 0) {
            portCombo.addItem("No ports available");
        } else {
            for (String port : ports) {
                portCombo.addItem(port);
            }
        }
    }
    private void updateLightOrder() {
        StringBuilder sequence = new StringBuilder();
        for (int i = 0; i < listModel.size(); i++) {
            sequence.append(listModel.get(i));
            if (i < listModel.size() - 1) sequence.append(",");
        }
        canvas.updateOrder(sequence.toString());
        sendOrderToArduino(sequence.toString());
    }
    
    public void sendOrderToArduino(String order) {
        arduinoComms.sendCommand("ORDER:" + order);
    }
    
    public TrafficPanel getCanvas() {
        return canvas;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        serialPanel = new javax.swing.JPanel();
        settingsPanel = new javax.swing.JPanel();
        arrowCheckBox = new javax.swing.JCheckBox();
        labelCheckBox = new javax.swing.JCheckBox();
        filePanel = new javax.swing.JPanel();
        trafficContainerPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        serialPanel.setBackground(new java.awt.Color(100, 100, 100));
        serialPanel.setPreferredSize(new java.awt.Dimension(300, 700));
        serialPanel.setLayout(new javax.swing.BoxLayout(serialPanel, javax.swing.BoxLayout.LINE_AXIS));
        getContentPane().add(serialPanel, java.awt.BorderLayout.LINE_START);

        settingsPanel.setBackground(new java.awt.Color(100, 100, 100));
        settingsPanel.setLayout(new javax.swing.BoxLayout(settingsPanel, javax.swing.BoxLayout.PAGE_AXIS));

        arrowCheckBox.setText("Hide Arrows");
        arrowCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                arrowCheckBoxActionPerformed(evt);
            }
        });
        settingsPanel.add(arrowCheckBox);

        labelCheckBox.setText("Hide Labels");
        labelCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelCheckBoxActionPerformed(evt);
            }
        });
        settingsPanel.add(labelCheckBox);

        getContentPane().add(settingsPanel, java.awt.BorderLayout.LINE_END);

        filePanel.setBackground(new java.awt.Color(50, 50, 50));
        filePanel.setPreferredSize(new java.awt.Dimension(400, 50));
        getContentPane().add(filePanel, java.awt.BorderLayout.PAGE_START);

        trafficContainerPanel.setLayout(new java.awt.BorderLayout());
        getContentPane().add(trafficContainerPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void arrowCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_arrowCheckBoxActionPerformed
        if (arrowCheckBox.isSelected()) canvas.setShowArrows(false);
        else canvas.setShowArrows(true);
    }//GEN-LAST:event_arrowCheckBoxActionPerformed

    private void labelCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_labelCheckBoxActionPerformed
        if (labelCheckBox.isSelected()) canvas.setShowLabels(false);
        else canvas.setShowLabels(true);
    }//GEN-LAST:event_labelCheckBoxActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TrafficFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TrafficFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TrafficFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TrafficFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name"));
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TrafficFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox arrowCheckBox;
    private javax.swing.JPanel filePanel;
    private javax.swing.JCheckBox labelCheckBox;
    private javax.swing.JPanel serialPanel;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JPanel trafficContainerPanel;
    // End of variables declaration//GEN-END:variables
}
