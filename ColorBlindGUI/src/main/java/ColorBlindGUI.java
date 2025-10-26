import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import javax.imageio.ImageIO;

import org.bytedeco.javacv.Frame;
import java.awt.image.BufferedImage;


public class ColorBlindGUI extends JFrame {
    private JLabel originalLabel, filteredLabel;
    private BufferedImage originalImage, filteredImage;
    private BufferedImage imageFromCamera;
    private JComboBox<String> filterSelector;
    private JCheckBox correctionMode;
    private JSlider intensitySlider;
    private JCheckBox imageCamera;
    private frameConverter frameConversion;
    private Camera camera;
    private Timer cameraTimer;

    public ColorBlindGUI() {
        setTitle("Colorblind Image Altering");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLayout(new BorderLayout());

        // ======== Top Panel ========
        JPanel topPanel = new JPanel();
        JButton loadButton = new JButton("Start");
        filterSelector = new JComboBox<>(new String[]{"None", "Deuteranopia (Red/Green)", "Protanopia (Red/Green)", "Tritanopia (Blue/Yellow)"});
        correctionMode = new JCheckBox("Flexible %");
        JButton applyButton = new JButton("Apply Filter to Frame");

        intensitySlider = new JSlider(0, 100, 100);
        intensitySlider.setMajorTickSpacing(25);
        intensitySlider.setPaintTicks(true);
        intensitySlider.setPaintLabels(true);
        intensitySlider.setPreferredSize(new Dimension(200, 50));
        JLabel intensityLabel = new JLabel("Intensity:");

        topPanel.add(loadButton);
        topPanel.add(filterSelector);
        topPanel.add(correctionMode);
        topPanel.add(intensityLabel);
        topPanel.add(intensitySlider);
        topPanel.add(applyButton);


        // ======== Image Display ========
        JPanel imagePanel = new JPanel(new GridLayout(1, 2));
        originalLabel = new JLabel("Original Image", SwingConstants.CENTER);
        filteredLabel = new JLabel("Filtered Image", SwingConstants.CENTER);
        imagePanel.add(originalLabel);
        imagePanel.add(filteredLabel);
        add(imagePanel, BorderLayout.CENTER);

        // ======== Actions ========
        loadButton.addActionListener(e -> loadImage());
        applyButton.addActionListener(e -> applyFilter());
        intensitySlider.addChangeListener(e -> applyFilter()); // live update when sliding

        // ========== Additional Features ===========
        imageCamera = new JCheckBox("Image Loading from Device Camera");
        topPanel.add(imageCamera);
        frameConversion = new frameConverter();
        camera = new Camera();
        imageCamera.addActionListener(e -> {
            if (!imageCamera.isSelected() && cameraTimer != null && cameraTimer.isRunning()) {
                cameraTimer.stop();
                try {
                    camera.closeCamera();
                } catch (Exception e1) {
                    System.out.println(e1.getMessage());
                }
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (cameraTimer != null && cameraTimer.isRunning()) {
                    cameraTimer.stop();
                }
                try {
                    camera.closeCamera();
                } catch (Exception e2) {
                    System.out.println(e2.getMessage());
                }
            }
        });



        add(topPanel, BorderLayout.NORTH);

        setVisible(true);
    }


