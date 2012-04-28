package gis_project;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PM25GUI {

    // GUI parameters
    private int ctrlWidth = 300;
    private int ctrlLWidth = 195;
    private int ctrlLHeight = 150;
    private int mapWidth = 770;
    private int mapHeight = 420;
    // scaling lat-long to x-y map pixels
    private int xOffset = 1535;
    private int yOffset = -50;
    private int xyScale = 12;
    // input data
    private int timeDomain = 0;
    private int inputSize;
    private DataSet inputData;
    private DataSet inputDataScaled;
    private boolean inputProcessed = false;
    private KDTree inputTree;
    private boolean correctDomain;
    // state borders
    private Coordinate[] stateBorders;
    private int numBorderCoords = 0;
    private boolean bordersProcessed = false;
    // location data
    private int numLocs;
    private boolean locationsProcessed = false;
    private boolean showObsSites = true;
    // output data
    private static int outputSize;
    private int nnn = 0; // IDW number nearest neighbors
    private double exp = 0; // IDW exponent
    private DataSet outputData;
    private DataSet outputDataScaled;
    private static double percentComplete1;
    private boolean outputComputed = false;
    // raster data
    private DataSet rasterData;
    private DataSet rasterDataScaled;
    private int rasterSetSize;
    boolean rasterComputed = false;
    private double rasterCellSizeScaled = 0;
    // animation
    private boolean animate = false;
    private boolean animationComplete = false;
    private int animationDay = 0;
    private double percentComplete2;
    // validation
    private boolean validationComplete = false;
    private double[][] va;
    //
    // GUI components
    //
    private JFrame mainFrame;
    private JPanel ctrlPanelBox;
    private JPanel ctrlPanel;
    private JPanel visPanel;
    private JPanel ctrlPanelBoxL;
    private JPanel ctrlPanelL;
    private JPanel timeSelectPanel;
    private JComboBox<String> domainDropdown;
    private JPanel inputSelectPanel;
    private JFileChooser inputFileChooser;
    private JPanel inputFileButtonBox;
    private JButton inputFileButton;
    private File inputFile;
    private JPanel statusPanel1;
    private JLabel statusLabel1;
    private JPanel processInputPanel;
    private JPanel processButtonBox;
    private JButton processButton;
    private JPanel statusPanel2;
    private JLabel statusLabel2;
    private JPanel locSelectPanel;
    private JFileChooser locFileChooser;
    private JPanel locFileButtonBox;
    private JButton locFileButton;
    private File locFile;
    private JPanel statusPanel3;
    private JLabel statusLabel3;
    private JPanel outputSelectPanel;
    private JFileChooser outputFileChooser;
    private JPanel outputFileButtonBox;
    private JButton outputFileButton;
    private File outputFile;
    private JPanel statusPanel4;
    private JLabel statusLabel4;
    private JPanel nnnSelectPanel;
    private JComboBox<Integer> nnnDropdown;
    private JPanel expSelectPanel;
    private JComboBox<Double> expDropdown;
    private JPanel computeOutputPanel;
    private JPanel computeButtonBox;
    private JButton computeButton;
    private JPanel statusPanel5;
    private JLabel statusLabel5;
    private JPanel statusPanel6;
    private static JLabel statusLabel6;
    private JPanel aniPanelGrid;
    private JPanel aniPanelGridTop;
    private JLabel aniLabelDesc;
    private JComboBox<String> rasterDropdown;
    private JButton aniButtonBox;
    private JButton aniButton;
    private JPanel aniPanelStatusBox;
    private JLabel aniLabelStatus;
    private JPanel validatePanelGrid;
    private JLabel validateLabelDesc;
    private JButton validateButtonBox;
    private JButton validateButton;
    private JPanel validatePanelStatusBox;
    private JLabel validateLabelStatus;
    private JPanel measurePanelGrid;
    private JLabel measureLabelDesc;
    private JButton measureButtonBox;
    private JButton measureButton;
    private JPanel measurePanelStatusBox;
    private JLabel measureLabelStatus;
    private JPanel customQueryPanelGrid;
    private JLabel customQueryLabelDesc;
    private JButton customQueryButtonBox;
    private JButton customQueryButton;
    private JPanel customQueryPanelStatusBox;
    private JLabel customQueryLabelStatus;
    private MapPanel mapPanel = new MapPanel();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                PM25GUI myGui = new PM25GUI();
                myGui.createAndShow();
            }
        });
    }
    
    public static void percentCompleteIncrement1() {
    percentComplete1 += (1.0 / outputSize) * 100.0;
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            statusLabel6.setText("[" + String.format("%2.1f", percentComplete1) + "% complete]");
        }
    });
}

    public void createAndShow() {
        mainFrame = new JFrame("PM25 Interpolation and Visualization");

        ctrlPanelBox = new JPanel();
        ctrlPanel = new JPanel();
        visPanel = new JPanel();
        ctrlPanelBoxL = new JPanel();
        ctrlPanelL = new JPanel();

        timeSelectPanel = new JPanel();
        inputSelectPanel = new JPanel();
        statusPanel1 = new JPanel();
        processInputPanel = new JPanel();
        statusPanel2 = new JPanel();
        locSelectPanel = new JPanel();
        statusPanel3 = new JPanel();
        outputSelectPanel = new JPanel();
        statusPanel4 = new JPanel();
        nnnSelectPanel = new JPanel();
        expSelectPanel = new JPanel();
        computeOutputPanel = new JPanel();
        statusPanel5 = new JPanel();
        statusPanel6 = new JPanel();

        aniPanelGrid = new JPanel();
        aniPanelGridTop = new JPanel();
        aniLabelDesc = new JLabel();
        aniButtonBox = new JButton();
        aniButton = new JButton();
        aniPanelStatusBox = new JPanel();
        aniLabelStatus = new JLabel();

        validatePanelGrid = new JPanel();
        validateLabelDesc = new JLabel();
        validateButtonBox = new JButton();
        validateButton = new JButton();
        validatePanelStatusBox = new JPanel();
        validateLabelStatus = new JLabel();

        measurePanelGrid = new JPanel();
        measureLabelDesc = new JLabel();
        measureButtonBox = new JButton();
        measureButton = new JButton();
        measurePanelStatusBox = new JPanel();
        measureLabelStatus = new JLabel();

        customQueryPanelGrid = new JPanel();
        customQueryLabelDesc = new JLabel();
        customQueryButtonBox = new JButton();
        customQueryButton = new JButton();
        customQueryPanelStatusBox = new JPanel();
        customQueryLabelStatus = new JLabel();

        timeSelectPanel.setLayout(new GridLayout(2, 1));
        timeSelectPanel.setMaximumSize(new Dimension(ctrlWidth, 50));
        timeSelectPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        timeSelectPanel.setBackground(Color.white);
        String[] domainOptions = {"", "(year)", "(year, month)", "(year, quarter)", "(year, month, day)"};
        domainDropdown = new JComboBox<String>(domainOptions);
        domainDropdown.setSelectedIndex(0);
        domainDropdown.addActionListener(new TimeDomListener());
        timeSelectPanel.add(new JLabel("Select time domain for input data:"));
        timeSelectPanel.add(domainDropdown);

        inputSelectPanel.setLayout(new GridLayout(1, 1));
        inputSelectPanel.setMaximumSize(new Dimension(ctrlWidth, 35));
        inputSelectPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        inputSelectPanel.setBackground(Color.white);
        inputFileChooser = new JFileChooser();
        inputFileButtonBox = new JPanel();
        inputFileButtonBox.setLayout(new BoxLayout(inputFileButtonBox, BoxLayout.PAGE_AXIS));
        inputFileButtonBox.setBackground(Color.white);
        inputFileButton = new JButton("Browse");
        inputFileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputFileButton.addActionListener(new InputFileListener());
        inputFileButtonBox.add(inputFileButton);
        inputSelectPanel.add(new JLabel("Select input file:"));
        inputSelectPanel.add(inputFileButtonBox);

        statusPanel1.setLayout(new GridLayout(1, 1));
        statusPanel1.setMaximumSize(new Dimension(ctrlWidth, 30));
        statusPanel1.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        statusPanel1.setBackground(Color.lightGray);
        statusLabel1 = new JLabel("Select a file.");
        statusPanel1.add(statusLabel1);

        processInputPanel.setLayout(new GridLayout(1, 1));
        processInputPanel.setMaximumSize(new Dimension(ctrlWidth, 35));
        processInputPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        processInputPanel.setBackground(Color.white);
        processButtonBox = new JPanel();
        processButtonBox.setLayout(new BoxLayout(processButtonBox, BoxLayout.PAGE_AXIS));
        processButtonBox.setBackground(Color.white);
        processButton = new JButton("Process");
        processButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        processButton.addActionListener(new ProcessInputListener());
        processButtonBox.add(processButton);
        processInputPanel.add(new JLabel("Process input:"));
        processInputPanel.add(processButtonBox);

        statusPanel2.setLayout(new GridLayout(1, 1));
        statusPanel2.setMaximumSize(new Dimension(ctrlWidth, 30));
        statusPanel2.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        statusPanel2.setBackground(Color.lightGray);
        statusLabel2 = new JLabel("Input not processed.");
        statusPanel2.add(statusLabel2);

        locSelectPanel.setLayout(new GridLayout(1, 1));
        locSelectPanel.setMaximumSize(new Dimension(ctrlWidth, 35));
        locSelectPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        locSelectPanel.setBackground(Color.white);
        locFileChooser = new JFileChooser();
        locFileButtonBox = new JPanel();
        locFileButtonBox.setLayout(new BoxLayout(locFileButtonBox, BoxLayout.PAGE_AXIS));
        locFileButtonBox.setBackground(Color.white);
        locFileButton = new JButton("Browse");
        locFileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        locFileButton.addActionListener(new LocFileListener());
        locFileButtonBox.add(locFileButton);
        locSelectPanel.add(new JLabel("Select location set:"));
        locSelectPanel.add(locFileButtonBox);

        statusPanel3.setLayout(new GridLayout(1, 1));
        statusPanel3.setMaximumSize(new Dimension(ctrlWidth, 30));
        statusPanel3.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        statusPanel3.setBackground(Color.lightGray);
        statusLabel3 = new JLabel("Select a file.");
        statusPanel3.add(statusLabel3);

        outputSelectPanel.setLayout(new GridLayout(1, 1));
        outputSelectPanel.setMaximumSize(new Dimension(ctrlWidth, 35));
        outputSelectPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        outputSelectPanel.setBackground(Color.white);
        outputFileChooser = new JFileChooser();
        outputFileButtonBox = new JPanel();
        outputFileButtonBox.setLayout(new BoxLayout(outputFileButtonBox, BoxLayout.PAGE_AXIS));
        outputFileButtonBox.setBackground(Color.white);
        outputFileButton = new JButton("Browse");
        outputFileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        outputFileButton.addActionListener(new OutputFileListener());
        outputFileButtonBox.add(outputFileButton);
        outputSelectPanel.add(new JLabel("Select output file:"));
        outputSelectPanel.add(outputFileButtonBox);

        statusPanel4.setLayout(new GridLayout(1, 1));
        statusPanel4.setMaximumSize(new Dimension(ctrlWidth, 30));
        statusPanel4.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        statusPanel4.setBackground(Color.lightGray);
        statusLabel4 = new JLabel("Select a file.");
        statusPanel4.add(statusLabel4);

        nnnSelectPanel.setLayout(new GridLayout(2, 1));
        nnnSelectPanel.setMaximumSize(new Dimension(ctrlWidth, 50));
        nnnSelectPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        nnnSelectPanel.setBackground(Color.white);
        Integer[] nnnOptions = {null, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
        nnnDropdown = new JComboBox<Integer>(nnnOptions);
        nnnDropdown.setSelectedIndex(0);
        nnnDropdown.addActionListener(new NnnListener());
        nnnSelectPanel.add(new JLabel("Number of nearest neighbors for IDW:"));
        nnnSelectPanel.add(nnnDropdown);

        expSelectPanel.setLayout(new GridLayout(2, 1));
        expSelectPanel.setMaximumSize(new Dimension(ctrlWidth, 50));
        expSelectPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        expSelectPanel.setBackground(Color.white);
        Double[] expOptions = {null, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0};
        expDropdown = new JComboBox<Double>(expOptions);
        expDropdown.setSelectedIndex(0);
        expDropdown.addActionListener(new ExpListener());
        expSelectPanel.add(new JLabel("Exponent for IDW:"));
        expSelectPanel.add(expDropdown);

        computeOutputPanel.setLayout(new GridLayout(1, 1));
        computeOutputPanel.setMaximumSize(new Dimension(ctrlWidth, 35));
        computeOutputPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        computeOutputPanel.setBackground(Color.white);
        computeButtonBox = new JPanel();
        computeButtonBox.setLayout(new BoxLayout(computeButtonBox, BoxLayout.PAGE_AXIS));
        computeButtonBox.setBackground(Color.white);
        computeButton = new JButton("Compute");
        computeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        computeButton.addActionListener(new ComputeOutputListener());
        computeButtonBox.add(computeButton);
        computeOutputPanel.add(new JLabel("Compute output:"));
        computeOutputPanel.add(computeButtonBox);

        statusPanel5.setLayout(new GridLayout(1, 1));
        statusPanel5.setMaximumSize(new Dimension(ctrlWidth, 25));
        statusPanel5.setBorder(BorderFactory.createEmptyBorder(4, 10, 0, 10));
        statusPanel5.setBackground(Color.lightGray);
        statusLabel5 = new JLabel("Output not computed.");
        statusPanel5.add(statusLabel5);

        statusPanel6.setLayout(new GridLayout(1, 1));
        statusPanel6.setMaximumSize(new Dimension(ctrlWidth, 25));
        statusPanel6.setBorder(BorderFactory.createEmptyBorder(0, 10, 4, 10));
        statusPanel6.setBackground(Color.lightGray);
        statusLabel6 = new JLabel("");
        statusPanel6.add(statusLabel6);

        ctrlPanel.setLayout(new BoxLayout(ctrlPanel, BoxLayout.PAGE_AXIS));
        ctrlPanel.setBackground(Color.white);
        ctrlPanel.setMaximumSize(new Dimension(ctrlWidth, 9999));
        ctrlPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        ctrlPanel.add(timeSelectPanel);
        ctrlPanel.add(inputSelectPanel);
        ctrlPanel.add(statusPanel1);
        ctrlPanel.add(processInputPanel);
        ctrlPanel.add(statusPanel2);
        ctrlPanel.add(locSelectPanel);
        ctrlPanel.add(statusPanel3);
        ctrlPanel.add(outputSelectPanel);
        ctrlPanel.add(statusPanel4);
        ctrlPanel.add(nnnSelectPanel);
        ctrlPanel.add(expSelectPanel);
        ctrlPanel.add(computeOutputPanel);
        ctrlPanel.add(statusPanel5);
        ctrlPanel.add(statusPanel6);

        ctrlPanelBox.setLayout(new BoxLayout(ctrlPanelBox, BoxLayout.PAGE_AXIS));
        ctrlPanelBox.setBackground(Color.white);
        ctrlPanelBox.setMaximumSize(new Dimension(ctrlWidth, 9999));
        ctrlPanelBox.add(ctrlPanel);

        aniPanelGrid.setLayout(new GridLayout(3, 1));
        aniPanelGrid.setMaximumSize(new Dimension(ctrlLWidth, 9999));
        aniPanelGrid.setBackground(Color.white);
        aniPanelGrid.setBorder(BorderFactory.createLineBorder(Color.black));
        aniPanelGridTop.setLayout(new GridLayout(2, 1));
        aniPanelGridTop.setBackground(Color.white);
        aniPanelGridTop.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        aniLabelDesc.setText("Raster set animation:");
        aniLabelDesc.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        String[] rasterOptions = {"select raster cell size", "4px/23mi (very slow build)", "6px/35mi (slow build)", "10px/58mi", "20px/115mi", "30px/173mi"};
        rasterDropdown = new JComboBox<String>(rasterOptions);
        rasterDropdown.addActionListener(new RasterCellListener());
        aniButtonBox.setLayout(new BoxLayout(aniButtonBox, BoxLayout.PAGE_AXIS));
        aniButtonBox.setBackground(Color.white);
        aniButtonBox.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
        aniButton = new JButton("Build");
        aniButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        aniButton.addActionListener(new AniListener());
        aniButtonBox.add(aniButton);
        aniPanelStatusBox.setLayout(new BoxLayout(aniPanelStatusBox, BoxLayout.PAGE_AXIS));
        aniPanelStatusBox.setBackground(Color.white);
        aniLabelStatus.setText("");
        aniLabelStatus.setBorder(BorderFactory.createEmptyBorder(4, 10, 0, 0));
        aniPanelStatusBox.add(aniLabelStatus);
        aniPanelGrid.add(aniPanelGridTop);
        aniPanelGridTop.add(aniLabelDesc);
        aniPanelGridTop.add(rasterDropdown);
        aniPanelGrid.add(aniButtonBox);
        aniPanelGrid.add(aniPanelStatusBox);

        validatePanelGrid.setLayout(new GridLayout(3, 1));
        validatePanelGrid.setMaximumSize(new Dimension(ctrlLWidth, 9999));
        validatePanelGrid.setBackground(Color.white);
        validatePanelGrid.setBorder(BorderFactory.createLineBorder(Color.black));
        validateLabelDesc.setText("Validate IDW using LOOCV:");
        validateLabelDesc.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        validateButtonBox.setLayout(new BoxLayout(validateButtonBox, BoxLayout.PAGE_AXIS));
        validateButtonBox.setBackground(Color.white);
        validateButtonBox.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
        validateButton = new JButton("Validate");
        validateButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        validateButton.addActionListener(new ValidateListener());
        validateButtonBox.add(validateButton);
        validatePanelStatusBox.setLayout(new BoxLayout(validatePanelStatusBox, BoxLayout.PAGE_AXIS));
        validatePanelStatusBox.setBackground(Color.white);
        validateLabelStatus.setText("");
        validateLabelStatus.setBorder(BorderFactory.createEmptyBorder(4, 10, 0, 0));
        validatePanelStatusBox.add(validateLabelStatus);
        validatePanelGrid.add(validateLabelDesc);
        validatePanelGrid.add(validateButtonBox);
        validatePanelGrid.add(validatePanelStatusBox);

        measurePanelGrid.setLayout(new GridLayout(3, 1));
        measurePanelGrid.setMaximumSize(new Dimension(ctrlLWidth, 9999));
        measurePanelGrid.setBackground(Color.white);
        measurePanelGrid.setBorder(BorderFactory.createLineBorder(Color.black));
        measureLabelDesc.setText("Measure error:");
        measureLabelDesc.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        measureButtonBox.setLayout(new BoxLayout(measureButtonBox, BoxLayout.PAGE_AXIS));
        measureButtonBox.setBackground(Color.white);
        measureButtonBox.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
        measureButton = new JButton("Measure");
        measureButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        measureButton.addActionListener(new MeasureListener());
        measureButtonBox.add(measureButton);
        measurePanelStatusBox.setLayout(new BoxLayout(measurePanelStatusBox, BoxLayout.PAGE_AXIS));
        measurePanelStatusBox.setBackground(Color.white);
        measureLabelStatus.setText("");
        measureLabelStatus.setBorder(BorderFactory.createEmptyBorder(4, 10, 0, 0));
        measurePanelStatusBox.add(measureLabelStatus);
        measurePanelGrid.add(measureLabelDesc);
        measurePanelGrid.add(measureButtonBox);
        measurePanelGrid.add(measurePanelStatusBox);

        customQueryPanelGrid.setLayout(new GridLayout(3, 1));
        customQueryPanelGrid.setMaximumSize(new Dimension(ctrlLWidth, 9999));
        customQueryPanelGrid.setBackground(Color.white);
        customQueryPanelGrid.setBorder(BorderFactory.createLineBorder(Color.black));
        customQueryLabelDesc.setText("<html>SELECT COUNT(*) FROM input<br/>WHERE year = 2009<br/>AND measurement > 20</html>");
        customQueryLabelDesc.setBorder(BorderFactory.createEmptyBorder(3, 10, 0, 0));
        customQueryButtonBox.setLayout(new BoxLayout(customQueryButtonBox, BoxLayout.PAGE_AXIS));
        customQueryButtonBox.setBackground(Color.white);
        customQueryButtonBox.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
        customQueryButton = new JButton("Execute custom query");
        customQueryButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        customQueryButton.addActionListener(new CustomQueryListener());
        customQueryButtonBox.add(customQueryButton);
        customQueryPanelStatusBox.setLayout(new BoxLayout(customQueryPanelStatusBox, BoxLayout.PAGE_AXIS));
        customQueryPanelStatusBox.setBackground(Color.white);
        customQueryLabelStatus.setText("");
        customQueryLabelStatus.setBorder(BorderFactory.createEmptyBorder(4, 10, 0, 0));
        customQueryPanelStatusBox.add(customQueryLabelStatus);
        customQueryPanelGrid.add(customQueryLabelDesc);
        customQueryPanelGrid.add(customQueryButtonBox);
        customQueryPanelGrid.add(customQueryPanelStatusBox);

        ctrlPanelL.setLayout(new BoxLayout(ctrlPanelL, BoxLayout.LINE_AXIS));
        ctrlPanelL.setPreferredSize(new Dimension(mapWidth, ctrlLHeight));
        ctrlPanelL.setBackground(Color.white);
        ctrlPanelL.add(aniPanelGrid);
        ctrlPanelL.add(validatePanelGrid);
        ctrlPanelL.add(measurePanelGrid);
        ctrlPanelL.add(customQueryPanelGrid);

        ctrlPanelBoxL.setLayout(new BoxLayout(ctrlPanelBoxL, BoxLayout.LINE_AXIS));
        ctrlPanelBoxL.setBackground(Color.white);
        ctrlPanelBoxL.add(ctrlPanelL);

        mapPanel = new MapPanel();
        mapPanel.setLayout(new BoxLayout(mapPanel, BoxLayout.PAGE_AXIS));
        mapPanel.setPreferredSize(new Dimension(mapWidth, mapHeight));
        mapPanel.setBackground(new Color(0xf7, 0xf7, 0xf7));
        mapPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        readBorders();

        visPanel.setLayout(new BorderLayout());
        visPanel.add(BorderLayout.CENTER, mapPanel);
        visPanel.add(BorderLayout.SOUTH, ctrlPanelBoxL);

        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.getContentPane().add(BorderLayout.WEST, ctrlPanelBox);
        mainFrame.getContentPane().add(BorderLayout.CENTER, visPanel);
        mainFrame.setLocation(300, 150);
        mainFrame.pack();
        mainFrame.setResizable(false);
        mainFrame.setVisible(true);
    }

    class TimeDomListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JComboBox cb = (JComboBox) e.getSource();
            String selected = (String) cb.getSelectedItem();
            if (selected.equals("(year)")) {
                timeDomain = 1;
            } else if (selected.equals("(year, month)")) {
                timeDomain = 2;
            } else if (selected.equals("(year, quarter)")) {
                timeDomain = 3;
            } else if (selected.equals("(year, month, day)")) {
                timeDomain = 4;
            }
        }
    }

    class InputFileListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int returnVal = inputFileChooser.showOpenDialog(inputSelectPanel);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                inputFile = inputFileChooser.getSelectedFile();
                statusLabel1.setText("Input: " + inputFile.getName());
                statusPanel1.setBackground(Color.green);
            }
        }
    }

    class ProcessInputListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (inputFile != null && timeDomain != 0) {
                processInput();
            } else if (inputFile == null) {
                statusLabel2.setText("Select an input file.");
                statusPanel2.setBackground(Color.yellow);
                mapPanel.repaint();
            } else if (timeDomain == 0) {
                statusLabel2.setText("Select a time domain.");
                statusPanel2.setBackground(Color.yellow);
                mapPanel.repaint();
            }
        }
    }

    class LocFileListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int returnVal = locFileChooser.showOpenDialog(locSelectPanel);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                locFile = locFileChooser.getSelectedFile();
                statusLabel3.setText("Locations: " + locFile.getName());
                statusPanel3.setBackground(Color.green);
            }
        }
    }

    class OutputFileListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int returnVal = outputFileChooser.showOpenDialog(outputSelectPanel);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                outputFile = outputFileChooser.getSelectedFile();
                statusLabel4.setText("Output: " + outputFile.getName());
                statusPanel4.setBackground(Color.green);
            }
        }
    }

    class NnnListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JComboBox cb = (JComboBox) e.getSource();
            if (cb.getSelectedItem() != null) {
                nnn = (Integer) cb.getSelectedItem();
            }
        }
    }

    class ExpListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JComboBox cb = (JComboBox) e.getSource();
            if (cb.getSelectedItem() != null) {
                exp = (Double) cb.getSelectedItem();
            }
        }
    }

    class ComputeOutputListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (outputFile != null && nnn != 0 && exp != 0 && locFile != null && inputProcessed) {
                computeOutput();
            } else if (outputFile == null) {
                statusPanel5.setBackground(Color.yellow);
                statusLabel5.setText("Select output file.");
                statusPanel6.setBackground(Color.yellow);
            } else if (nnn == 0) {
                statusPanel5.setBackground(Color.yellow);
                statusLabel5.setText("Select number of nearest neighbors.");
                statusPanel6.setBackground(Color.yellow);
            } else if (exp == 0) {
                statusPanel5.setBackground(Color.yellow);
                statusLabel5.setText("Select exponent.");
                statusPanel6.setBackground(Color.yellow);
            } else if (locFile == null) {
                statusPanel5.setBackground(Color.yellow);
                statusLabel5.setText("Select location file.");
                statusPanel6.setBackground(Color.yellow);
            } else if (!inputProcessed) {
                statusPanel5.setBackground(Color.yellow);
                statusLabel5.setText("Input not yet processed.");
                statusPanel6.setBackground(Color.yellow);
            }
        }
    }

    class RasterCellListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JComboBox cb = (JComboBox) e.getSource();
            String selected = (String) cb.getSelectedItem();
            if (selected.equals("4px/23mi (very slow build)")) {
                rasterCellSizeScaled = 4;
            } else if (selected.equals("6px/35mi (slow build)")) {
                rasterCellSizeScaled = 6;
            } else if (selected.equals("10px/58mi")) {
                rasterCellSizeScaled = 10;
            } else if (selected.equals("20px/115mi")) {
                rasterCellSizeScaled = 20;
            } else if (selected.equals("30px/173mi")) {
                rasterCellSizeScaled = 30;
            }
        }
    }

    class AniListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (timeDomain == 1 || timeDomain == 2 || timeDomain == 3) {
                aniPanelStatusBox.setBackground(Color.yellow);
                aniLabelStatus.setText("<html>Animation only implemented<br/>for (year, month, day) domain.</html>");
            } else {
                if (rasterCellSizeScaled != 0 && inputProcessed && nnn != 0 && exp != 0 && !rasterComputed) {
                    aniLabelDesc.setText("Cell size locked until restart.");
                    rasterDropdown.setEnabled(false);
                    aniButton.setEnabled(false);
                    buildRasterSet();
                    computeRaster();
                } else if (rasterCellSizeScaled == 0) {
                    aniPanelStatusBox.setBackground(Color.yellow);
                    aniLabelStatus.setText("Select a cell size.");
                } else if (exp == 0) {
                    aniPanelStatusBox.setBackground(Color.yellow);
                    aniLabelStatus.setText("Select exponent.");
                } else if (nnn == 0) {
                    aniPanelStatusBox.setBackground(Color.yellow);
                    aniLabelStatus.setText("<html>Select number of nearest neighbors.</html>");
                } else if (!inputProcessed) {
                    aniPanelStatusBox.setBackground(Color.yellow);
                    aniLabelStatus.setText("Input not yet processed.");
                }
                if (rasterComputed && animationDay == 0 && !animationComplete) {
                    animate = true;
                    aniButton.setEnabled(false);
                    animate();
                }
                if (animationComplete && animationDay == (365 * inputData.numYears() - 1)) {
                    animate = true;
                    animationComplete = false;
                    animationDay = 0;
                    aniButton.setEnabled(false);
                    animate();
                }
            }
        }
    }

    class ValidateListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (inputProcessed) {
                validate();
            } else {
                validatePanelStatusBox.setBackground(Color.yellow);
                validateLabelStatus.setText("Input not yet processed.");
            }
        }
    }

    class MeasureListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (inputProcessed && validationComplete) {
                measure();
            } else {
                measurePanelStatusBox.setBackground(Color.yellow);
                measureLabelStatus.setText("Validation not complete.");
            }
        }
    }

    class CustomQueryListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (inputProcessed) {
                int queryCount = 0;
                for (int i = 0; i < inputSize; i++) {
                    DataPoint dp = inputData.get(i);
                    if (dp.getTime()[0] == 2009 && dp.getMeasurement() > 20) {
                        queryCount++;
                    }
                }
                customQueryPanelStatusBox.setBackground(Color.green);
                customQueryLabelStatus.setText("Result: " + queryCount);
            } else {
                customQueryPanelStatusBox.setBackground(Color.yellow);
                customQueryLabelStatus.setText("Input not yet processed.");
            }
        }
    }

    public void readBorders() {
        final File coordsFile = new File("st99_d00.dat");

        final SwingWorker<Coordinate[], Void> readBordersWorker = new SwingWorker<Coordinate[], Void>() {

            @Override
            public Coordinate[] doInBackground() {
                int i = 0;
                boolean isNewSet = true;
                boolean ignoreSet = false;
                double x;
                double y;

                BufferedReader br = null;
                String line;
                String[] lineSplit;
                Coordinate[] retCoords = new Coordinate[numBorderCoords];

                try {
                    br = new BufferedReader(new FileReader(coordsFile));
                    while ((line = br.readLine()) != null) {
                        lineSplit = line.split("\\s+");
                        if (isNewSet && !lineSplit[0].equals("END")) { // repeated END near last line
                            int cat = Integer.parseInt(lineSplit[1]);
                            if ((cat >= 1 && cat <= 81) || (cat == 220) || (cat >= 223 && cat <= 228) || (cat >= 230 && cat <= 231) || (cat == 233) || (cat >= 242 && cat <= 273) || (cat == -99999)) {
                                ignoreSet = true; // ignore Alaska, Hawaii, Puerto Rico, and mystery state
                            } else {
                                ignoreSet = false;
                                x = Double.parseDouble(lineSplit[2]);
                                y = Double.parseDouble(lineSplit[3]);
                                retCoords[i] = new Coordinate(x, y);
                                i++;
                            }
                            isNewSet = false;
                        } else {
                            if (lineSplit[0].equals("END")) {
                                isNewSet = true;
                            } else {
                                if (!ignoreSet) {
                                    x = Double.parseDouble(lineSplit[1]);
                                    y = Double.parseDouble(lineSplit[2]);
                                    retCoords[i] = new Coordinate(x, y);
                                    i++;
                                }
                            }
                        }
                    }
                    // re-scale retCoords for panel display
                    for (int j = 0; j < retCoords.length; j++) {
                        retCoords[j].setX(retCoords[j].getX() * xyScale);
                        retCoords[j].setY(retCoords[j].getY() * xyScale);
                        retCoords[j].setX(retCoords[j].getX() + xOffset);
                        retCoords[j].setY(retCoords[j].getY() + yOffset);
                    }
                    br.close();
                } catch (Exception e) {
                    System.err.println("Error:" + e.getMessage());
                }
                return retCoords;
            }

            @Override
            protected void done() {
                try {
                    stateBorders = get();
                    bordersProcessed = true;
                    mapPanel.repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        SwingWorker<Integer, Void> countCoordsWorker = new SwingWorker<Integer, Void>() {

            @Override
            public Integer doInBackground() {
                int count = 0;
                boolean isNewSet = true;
                boolean ignoreSet = false;

                BufferedReader br = null;
                String line;
                String[] lineSplit;

                try {
                    br = new BufferedReader(new FileReader(coordsFile));
                    while ((line = br.readLine()) != null) {
                        lineSplit = line.split("\\s+");
                        if (isNewSet && !lineSplit[0].equals("END")) { // repeated END near last line
                            int cat = Integer.parseInt(lineSplit[1]);
                            if ((cat >= 1 && cat <= 81) || (cat == 220) || (cat >= 223 && cat <= 228) || (cat >= 230 && cat <= 231) || (cat == 233) || (cat >= 242 && cat <= 273) || (cat == -99999)) {
                                ignoreSet = true; // ignore Alaska, Hawaii, Puerto Rico, and mystery state
                            } else {
                                ignoreSet = false;
                                count++;
                            }
                            isNewSet = false;
                        } else {
                            if (lineSplit[0].equals("END")) {
                                isNewSet = true;
                            } else {
                                if (!ignoreSet) {
                                    count++;
                                }
                            }
                        }
                    }
                    br.close();
                } catch (Exception e) {
                    System.err.println("Error:" + e.getMessage());
                }
                return count;
            }

            @Override
            protected void done() {
                try {
                    numBorderCoords = get();
                    readBordersWorker.execute();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        countCoordsWorker.execute();
    }

    public void processInput() {
        final SwingWorker<DataSet, Void> processInputWorker = new SwingWorker<DataSet, Void>() {

            @Override
            public DataSet doInBackground() {
                BufferedReader br = null;
                String line;
                String[] lineSplit;
                DataSet retData = new DataSet(inputSize, timeDomain);
                inputDataScaled = new DataSet(inputSize, timeDomain);
                try {
                    br = new BufferedReader(new FileReader(inputFile));
                    br.readLine(); // throw away header
                    for (int i = 0; i < inputSize; i++) {
                        line = br.readLine();
                        lineSplit = line.split("\\s+");

                        // on first pass through loop, check time domain chosen was correct
                        if (i == 0) {
                            if (!((lineSplit.length == 5 && timeDomain == 1)
                                    || (lineSplit.length == 6 && timeDomain == 2)
                                    || (lineSplit.length == 6 && timeDomain == 3)
                                    || (lineSplit.length == 7 && timeDomain == 4))) {
                                correctDomain = false;
                            } else {
                                correctDomain = true;
                            }
                        }

                        int id = Integer.parseInt(lineSplit[0]);
                        double x = Double.parseDouble(lineSplit[1 + retData.timeUnitQty()]);
                        double y = Double.parseDouble(lineSplit[2 + retData.timeUnitQty()]);
                        double measurement = Double.parseDouble(lineSplit[3 + retData.timeUnitQty()]);
                        int[] time = new int[retData.timeUnitQty()];
                        for (int j = 0; j < retData.timeUnitQty(); j++) {
                            time[j] = Integer.parseInt(lineSplit[j + 1]);
                        }

                        DataPoint dataPoint1 = new DataPoint(id, time, x, y, measurement);
                        DataPoint dataPoint2 = new DataPoint(id, time, x, y, measurement);
                        retData.set(i, dataPoint1);
                        inputDataScaled.set(i, dataPoint2);
                    }
                    for (int j = 0; j < retData.getSize(); j++) {
                        inputDataScaled.get(j).setX(inputDataScaled.get(j).getX() * xyScale);
                        inputDataScaled.get(j).setY(inputDataScaled.get(j).getY() * xyScale);
                        inputDataScaled.get(j).setX(inputDataScaled.get(j).getX() + xOffset);
                        inputDataScaled.get(j).setY(inputDataScaled.get(j).getY() + yOffset);
                    }
                    br.close();
                    retData.setAllT();
                } catch (Exception e) {
                    System.err.println("Error:" + e.getMessage());
                }
                return retData;
            }

            @Override
            protected void done() {
                if (inputSize > 0) {
                    try {
                        inputData = get();
                        if (!correctDomain) {
                            statusLabel2.setText("Wrong time domain chosen!");
                            statusPanel2.setBackground(Color.red);
                        } else {
                            statusLabel2.setText("Observation sites shown.");
                            statusPanel2.setBackground(Color.green);
                            inputData = get();
                            inputTree = new KDTree(inputData);
                            inputProcessed = true;
                            mapPanel.repaint();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
        SwingWorker<Integer, Void> countLinesWorker = new SwingWorker<Integer, Void>() {

            @Override
            public Integer doInBackground() {
                int count = -1; // ignore first line of input (header row)
                // also, input assumed to end with empty line
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(inputFile));
                    while (br.readLine() != null) {
                        count++;
                    }
                    br.close();
                } catch (Exception e) {
                    System.err.println("Error:" + e.getMessage());
                }
                return count;
            }

            @Override
            protected void done() {
                try {
                    inputSize = get();
                    if (inputSize > 0) {
                        statusLabel2.setText("Processing " + inputSize + " data points...");
                        processInputWorker.execute();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        if (inputFile != null && timeDomain != 0) {
            statusLabel2.setText("Counting data points...");
            statusPanel2.setBackground(Color.yellow);
            countLinesWorker.execute();
        }
    }

    public void computeOutput() {

        final SwingWorker<Void, Void> outputWriteWorker = new SwingWorker<Void, Void>() {

            @Override
            public Void doInBackground() {

                BufferedWriter bw = null;
                try {
                    bw = new BufferedWriter(new FileWriter(outputFile));
                    if (timeDomain == 1) {
                        bw.write(String.format("%-11s%-6s%-6s\n", "county_id", "year", "pm25"));
                        for (int i = 0; i < outputSize; i++) {
                            percentComplete1 = ((double) i / (double) outputSize) * 100;
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    statusLabel6.setText("[" + String.format("%2.1f", percentComplete1) + "% complete]");
                                }
                            });
                            bw.write(String.format("%-11d%-6d%-6.1f\n", outputData.get(i).getId(), outputData.get(i).getTime()[0], outputData.get(i).getMeasurement()));
                        }
                    } else if (timeDomain == 2) {
                        bw.write(String.format("%-11s%-6s%-6s%-6s\n", "county_id", "year", "month", "pm25"));
                        for (int i = 0; i < outputSize; i++) {
                            percentComplete1 = ((double) i / (double) outputSize) * 100;
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    statusLabel6.setText("[" + String.format("%2.1f", percentComplete1) + "% complete]");
                                }
                            });
                            bw.write(String.format("%-11d%-6d%-6d%-6.1f\n", outputData.get(i).getId(), outputData.get(i).getTime()[0], outputData.get(i).getTime()[1], outputData.get(i).getMeasurement()));
                        }
                    } else if (timeDomain == 3) {
                        bw.write(String.format("%-11s%-6s%-6s%-6s\n", "county_id", "year", "quarter", "pm25"));
                        for (int i = 0; i < outputSize; i++) {
                            percentComplete1 = ((double) i / (double) outputSize) * 100;
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    statusLabel6.setText("[" + String.format("%2.1f", percentComplete1) + "% complete]");
                                }
                            });
                            bw.write(String.format("%-11d%-6d%-6d%-6.1f\n", outputData.get(i).getId(), outputData.get(i).getTime()[0], outputData.get(i).getTime()[1], outputData.get(i).getMeasurement()));
                        }
                    } else if (timeDomain == 4) {
                        bw.write(String.format("%-11s%-6s%-6s%-6s%-6s\n", "county_id", "year", "month", "day", "pm25"));
                        for (int i = 0; i < outputSize; i++) {
                            percentComplete1 = ((double) i / (double) outputSize) * 100;
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    statusLabel6.setText("[" + String.format("%2.1f", percentComplete1) + "% complete]");
                                }
                            });
                            bw.write(String.format("%-11d%-6d%-6d%-6d%-6.1f\n", outputData.get(i).getId(), outputData.get(i).getTime()[0], outputData.get(i).getTime()[1], outputData.get(i).getTime()[2], outputData.get(i).getMeasurement()));
                        }
                    }
                    bw.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    statusPanel5.setBackground(Color.green);
                    statusPanel6.setBackground(Color.green);
                    statusLabel5.setText(outputSize + " data points written to file.");
                    statusLabel6.setText("");
                    outputComputed = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        final SwingWorker<Void, Void> interpWorker = new SwingWorker<Void, Void>() {

            @Override
            public Void doInBackground() {

                //System.out.println("Initial DS len/size/outputsize: " + outputData.getData().length + "/" + outputData.getSize() + "/" + outputSize);
                long startTime = System.currentTimeMillis();
                //get number of processors
                int procs = Runtime.getRuntime().availableProcessors();

                //create a ExecutorService with fixed Thread count set to # of procs
                ExecutorService executor = Executors.newFixedThreadPool(procs);

                int partitionSize = (int) Math.ceil(outputSize / procs);
                int partitionLen = 0;
                ArrayList<Future<DataSet>> futureList = new ArrayList<Future<DataSet>>();

                //disbatch Callables to ExecutorService
                for (int i = 0, start = 0; i < procs; i++, start += partitionSize + 1) {
                    partitionLen = (start + partitionSize >= outputSize) ? outputSize : start + partitionSize + 1;
                    DataSet ds = DataSet.copyOfRange(outputData, start, partitionLen);
                    Callable<DataSet> worker = new CallableIDW(inputTree, ds, nnn, exp);
                    System.out.println("Starting thread " + i + ": [" + start + " - " + partitionLen + "]");
                    Future<DataSet> f = executor.submit(worker);
                    futureList.add(f);
                }

                //lets get our data back from the threads...
                int maxFutures = futureList.size();

                Future<DataSet> f = futureList.get(0);
                try {
                    //  System.out.println("Waiting for thread 0.");
                    outputData = f.get();
                    //   System.out.println("Thread 0 is done. size=" + outputData.getData().length);

                } catch (InterruptedException ex) {
                    Logger.getLogger(PM25GUI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(PM25GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                for (int i = 1; i < maxFutures; i++) {
                    f = futureList.get(i);
                    try {
                        // System.out.println("Waiting on thread " + i);
                        outputData = DataSet.combineDataSet(outputData, f.get());
                        System.out.println("Thread " + i + " is done. size=" + outputData.getData().length);


                    } catch (InterruptedException ex) {
                        System.out.println(ex.toString());
                    } catch (ExecutionException ex) {
                        System.out.println(ex.toString());
                    }
                }

                return null;
            }

            @Override
            protected void done() {
                try {
                    outputComputed = true;
                    statusLabel5.setText("Writing " + outputSize + " data points to file...");
                    statusLabel6.setText("");
                    outputWriteWorker.execute();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        final SwingWorker<Void, Void> interpWorker_old = new SwingWorker<Void, Void>() {

            @Override
            public Void doInBackground() {
                long startTime = System.currentTimeMillis();
                for (int i = 0; i < outputSize; i++) {
                    percentComplete1 = ((double) i / (double) outputSize) * 100;
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            statusLabel6.setText("[" + String.format("%2.1f", percentComplete1) + "% complete]");
                        }
                    });
                    DataPoint tp = outputData.get(i);
                    Neighbor[] nn = inputTree.nearestNeighbors(tp, nnn);
                    double interpVal = IDW.calc(tp, nn, exp);
                    tp.setMeasurement(interpVal);
                }
                outputDataScaled = new DataSet(outputData);
                for (int j = 0; j < outputSize; j++) {
                    outputDataScaled.get(j).setX(outputDataScaled.get(j).getX() * xyScale);
                    outputDataScaled.get(j).setY(outputDataScaled.get(j).getY() * xyScale);
                    outputDataScaled.get(j).setX(outputDataScaled.get(j).getX() + xOffset);
                    outputDataScaled.get(j).setY(outputDataScaled.get(j).getY() + yOffset);
                }
                long totalTime = System.currentTimeMillis() - startTime;
                System.out.println("Time: " + totalTime);
                return null;
            }

            @Override
            protected void done() {
                try {
                    outputComputed = true;
                    statusLabel5.setText("Writing " + outputSize + " data points to file...");
                    statusLabel6.setText("");
                    outputWriteWorker.execute();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        final SwingWorker<DataSet, Void> processLocsWorker = new SwingWorker<DataSet, Void>() {

            @Override
            public DataSet doInBackground() {
                BufferedReader br = null;
                String line;
                String[] lineSplit;
                int timesPerLoc = 0;
                if (timeDomain == 1) {
                    timesPerLoc = 1;
                } // year
                else if (timeDomain == 2) {
                    timesPerLoc = 12;
                } // month
                else if (timeDomain == 3) {
                    timesPerLoc = 4;
                } // quarter
                else if (timeDomain == 4) {
                    timesPerLoc = 365;
                } // day
                outputSize = numLocs * timesPerLoc;
                DataSet retData = new DataSet(outputSize, timeDomain);
                int rdc = 0; // retDataCounter
                int inputNumYears = inputData.numYears();
                int inputStartYear = inputData.startYear();
                try {
                    br = new BufferedReader(new FileReader(locFile));
                    br.readLine(); // throw away header
                    for (int i = 0; i < numLocs; i++) {
                        line = br.readLine();
                        lineSplit = line.split("\\s+");

                        int id = Integer.parseInt(lineSplit[0]);
                        double x = Double.parseDouble(lineSplit[1]);
                        double y = Double.parseDouble(lineSplit[2]);

                        for (int j = 0; j < inputNumYears; j++) {
                            if (timeDomain == 1) {
                                int[] time = {inputStartYear + j};
                                DataPoint dp = new DataPoint(id, time, x, y, -1);
                                retData.set(rdc, dp);
                                rdc++;
                            } else if (timeDomain == 2) {
                                for (int k = 1; k <= 12; k++) {
                                    int[] time = {inputStartYear + j, k};
                                    DataPoint dp = new DataPoint(id, time, x, y, -1);
                                    retData.set(rdc, dp);
                                    rdc++;
                                }
                            } else if (timeDomain == 3) {
                                for (int k = 1; k <= 4; k++) {
                                    int[] time = {inputStartYear + j, k};
                                    DataPoint dp = new DataPoint(id, time, x, y, -1);
                                    retData.set(rdc, dp);
                                    rdc++;
                                }
                            } else if (timeDomain == 4) {
                                for (int k = 1; k <= 31; k++) { // JANUARY
                                    int[] time = {inputStartYear + j, 1, k};
                                    DataPoint dp = new DataPoint(id, time, x, y, -1);
                                    retData.set(rdc, dp);
                                    rdc++;
                                }
                                for (int k = 1; k <= 28; k++) { // FEBRUARY
                                    int[] time = {inputStartYear + j, 2, k};
                                    DataPoint dp = new DataPoint(id, time, x, y, -1);
                                    retData.set(rdc, dp);
                                    rdc++;
                                }
                                for (int k = 1; k <= 31; k++) { // MARCH
                                    int[] time = {inputStartYear + j, 3, k};
                                    DataPoint dp = new DataPoint(id, time, x, y, -1);
                                    retData.set(rdc, dp);
                                    rdc++;
                                }
                                for (int k = 1; k <= 30; k++) { // APRIL
                                    int[] time = {inputStartYear + j, 4, k};
                                    DataPoint dp = new DataPoint(id, time, x, y, -1);
                                    retData.set(rdc, dp);
                                    rdc++;
                                }
                                for (int k = 1; k <= 31; k++) { // MAY
                                    int[] time = {inputStartYear + j, 5, k};
                                    DataPoint dp = new DataPoint(id, time, x, y, -1);
                                    retData.set(rdc, dp);
                                    rdc++;
                                }
                                for (int k = 1; k <= 30; k++) { // JUNE
                                    int[] time = {inputStartYear + j, 6, k};
                                    DataPoint dp = new DataPoint(id, time, x, y, -1);
                                    retData.set(rdc, dp);
                                    rdc++;
                                }
                                for (int k = 1; k <= 31; k++) { // JULY
                                    int[] time = {inputStartYear + j, 7, k};
                                    DataPoint dp = new DataPoint(id, time, x, y, -1);
                                    retData.set(rdc, dp);
                                    rdc++;
                                }
                                for (int k = 1; k <= 31; k++) { // AUGUST
                                    int[] time = {inputStartYear + j, 8, k};
                                    DataPoint dp = new DataPoint(id, time, x, y, -1);
                                    retData.set(rdc, dp);
                                    rdc++;
                                }
                                for (int k = 1; k <= 30; k++) { // SEPTEMBER
                                    int[] time = {inputStartYear + j, 9, k};
                                    DataPoint dp = new DataPoint(id, time, x, y, -1);
                                    retData.set(rdc, dp);
                                    rdc++;
                                }
                                for (int k = 1; k <= 31; k++) { // OCTOBER
                                    int[] time = {inputStartYear + j, 10, k};
                                    DataPoint dp = new DataPoint(id, time, x, y, -1);
                                    retData.set(rdc, dp);
                                    rdc++;
                                }
                                for (int k = 1; k <= 30; k++) { // NOVEMBER
                                    int[] time = {inputStartYear + j, 11, k};
                                    DataPoint dp = new DataPoint(id, time, x, y, -1);
                                    retData.set(rdc, dp);
                                    rdc++;
                                }
                                for (int k = 1; k <= 31; k++) { // DECEMBER
                                    int[] time = {inputStartYear + j, 12, k};
                                    DataPoint dp = new DataPoint(id, time, x, y, -1);
                                    retData.set(rdc, dp);
                                    rdc++;
                                }
                            }
                        }
                    }
                    retData.setAllT();
                    br.close();
                } catch (Exception e) {
                    System.err.println("Error:" + e.getMessage());
                }
                return retData;
            }

            @Override
            protected void done() {
                try {
                    outputData = get();
                    locationsProcessed = true;
                    statusLabel5.setText("Interpolating " + outputSize + " data points...");
                    interpWorker.execute();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        SwingWorker<Integer, Void> countLocsWorker = new SwingWorker<Integer, Void>() {

            @Override
            public Integer doInBackground() {
                int count = -1; // ignore first line of input (header row)
                // also, input assumed to end with empty line
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(locFile));
                    String line;
                    while ((line = br.readLine()) != null) {
                        count++;
                    }
                    br.close();
                } catch (Exception e) {
                    System.err.println("Error:" + e.getMessage());
                }
                return count;
            }

            @Override
            protected void done() {
                try {
                    numLocs = get();
                    if (numLocs > 0) {
                        statusLabel5.setText("Processing " + numLocs + " locations...");
                        processLocsWorker.execute();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        if (inputFile != null && locFile != null && outputFile != null && timeDomain != 0 && nnn != 0 && exp != 0 && inputProcessed) {
            statusLabel5.setText("Counting locations...");
            statusPanel5.setBackground(Color.yellow);
            statusPanel6.setBackground(Color.yellow);
            countLocsWorker.execute();
        }
    }

    public void buildRasterSet() {
        int id = -1;
        double x;
        double y;
        int inputNumYears = inputData.numYears();
        int inputStartYear = inputData.startYear();
        ArrayList<DataPoint> dpa = new ArrayList<DataPoint>();
        for (x = 38.0; x - (rasterCellSizeScaled / 2) < 732.0; x += rasterCellSizeScaled) {
            for (y = 244.0; y - (rasterCellSizeScaled / 2) < 543.0; y += rasterCellSizeScaled) {
                for (int j = 0; j < inputNumYears; j++) {
                    // optional  method only implements for timeDomain = 4
                    if (timeDomain == 4) {
                        for (int k = 1; k <= 31; k++) { // JANUARY
                            int[] time = {inputStartYear + j, 1, k};
                            DataPoint dp = new DataPoint(id, time, x, y, -1);
                            dpa.add(dp);
                        }
                        for (int k = 1; k <= 28; k++) { // FEBRUARY
                            int[] time = {inputStartYear + j, 2, k};
                            DataPoint dp = new DataPoint(id, time, x, y, -1);
                            dpa.add(dp);
                        }
                        for (int k = 1; k <= 31; k++) { // MARCH
                            int[] time = {inputStartYear + j, 3, k};
                            DataPoint dp = new DataPoint(id, time, x, y, -1);
                            dpa.add(dp);
                        }
                        for (int k = 1; k <= 30; k++) { // APRIL
                            int[] time = {inputStartYear + j, 4, k};
                            DataPoint dp = new DataPoint(id, time, x, y, -1);
                            dpa.add(dp);
                        }
                        for (int k = 1; k <= 31; k++) { // MAY
                            int[] time = {inputStartYear + j, 5, k};
                            DataPoint dp = new DataPoint(id, time, x, y, -1);
                            dpa.add(dp);
                        }
                        for (int k = 1; k <= 30; k++) { // JUNE
                            int[] time = {inputStartYear + j, 6, k};
                            DataPoint dp = new DataPoint(id, time, x, y, -1);
                            dpa.add(dp);
                        }
                        for (int k = 1; k <= 31; k++) { // JULY
                            int[] time = {inputStartYear + j, 7, k};
                            DataPoint dp = new DataPoint(id, time, x, y, -1);
                            dpa.add(dp);
                        }
                        for (int k = 1; k <= 31; k++) { // AUGUST
                            int[] time = {inputStartYear + j, 8, k};
                            DataPoint dp = new DataPoint(id, time, x, y, -1);
                            dpa.add(dp);
                        }
                        for (int k = 1; k <= 30; k++) { // SEPTEMBER
                            int[] time = {inputStartYear + j, 9, k};
                            DataPoint dp = new DataPoint(id, time, x, y, -1);
                            dpa.add(dp);
                        }
                        for (int k = 1; k <= 31; k++) { // OCTOBER
                            int[] time = {inputStartYear + j, 10, k};
                            DataPoint dp = new DataPoint(id, time, x, y, -1);
                            dpa.add(dp);
                        }
                        for (int k = 1; k <= 30; k++) { // NOVEMBER
                            int[] time = {inputStartYear + j, 11, k};
                            DataPoint dp = new DataPoint(id, time, x, y, -1);
                            dpa.add(dp);
                        }
                        for (int k = 1; k <= 31; k++) { // DECEMBER
                            int[] time = {inputStartYear + j, 12, k};
                            DataPoint dp = new DataPoint(id, time, x, y, -1);
                            dpa.add(dp);
                        }
                    }
                }
            }
        }
        rasterDataScaled = new DataSet(dpa.size(), timeDomain);
        DataPoint[] dpaa = new DataPoint[dpa.size()];
        dpa.toArray(dpaa);
        rasterDataScaled.setData(dpaa);
        rasterDataScaled.setAllT();
        rasterSetSize = rasterDataScaled.getSize();
    }

    public void computeRaster() {
        final SwingWorker<Void, Void> interpWorker = new SwingWorker<Void, Void>() {

            @Override
            public Void doInBackground() {
                rasterData = new DataSet(rasterDataScaled);
                for (int j = 0; j < rasterSetSize; j++) {
                    rasterData.get(j).setX(rasterData.get(j).getX() - xOffset);
                    rasterData.get(j).setY(rasterData.get(j).getY() - yOffset);
                    rasterData.get(j).setX(rasterData.get(j).getX() / xyScale);
                    rasterData.get(j).setY(rasterData.get(j).getY() / xyScale);
                }
                for (int i = 0; i < rasterSetSize; i++) {
                    percentComplete2 = ((double) i / (double) rasterSetSize) * 100;
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            aniPanelStatusBox.setBackground(Color.yellow);
                            aniLabelStatus.setText("<html>Building raster data set...<br/>[" + String.format("%2.1f", percentComplete2) + "% complete]");
                        }
                    });
                    DataPoint tp = rasterData.get(i);
                    Neighbor[] nn = inputTree.nearestNeighbors(tp, nnn);
                    double interpVal = IDW.calc(tp, nn, exp);
                    tp.setMeasurement(interpVal);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    rasterComputed = true;
                    aniPanelStatusBox.setBackground(Color.green);
                    aniLabelStatus.setText("Ready to animate!");
                    aniButton.setText("Play");
                    showObsSites = false;
                    statusLabel2.setText("Observation sites hidden.");
                    aniButton.setEnabled(true);
                    mapPanel.repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        interpWorker.execute();
    }

    public void animate() {
        final SwingWorker<Void, Void> interpWorker = new SwingWorker<Void, Void>() {

            @Override
            public Void doInBackground() {
                // OPTIONAL method only implemented for timeDomain = 4
                if (timeDomain == 4) {
                    for (animationDay = 0; !animationComplete;) {
                        try {
                            Thread.sleep(100);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                int[] time = rasterData.get(animationDay).getTime();
                                aniLabelStatus.setText(
                                        "<html>Now playing animation...<br/>Time: "
                                        + time[1] + "/" + time[2] + "/" + time[0] + "</html>");
                                mapPanel.repaint();
                            }
                        });
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    aniLabelStatus.setText("Animation complete.");
                    aniButton.setEnabled(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        interpWorker.execute();
    }

    public void validate() {
        final SwingWorker<Void, Void> validateWorker = new SwingWorker<Void, Void>() {

            @Override
            public Void doInBackground() {
                va = LOOCV.calc(inputData, inputTree, validateLabelStatus);
                return null;
            }

            @Override
            protected void done() {
                validatePanelStatusBox.setBackground(Color.green);
                validationComplete = true;
            }
        };
        validatePanelStatusBox.setBackground(Color.yellow);
        validateWorker.execute();
    }

    public void measure() {
        final SwingWorker<Void, Void> measureWorker = new SwingWorker<Void, Void>() {

            @Override
            public Void doInBackground() {
                LOOCV.errorCalc(inputData, va, measureLabelStatus);
                return null;
            }

            @Override
            protected void done() {
                measurePanelStatusBox.setBackground(Color.green);
            }
        };
        measurePanelStatusBox.setBackground(Color.yellow);
        measureWorker.execute();
    }

    class MapPanel extends JPanel {

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            if (rasterComputed && animationDay < (rasterData.numYears() * 365)) {
                int rcss = (int) rasterCellSizeScaled;
                int j = animationDay;
                int x1 = (int) (rasterDataScaled.getX(j) - rcss / 2);
                int y2 = (int) (600 - rasterDataScaled.getY(j) + rcss / 2);
                for (; j < rasterSetSize; j += 365) {
                    g.setColor(getRasterColor(rasterData.get(j).getMeasurement()));
                    g2d.fillRect((int) (rasterDataScaled.getX(j) - rcss / 2), (int) (600 - rasterDataScaled.getY(j) - rcss / 2), rcss, rcss);
                }
                int x2 = (int) (rasterDataScaled.getX(j - 365) - rcss / 2) + rcss;
                int y1 = (int) (600 - rasterDataScaled.getY(j - 365) - rcss / 2);
                g.setColor(Color.black);
                g2d.drawRect(x1, y1, (x2 - x1), (y2 - y1));
                g2d.drawRect(x1 - 1, y1 - 1, (x2 - x1) + 2, (y2 - y1) + 2);
                if (animate && animationDay != (365 * inputData.numYears() - 1)) {
                    animationDay++;
                } else if (animationDay == (365 * inputData.numYears() - 1)) {
                    animationComplete = true;
                }
            }
            if (bordersProcessed) {
                g.setColor(Color.BLACK);
                for (int i = 0; i < numBorderCoords; i++) {
                    g2d.fillRect((int) stateBorders[i].getX(), (int) (600 - stateBorders[i].getY()), 2, 2);
                }
            }
            if (inputProcessed && showObsSites && !animate) {
                g.setColor(Color.RED);
                for (int i = 0; i < inputSize; i++) {
                    Ellipse2D.Double circle = new Ellipse2D.Double(inputDataScaled.get(i).getX(), (600 - inputDataScaled.get(i).getY()), 5, 5);
                    g2d.fill(circle);
                }
            }
        }
    }

    public Color getRasterColor(double meas) {
        Color c = new Color(0x00, 0x00, 0xff); // blue indicates error
        if (meas >= 0 && meas < 8.7) {
            if (meas < 5.8) {
                if (meas < 3.8) {
                    c = new Color(0xff, 0x00, 0x00);
                } else if (meas < 4.9) {
                    c = new Color(0xff, 0x2a, 0x00);
                } else {
                    c = new Color(0xff, 0x55, 0x00);
                }
            } else {
                if (meas < 6.7) {
                    c = new Color(0xff, 0x7f, 0x00);
                } else if (meas < 7.7) {
                    c = new Color(0xff, 0xaa, 0x00);
                } else {
                    c = new Color(0xff, 0xd4, 0x00);
                }
            }
        } else if (meas < 985) {
            if (meas < 12.6) {
                if (meas < 9.8) {
                    c = new Color(0xff, 0xff, 0x00);
                } else if (meas < 11) {
                    c = new Color(0xd5, 0xff, 0x00);
                } else {
                    c = new Color(0xaa, 0xff, 0x00);
                }
            } else {
                if (meas < 14.8) {
                    c = new Color(0x80, 0xff, 0x00);
                } else if (meas < 17.7) {
                    c = new Color(0x55, 0xff, 0x00);
                } else if (meas < 23) {
                    c = new Color(0x2a, 0xff, 0x00);
                } else {
                    c = new Color(0x00, 0xff, 0x00);
                }
            }
        }
        return c;
    }
}