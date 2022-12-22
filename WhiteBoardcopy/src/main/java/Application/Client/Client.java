package Application.Client;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.image.*;
import java.awt.event.*;

public class Client {
    boolean isConnected = false;
    String serverHost = null;
    int serverPort;
    Socket clientSocket;
    BufferedReader readSocket;
    BufferedWriter writeSocket;
    JFrame frame;
    JToolBar toolbar;
    JPanel menu;
    JLabel existLabel;
    JLabel notFoundLabel;
    BoardPanel boardPanel;
    BufferedImage board = null;
    Graphics2D graphics;
    Color currentColor;
    int brushSize = 10;
    class BoardPanel extends JPanel implements Serializable {
        private static final long serialVersionUID = -109728024865681281L;
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(board, 0, 0, this);
        }
    }

    class BoardThread extends Thread {
        String message;
        String[] splitMessage;
        public BoardThread() {
            this.start();
        }
        public void run() {
            try {
                try {
                    while (true) {
                        message = readSocket.readLine();
                        splitMessage = message.split(" ", 2);
                        if (splitMessage[0].equals("CREATE")) {
                            if (splitMessage[1].equals("OK")) {
                                board = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
                                graphics = board.createGraphics();
                                graphics.setColor(Color.white);
                                graphics.fillRect(0, 0, 800, 600);
                                isConnected = true;
                                frame.remove(menu);
                                frame.add(boardPanel);
                                frame.repaint();
                            } else if (splitMessage[1].equals("EXISTS")) {
                                menu.add(existLabel);
                                frame.repaint();
                            }
                        } else if (splitMessage[0].equals("CONNECT")) {
                            if (splitMessage[1].equals("OK")) {
                                int[] rgbArray = new int[480000];
                                for (int i = 0; i < rgbArray.length; i++) {
                                    message = readSocket.readLine();
                                    rgbArray[i] = Integer.parseInt(message);
                                }
                                board = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
                                board.setRGB(0, 0, 800, 600, rgbArray, 0, 800);
                                graphics = board.createGraphics();
                                isConnected = true;
                                frame.remove(menu);
                                frame.add(boardPanel);
                                frame.repaint();
                            } else if (splitMessage[1].equals("NOT FOUND")) {
                                menu.add(notFoundLabel);
                                frame.repaint();
                            }
                        } else {
                            splitMessage = message.split(" ", 4);
                            int color = Integer.parseInt(splitMessage[0]);
                            int coordX = Integer.parseInt(splitMessage[1]);
                            int coordY = Integer.parseInt(splitMessage[2]);
                            int size = Integer.parseInt(splitMessage[3]);

                            graphics.setColor(new Color(color));
                            graphics.fillOval(coordX, coordY, size, size);
                            boardPanel.repaint();
                        }
                    }
                } catch (IOException err) {
                    System.out.println(err.toString());
                    readSocket.close();
                    writeSocket.close();
                }
            } catch (IOException err) {
                System.out.println(err.toString());
            }
        }
    }

    public Client(String serverHost, int serverPort) {
        try {
            try {
                this.serverHost = serverHost;
                this.serverPort = serverPort;
                clientSocket = new Socket(serverHost, serverPort);
                readSocket = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writeSocket = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                new BoardThread();
            } catch (IOException err) {
                System.out.println(err.toString());
                readSocket.close();
                writeSocket.close();
            }
        } catch (IOException err) {
            System.out.println(err.toString());
        }
        frame = new JFrame("MultiPaint");
        frame.setSize(840, 600);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.setVisible(true);
        boardPanel = new BoardPanel();
        boardPanel.setBounds(40, 0, 800, 600);
        boardPanel.setOpaque(true);
        currentColor = Color.white;
        menu = new JPanel();
        menu.setBounds(40, 0, 800, 600);
        menu.setBackground(currentColor);
        menu.setLayout(null);
        frame.add(menu);
        ImageIcon exist = new ImageIcon(this.getClass().getClassLoader().getResource("exist.png"));
        existLabel = new JLabel(exist);
        existLabel.setBounds(20, 85, 200, 30);
        ImageIcon notFound = new ImageIcon(this.getClass().getClassLoader().getResource("notFound.png"));
        notFoundLabel = new JLabel(notFound);
        notFoundLabel.setBounds(20, 85, 200, 30);
        ImageIcon invite = new ImageIcon(this.getClass().getClassLoader().getResource("invite.png"));
        JLabel inviteLabel = new JLabel(invite);
        inviteLabel.setBounds(20, 5, 200, 30);
        menu.add(inviteLabel);
        JTextField textField = new JTextField();
        textField.setBounds(20, 45, 200, 30);
        textField.setText("default");
        menu.add(textField);
        JButton createBoard = new JButton(
                new ImageIcon(this.getClass().getClassLoader().getResource("createBoard.png")));
        createBoard.setBounds(225, 40, 210, 40);
        createBoard.setBorderPainted(false);
        createBoard.setBackground(Color.lightGray);
        createBoard.setOpaque(false);
        createBoard.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String nameBoard = textField.getText();
                if (nameBoard.equals("")) {
                    frame.repaint();
                    return;
                }
                if (menu.isAncestorOf(existLabel)) {
                    menu.remove(existLabel);
                    frame.repaint();
                }
                if (menu.isAncestorOf(notFoundLabel)) {
                    menu.remove(notFoundLabel);
                    frame.repaint();
                }
                try {
                    try {
                        writeSocket.write("CREATE " + nameBoard + "\n");
                        writeSocket.flush();
                    } catch (IOException err) {
                        System.out.println(err.toString());
                        readSocket.close();
                        writeSocket.close();
                    }
                } catch (IOException err) {
                    System.out.println(err.toString());
                }
            }
        });
        menu.add(createBoard);
        JButton joinBoard = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("joinBoard.png")));
        joinBoard.setBounds(435, 40, 210, 40);
        joinBoard.setBorderPainted(false);
        joinBoard.setBackground(Color.lightGray);
        joinBoard.setOpaque(false);
        joinBoard.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String nameBoard = textField.getText();
                if (nameBoard.equals("")) {
                    System.out.println();
                    frame.repaint();
                    return;
                }
                if (menu.isAncestorOf(existLabel)) {
                    menu.remove(existLabel);
                    frame.repaint();
                }
                if (menu.isAncestorOf(notFoundLabel)) {
                    menu.remove(notFoundLabel);
                    frame.repaint();
                }
                try {
                    try {
                        writeSocket.write("CONNECT " + nameBoard + "\n");
                        writeSocket.flush();
                    } catch (IOException err) {
                        System.out.println(err.toString());
                        readSocket.close();
                        writeSocket.close();
                    }
                } catch (IOException err) {
                    System.out.println(err.toString());
                }
            }
        });
        menu.add(joinBoard);
        JToolBar toolbar = new JToolBar("Toolbar", JToolBar.VERTICAL);
        toolbar.setBounds(0, 0, 40, 600);
        toolbar.setLayout(null);
        toolbar.setFloatable(false);
        toolbar.setBorderPainted(false);
        toolbar.setBackground(currentColor);
        frame.add(toolbar);
        JButton menuButton = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("menu.png")));
        menuButton.setBounds(0, 0, 40, 40);
        menuButton.setBorderPainted(false);
        menuButton.setBackground(Color.lightGray);
        menuButton.setOpaque(false);
        menuButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (isConnected) {
                    if (frame.isAncestorOf(menu)) {
                        frame.remove(menu);
                        frame.add(boardPanel);
                        frame.repaint();
                    } else {
                        frame.remove(boardPanel);
                        frame.add(menu);
                        frame.repaint();
                    }
                }
            }
        });
        toolbar.add(menuButton);
        JButton size4Button = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("size10.png")));
        size4Button.setBounds(0, 40, 40, 40);
        size4Button.setBorderPainted(false);
        size4Button.setBackground(Color.lightGray);
        size4Button.setOpaque(false);
        size4Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                brushSize = 10;
            }
        });
        toolbar.add(size4Button);
        JButton size10Button = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("size20.png")));
        size10Button.setBounds(0, 80, 40, 40);
        size10Button.setBorderPainted(false);
        size10Button.setBackground(Color.lightGray);
        size10Button.setOpaque(false);
        size10Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                brushSize = 20;
            }
        });
        toolbar.add(size10Button);
        JButton size20Button = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("size40.png")));
        size20Button.setBounds(0, 120, 40, 40);
        size20Button.setBorderPainted(false);
        size20Button.setBackground(Color.lightGray);
        size20Button.setOpaque(false);
        size20Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                brushSize = 40;
            }
        });
        toolbar.add(size20Button);
        JButton size30Button = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("size80.png")));
        size30Button.setBounds(0, 160, 40, 40);
        size30Button.setBorderPainted(false);
        size30Button.setBackground(Color.lightGray);
        size30Button.setOpaque(false);
        size30Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                brushSize = 80;
            }
        });
        toolbar.add(size30Button);
        JButton whiteButton = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("white.png")));
        whiteButton.setBounds(0, 240, 40, 40);
        whiteButton.setBorderPainted(false);
        whiteButton.setBackground(Color.lightGray);
        whiteButton.setOpaque(false);
        whiteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                currentColor = Color.white;
                toolbar.setBackground(currentColor);
                menu.setBackground(currentColor);
            }
        });
        toolbar.add(whiteButton);
        JButton blackButton = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("black.png")));
        blackButton.setBounds(0, 280, 40, 40);
        blackButton.setBorderPainted(false);
        blackButton.setBackground(Color.lightGray);
        blackButton.setOpaque(false);
        blackButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                currentColor = Color.black;
                toolbar.setBackground(currentColor);
                menu.setBackground(currentColor);
            }
        });
        toolbar.add(blackButton);
        JButton redButton = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("red.png")));
        redButton.setBounds(0, 320, 40, 40);
        redButton.setBorderPainted(false);
        redButton.setBackground(Color.lightGray);
        redButton.setOpaque(false);
        redButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                currentColor = Color.red;
                toolbar.setBackground(currentColor);
                menu.setBackground(currentColor);
            }
        });
        toolbar.add(redButton);
        JButton orangeButton = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("orange.png")));
        orangeButton.setBounds(0, 360, 40, 40);
        orangeButton.setBorderPainted(false);
        orangeButton.setBackground(Color.lightGray);
        orangeButton.setOpaque(false);
        orangeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                currentColor = Color.orange;
                toolbar.setBackground(currentColor);
                menu.setBackground(currentColor);
            }
        });
        toolbar.add(orangeButton);
        JButton yellowButton = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("yellow.png")));
        yellowButton.setBounds(0, 400, 40, 40);
        yellowButton.setBorderPainted(false);
        yellowButton.setBackground(Color.lightGray);
        yellowButton.setOpaque(false);
        yellowButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                currentColor = Color.yellow;
                toolbar.setBackground(currentColor);
                menu.setBackground(currentColor);
            }
        });
        toolbar.add(yellowButton);
        JButton greenButton = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("green.png")));
        greenButton.setBounds(0, 440, 40, 40);
        greenButton.setBorderPainted(false);
        greenButton.setBackground(Color.lightGray);
        greenButton.setOpaque(false);
        greenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                currentColor = Color.green;
                toolbar.setBackground(currentColor);
                menu.setBackground(currentColor);
            }
        });
        toolbar.add(greenButton);
        JButton cyanButton = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("cyan.png")));
        cyanButton.setBounds(0, 480, 40, 40);
        cyanButton.setBorderPainted(false);
        cyanButton.setBackground(Color.lightGray);
        cyanButton.setOpaque(false);
        cyanButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                currentColor = Color.cyan;
                toolbar.setBackground(currentColor);
                menu.setBackground(currentColor);
            }
        });
        toolbar.add(cyanButton);
        JButton blueButton = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("blue.png")));
        blueButton.setBounds(0, 520, 40, 40);
        blueButton.setBorderPainted(false);
        blueButton.setBackground(Color.lightGray);
        blueButton.setOpaque(false);
        blueButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                currentColor = Color.blue;
                toolbar.setBackground(currentColor);
                menu.setBackground(currentColor);
            }
        });
        toolbar.add(blueButton);
        JButton magentaButton = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("magenta.png")));
        magentaButton.setBounds(0, 560, 40, 40);
        magentaButton.setBorderPainted(false);
        magentaButton.setBackground(Color.lightGray);
        magentaButton.setOpaque(false);
        magentaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                currentColor = Color.magenta;
                toolbar.setBackground(currentColor);
                menu.setBackground(currentColor);
            }
        });
        toolbar.add(magentaButton);
        boardPanel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                try {
                    try {
                        String message = currentColor.getRGB() + " " + (e.getX() - brushSize / 2) + " " + (e.getY() - brushSize / 2)
                                + " " + brushSize;
                        writeSocket.write(message + "\n");
                        writeSocket.flush();
                    } catch (IOException err) {
                        System.out.println(err.toString());
                        readSocket.close();
                        writeSocket.close();
                    }
                } catch (IOException err) {
                    System.out.println(err.toString());
                }

            }
        });
        boardPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                try {
                    try {
                        String message = currentColor.getRGB() + " " + (e.getX() - brushSize / 2) + " " + (e.getY() - brushSize / 2)
                                + " " + brushSize;
                        writeSocket.write(message + "\n");
                        writeSocket.flush();
                    } catch (IOException err) {
                        System.out.println(err.toString());
                        readSocket.close();
                        writeSocket.close();
                    }
                } catch (IOException err) {
                    System.out.println(err.toString());
                }
            }
        });
    }
}