    private void loadImage() {
        boolean deviceCameraSelected = imageCamera.isSelected();
        System.out.println("Load Image called. Camera selected: " + deviceCameraSelected);

        if (deviceCameraSelected) {
            // Stop any existing camera timer
            if (cameraTimer != null && cameraTimer.isRunning()) {
                System.out.println("Stopping existing camera timer");
                cameraTimer.stop();
            }

            try {
                System.out.println("Starting camera...");
                camera.startCamera();
                System.out.println("Camera started successfully");

                // Create a timer to continuously grab frames
                cameraTimer = new Timer(33, e -> { // ~30 fps
                    try {
                        Frame frame = camera.getFrame();
                        System.out.println("Frame grabbed: " + (frame != null));

                        if (frame != null) {
                            imageFromCamera = frameConversion.convertFrameToBufferedImage(frame);
                            System.out.println("Frame converted: " + (imageFromCamera != null));

                            originalImage = imageFromCamera; // Set as original for filters

                            if (originalImage != null) {
                                System.out.println("Image size: " + originalImage.getWidth() + "x" + originalImage.getHeight());
                                originalLabel.setIcon(new ImageIcon(originalImage.getScaledInstance(
                                        originalLabel.getWidth(),
                                        originalLabel.getHeight(),
                                        Image.SCALE_SMOOTH)));
                            }
                        }
                    } catch (Exception e1) {
                        System.err.println("Error in timer: " + e1.getMessage());

                        cameraTimer.stop();
                        try {
                            camera.closeCamera();
                        } catch (Exception closeEx) {
                            closeEx.printStackTrace();
                        }
                        JOptionPane.showMessageDialog(ColorBlindGUI.this, "Error capturing frame: " + e1.getMessage());
                    }
                });
                cameraTimer.start();
                System.out.println("Camera timer started");

            } catch (Exception e) {
                System.err.println("Error starting camera: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error starting camera: " + e.getMessage());
            }
        } else {
            // Stop camera if it's running
            if (cameraTimer != null && cameraTimer.isRunning()) {
                cameraTimer.stop();
                try {
                    camera.closeCamera();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Load from file
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    originalImage = ImageIO.read(file);
                    originalLabel.setIcon(new ImageIcon(originalImage.getScaledInstance(
                            originalLabel.getWidth(),
                            originalLabel.getHeight(),
                            Image.SCALE_SMOOTH)));
                    filteredLabel.setIcon(null);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage());
                }
            }
        }
        applyFilter();
    }

    private void applyFilter() {
        if (originalImage == null) return;

        String filterType = (String) filterSelector.getSelectedItem();
        boolean correct = correctionMode.isSelected();
        double intensity = intensitySlider.getValue() / 100.0;

        if ("None".equals(filterType)) {
            filteredImage = originalImage;
        } else {
            filteredImage = simulateColorBlindness(originalImage, filterType, correct, intensity);
        }

        filteredLabel.setIcon(new ImageIcon(filteredImage.getScaledInstance(filteredLabel.getWidth(), filteredLabel.getHeight(), Image.SCALE_SMOOTH)));
    }

    // ======== Core Transformation Logic ========
    private BufferedImage simulateColorBlindness(BufferedImage src, String type, boolean correct, double intensity) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        double[][] M = switch (type) {
            case "Protanopia (Red/Green)" -> new double[][]{
                    {0.56667, 0.43333, 0.0},
                    {0.55833, 0.44167, 0.0},
                    {0.0, 0.24167, 0.75833}
            };
            case "Deuteranopia (Red/Green)" -> new double[][]{
                    {0.625, 0.375, 0.0},
                    {0.7, 0.3, 0.0},
                    {0.0, 0.3, 0.7}
            };
            case "Tritanopia (Blue/Yellow)" -> new double[][]{
                    {0.95, 0.05, 0.0},
                    {0.0, 0.43333, 0.56667},
                    {0.0, 0.475, 0.525}
            };
            default -> new double[][]{
                    {1, 0, 0},
                    {0, 1, 0},
                    {0, 0, 1}
            };
        };

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c = new Color(src.getRGB(x, y));
                double r = c.getRed() / 255.0;
                double g = c.getGreen() / 255.0;
                double b = c.getBlue() / 255.0;

                // Simulate deficiency
                double rSim = M[0][0] * r + M[0][1] * g + M[0][2] * b;
                double gSim = M[1][0] * r + M[1][1] * g + M[1][2] * b;
                double bSim = M[2][0] * r + M[2][1] * g + M[2][2] * b;

                if (correct) {
                    // Daltonization correction
                    double errR = r - rSim;
                    double errG = g - gSim;
                    double errB = b - bSim;
                    r = clamp(r + 0.7 * errR);
                    g = clamp(g + 0.7 * errG);
                    b = clamp(b + 0.7 * errB);
                } else {
                    // Blend between original and simulated
                    r = r + (rSim - r) * intensity;
                    g = g + (gSim - g) * intensity;
                    b = b + (bSim - b) * intensity;
                }

                result.setRGB(x, y, new Color((float) r, (float) g, (float) b).getRGB());
            }
        }

        return result;
    }

    private double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ColorBlindGUI::new);
    }
}

