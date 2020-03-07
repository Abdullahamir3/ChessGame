import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



public class GUIChess extends JFrame {
    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;

    private Player turn;
    private Board board = new Board();

    private JButton[][] buttons = new JButton[8][8];
    private Tile lastTile;

    private ChessTimer whiteTimer;
    private ChessTimer blackTimer;

    private JLabel turnLabel;
    private JLabel whiteTimeLabel;
    private JLabel blackTimeLabel;

    public GUIChess() {
        setTitle("Chess");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        createContent();
        newGame();
        startTimer();


        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ------------------------------------------------------------------------
    // Game Initialization
    // ------------------------------------------------------------------------

    private void newGame() {
        turn = Player.WHITE;
        board.initialize();
        refresh();

        whiteTimer = new ChessTimer();
        blackTimer = new ChessTimer();
        resumeTimer();
        updateLabels();
    }

    // ------------------------------------------------------------------------
    // GUI Setup
    // ------------------------------------------------------------------------

    private void createContent() {
        createMenu();

        // --------------------------------------------------------------------
        // Main Grid (Board Buttons)
        // --------------------------------------------------------------------

        JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayout(8, 8));
        add(panel1);

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                JButton button = new JButton();
                button.setFont(new Font("Arial Unicode MS", Font.BOLD, 25));
                button.addActionListener(new ButtonListener(rank, file));

                panel1.add(button);
                buttons[rank][file] = button;
            }
        }

        // --------------------------------------------------------------------
        // Status Bar (Timers, Current Turn Label)
        // --------------------------------------------------------------------

        JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayout(1, 3));
        add(panel2, BorderLayout.SOUTH);

        JPanel panel3 = new JPanel();
        JPanel panel4 = new JPanel();
        JPanel panel5 = new JPanel();

        panel2.add(panel3);
        panel2.add(panel4);
        panel2.add(panel5);

        whiteTimeLabel = new JLabel();
        panel3.add(new JLabel("White time:"));
        panel3.add(whiteTimeLabel);

        turnLabel = new JLabel();
        panel4.add(turnLabel);

        blackTimeLabel = new JLabel();
        panel5.add(new JLabel("Black time:"));
        panel5.add(blackTimeLabel);
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu menu = new JMenu("Game");
        menuBar.add(menu);

        // --------------------------------------------------------------------
        // New Game
        // --------------------------------------------------------------------

        JMenuItem newGame = new JMenuItem("New Game");
        menu.add(newGame);
        newGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newGame();
            }
        });

        menu.addSeparator();

        // --------------------------------------------------------------------
        // Saving and Loading
        // --------------------------------------------------------------------

        final GameSerializer serializer = new GameSerializer();

        JMenuItem loadGame = new JMenuItem("Load Game");
        menu.add(loadGame);
        loadGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();

                pauseTimers();

                if (chooser.showOpenDialog(GUIChess.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        GameState state = serializer.load(chooser.getSelectedFile());

                        turn = state.turn;
                        board = state.board;
                        refresh();

                        whiteTimer = new ChessTimer(state.whiteTime);
                        blackTimer = new ChessTimer(state.blackTime);
                        updateLabels();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(GUIChess.this, ex.getMessage());
                    }
                }

                resumeTimer();
            }
        });

        JMenuItem saveGame = new JMenuItem("Save Game");
        menu.add(saveGame);
        saveGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();

                pauseTimers();

                if (chooser.showSaveDialog(GUIChess.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        serializer.save(chooser.getSelectedFile(), new GameState(board, turn, whiteTimer.getElapsed(), blackTimer.getElapsed()));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(GUIChess.this, ex.getMessage());
                    }
                }

                resumeTimer();
            }
        });
    }

    private JButton getButtonAt(Tile t) {
        return buttons[t.getRank()][t.getFile()];
    }

    // ------------------------------------------------------------------------
    // Timers
    // ------------------------------------------------------------------------

    private void startTimer() {
        new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                whiteTimer.update();
                blackTimer.update();

                updateLabels();
            }
        }).start();
    }

    private void pauseTimers() {
        whiteTimer.pause();
        blackTimer.pause();
    }

    private void resumeTimer() {
        if (turn == Player.WHITE) {
            whiteTimer.resume();
        } else {
            blackTimer.resume();
        }
    }

   
    //promotion method
    private String Promotion(){
   
   
    Object selectedValue =null;
   
    
    // if the user press cancel
    while(selectedValue==null){
    Object[] possibleValues = {"Queen", "Knight", "Rook","Bishop"};
     
     
    selectedValue = JOptionPane.showInputDialog(null,
                "Choose one to promote the pawn", "Input",
                JOptionPane.INFORMATION_MESSAGE, null,
                possibleValues, possibleValues[0]);
    }
     
       
    return selectedValue.toString();
       
    }

    private void refresh() {
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Tile t = new Tile(rank, file);
                Piece jerk = board.getPieceAt(t);

                if ((rank + file) % 2 == 0) {
                    getButtonAt(t).setBackground(new Color(255, 255, 200));
                } else {
                    getButtonAt(t).setBackground(new Color(200, 200, 255));
                }

                getButtonAt(t).setText(jerk == null ? null : jerk.toString());
            }
        }

        turnLabel.setText("(" + turn.toString() + " to move)");
    }

    private void updateLabels() {
        int w = (int) (whiteTimer.getElapsed() / 1e3);
        int b = (int) (blackTimer.getElapsed() / 1e3);

        whiteTimeLabel.setText(String.format("%d:%02d", w / 60, w % 60));
        blackTimeLabel.setText(String.format("%d:%02d", b / 60, b % 60));
    }

    // ------------------------------------------------------------------------
    // Selection and Movement
    // ------------------------------------------------------------------------

    private class ButtonListener implements ActionListener {
        private Tile thisTile;

        public ButtonListener(int rank, int file) {
            thisTile = new Tile(rank, file);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (lastTile == null) {
                if (board.isOccupiedByPlayer(thisTile, turn)) {
                    lastTile = thisTile;

                    for (Tile tile : board.getPieceAt(thisTile).getAllSafeMoves(thisTile)) {
                        if (board.isOccupied(tile)) {
                            getButtonAt(tile).setBackground(new Color(150, 100, 100));
                        } else {
                            getButtonAt(tile).setBackground(new Color(100, 150, 100));
                        }
                    }

                    getButtonAt(thisTile).setBackground(Color.green);
                }
            } else {
                if (board.move(lastTile, thisTile)) {
                
                  pauseTimers();
                //to decide when to promote
                if (thisTile.getRank()==0||thisTile.getRank()==7){
                if (board.getPieceAt(thisTile).getClass().getName().equals("Pawn")){
               
                String choice=Promotion();
               
               //the choices
                if (choice.equals("Queen") && thisTile.getRank()==0)
                board.setPieceAt(thisTile, new Queen(board,Player.WHITE));
               
                else if (choice.equals("Queen") && thisTile.getRank()==7)
                    board.setPieceAt(thisTile, new Queen(board,Player.BLACK));
               
                else if (choice.equals("Knight") && thisTile.getRank()==0)
                board.setPieceAt(thisTile, new Knight(board,Player.WHITE));
               
                else if (choice.equals("Knight") && thisTile.getRank()==7)
                    board.setPieceAt(thisTile, new Knight(board,Player.BLACK));
               
                else if (choice.equals("Rook") && thisTile.getRank()==0)
                board.setPieceAt(thisTile, new Rook(board,Player.WHITE));
               
                else if (choice.equals("Rook") && thisTile.getRank()==7)
                    board.setPieceAt(thisTile, new Rook(board,Player.BLACK));
               
                else if (choice.equals("Bishop") && thisTile.getRank()==0)
                    board.setPieceAt(thisTile, new Bishop(board,Player.WHITE));
                   
                else if (choice.equals("Bishop") && thisTile.getRank()==7)
                        board.setPieceAt(thisTile, new Bishop(board,Player.BLACK));
                }
                else{}
                }
                 
                    turn = turn.opposite();

                    if (board.isPlayerInCheckMate(turn)) {
                        JOptionPane.showMessageDialog(GUIChess.this, "Checkmate!");
                        newGame();
                    } else if (board.isPlayerInCheck(turn)) {
                        JOptionPane.showMessageDialog(GUIChess.this, "Check.");
                    }
                   
                    resumeTimer();
                }

                refresh();
                lastTile = null;
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }

        new GUIChess();
    }
}

