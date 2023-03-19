import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;
import java.io.*;

import java.util.Timer;
import java.util.TimerTask;

public class Minesweeper extends JFrame implements MouseListener, ActionListener{
    JPanel buttonPanel;
    // JFrame frame;
    JToggleButton[][] buttons;
    
    JMenuBar menuBar;
    JMenu diffMenu;
    JMenuItem beg, inter, exp;
    JButton resetButton;
    JTextField timerDisplay;

    boolean firstClick = true, gameOver = false;
    int numOfMines = 10, clickR, clickC, clickedCount = 0;
    int dimensR = 9, dimensC = 9;
    int gridScale = 35, menuScale = 55;

    Timer timer;
    int timePassed = 0;
    GraphicsEnvironment ge;
    Font clockFont;

    ImageIcon[] numbers = new ImageIcon[9];
    ImageIcon unopened, flag, flag_wrong, mine, mine_red, smile, win, lose, wait;

    public Minesweeper(){
        loadIcons();

        createMenu();
        setGrid(dimensR,dimensC);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public void loadIcons(){
        for(int i=0; i<numbers.length; i++){
            numbers[i] = new ImageIcon("icons\\type" +i+".png");
            numbers[i] = new ImageIcon(numbers[i].getImage().getScaledInstance(gridScale, gridScale, Image.SCALE_SMOOTH));
        }
        
        unopened = new ImageIcon(new ImageIcon("icons\\closed.png").getImage().getScaledInstance(gridScale,gridScale,Image.SCALE_SMOOTH));
        flag = new ImageIcon(new ImageIcon("icons\\flag.png").getImage().getScaledInstance(gridScale,gridScale,Image.SCALE_SMOOTH));
        flag_wrong = new ImageIcon(new ImageIcon("icons\\flag_wrong.png").getImage().getScaledInstance(gridScale,gridScale,Image.SCALE_SMOOTH));
        mine = new ImageIcon(new ImageIcon("icons\\mine.png").getImage().getScaledInstance(gridScale,gridScale,Image.SCALE_SMOOTH));
        mine_red = new ImageIcon(new ImageIcon("icons\\mine_red.png").getImage().getScaledInstance(gridScale,gridScale,Image.SCALE_SMOOTH));
        
        smile = new ImageIcon(new ImageIcon("icons\\face_smile.png").getImage().getScaledInstance(menuScale,menuScale,Image.SCALE_SMOOTH));
        win = new ImageIcon(new ImageIcon("icons\\face_win.png").getImage().getScaledInstance(menuScale,menuScale,Image.SCALE_SMOOTH));
        lose = new ImageIcon(new ImageIcon("icons\\face_lose.png").getImage().getScaledInstance(menuScale,menuScale,Image.SCALE_SMOOTH));
        wait = new ImageIcon(new ImageIcon("icons\\face_active.png").getImage().getScaledInstance(menuScale,menuScale,Image.SCALE_SMOOTH));

        try {
            ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            clockFont=Font.createFont(Font.TRUETYPE_FONT,new File("fonts\\digital-7.ttf"));
            ge.registerFont(clockFont);
       } catch (IOException|FontFormatException e) {}
    }

    public void setGrid(int rows, int cols){
        if(buttonPanel != null){
            this.remove(buttonPanel);
        }

        buttons = new JToggleButton[rows][cols];
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(rows, cols));

        for(int r=0; r<rows; r++){
            for(int c=0; c<cols; c++){
                buttons[r][c] = new JToggleButton();
                buttons[r][c].setBorder(null);
                buttons[r][c].setIcon(unopened);
                
                buttons[r][c].addMouseListener(this);
                buttons[r][c].putClientProperty("row", r);
                buttons[r][c].putClientProperty("col", c);

                buttons[r][c].putClientProperty("mineVal", 0);

                buttons[r][c].setFont(new Font("Arial", Font.ITALIC, 18));
                buttonPanel.add(buttons[r][c]);
            }
        }

        this.add(buttonPanel);

        this.setSize(cols*gridScale,(rows*gridScale)+50);
    }

    public void createMenu(){
        menuBar = new JMenuBar();
        menuBar.setLayout(new GridLayout(1,3));

        diffMenu = new JMenu("Difficulty");
        beg = new JMenuItem("Beginner");
        inter = new JMenuItem("Intermediate");
        exp = new JMenuItem("Expert");

        beg.addActionListener(this);
        inter.addActionListener(this);
        exp.addActionListener(this);

        diffMenu.add(beg);
        diffMenu.add(inter);
        diffMenu.add(exp);
        menuBar.add(diffMenu, BorderLayout.WEST);

        resetButton = new JButton();
        resetButton.setBorder(null);
        resetButton.setPreferredSize(new Dimension(menuScale, menuScale));
        resetButton.setIcon(smile);
        resetButton.addActionListener(this);
        JPanel p = new JPanel();
        p.add(resetButton);
        p.setBackground(UIManager.getColor(p));
        menuBar.add(p, BorderLayout.CENTER);

        timerDisplay = new JTextField(timePassed);
        timerDisplay.setEditable(false);
        timerDisplay.setFont(clockFont.deriveFont(54f));
        timerDisplay.setText("0");
        timerDisplay.setHorizontalAlignment(JTextField.RIGHT);
        timerDisplay.setForeground(Color.RED);
        timerDisplay.setBackground(Color.BLACK);
        menuBar.add(timerDisplay, BorderLayout.EAST);

        this.add(menuBar, BorderLayout.NORTH);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        clickR = (int)((JToggleButton)e.getComponent()).getClientProperty("row");
        clickC = (int)((JToggleButton)e.getComponent()).getClientProperty("col");
        if(!gameOver){
            if(e.getButton() == MouseEvent.BUTTON1 && buttons[clickR][clickC].isEnabled()){            // left click
                if(firstClick){
                    timer = new Timer();
                    timer.schedule(new UpdateTimer(),0,1000);
                    dropMines(buttons.length,buttons[0].length);
                    firstClick = false;
                }
                int state = (int)buttons[clickR][clickC].getClientProperty("mineVal");
                if(state == -1){
                    gameOver = true;
                    buttons[clickR][clickC].setSelected(false);
                    buttons[clickR][clickC].setIcon(mine_red);
                    buttons[clickR][clickC].setDisabledIcon(mine_red);
                    resetButton.setIcon(lose);
                    timer.cancel();
                    disableBoard();
                }
                else{
                    clickedCount++;
                    expand(clickR, clickC);
                    if(clickedCount == buttons.length*buttons[0].length-numOfMines){
                        gameOver = true;
                        resetButton.setIcon(win);
                        timer.cancel();
                        JOptionPane.showMessageDialog(this, "You won! Good Job!");
                    }
                }
            }
            if(!firstClick && e.getButton() == MouseEvent.BUTTON3){            // right click
                if(!buttons[clickR][clickC].isSelected()){
                    if(buttons[clickR][clickC].getIcon() == unopened){
                        buttons[clickR][clickC].setIcon(flag);
                        buttons[clickR][clickC].setDisabledIcon(flag);
                        buttons[clickR][clickC].setEnabled(false);
                    }
                    else if(buttons[clickR][clickC].getIcon() == flag){
                        buttons[clickR][clickC].setIcon(unopened);
                        buttons[clickR][clickC].setEnabled(true);
                    }
                }
            }             
        }
        revalidate();
    }

    public void disableBoard(){
        for(int i=0; i<buttons.length; i++){
            for(int j=0; j<buttons[0].length; j++){
                buttons[i][j].setDisabledIcon(buttons[i][j].getIcon());
                buttons[i][j].setEnabled(false);

                int state = (int)buttons[i][j].getClientProperty("mineVal");
                if(state == -1 && !(i == clickR && j == clickC) && buttons[i][j].getIcon() != flag){
                    buttons[i][j].setIcon(mine);
                    buttons[i][j].setDisabledIcon(mine);
                } else if(state != -1 && buttons[i][j].getIcon() == flag){
                    buttons[i][j].setIcon(flag_wrong);
                    buttons[i][j].setDisabledIcon(flag_wrong);   
                }

            }
        }
    }

    public void expand(int row, int col){
        if(!buttons[row][col].isSelected()){
            buttons[row][col].setSelected(true);
            clickedCount++;
        }

        int state = (int)buttons[row][col].getClientProperty("mineVal");
        if(state > 0)
            buttons[row][col].setIcon(numbers[state]);
        else{
            buttons[row][col].setIcon(numbers[0]);
            for(int i=row-1; i<=row+1; i++){
                for(int j=col-1; j<=col+1; j++){
                    try{
                        // state = (int)(buttons[i][j].getClientProperty("mineVal"));
                        if(!buttons[i][j].isSelected())
                            expand(i,j);
                    }catch(Exception e){}
                }
            }
        }
    }

    public void dropMines(int rows, int cols){
        int count = numOfMines;
        while(count > 0){
            int r = (int)(Math.random()*rows);
            int c = (int)(Math.random()*cols);
            if(r != clickR && c != clickC && (int)buttons[r][c].getClientProperty("mineVal") == 0){
                if(r > clickR+1 || r < clickR-1 && c > clickC+1 || c < clickC-1){
                    buttons[r][c].putClientProperty("mineVal", -1);
                    count--;
                }
            }

            // if(r != clickR && c != clickC && (int)buttons[r][c].getClientProperty("mineVal") == 0){
            //     System.out.println(r +" "+ c);
            //     buttons[r][c].putClientProperty("mineVal", -1);
            //     count--;
            // }
        }

        for(int r=0; r<rows; r++){
            for(int c=0; c<cols; c++){
                int state = (int)(buttons[r][c].getClientProperty("mineVal"));
                if(state != -1){
                    count = 0;
                    for(int i=r-1; i<=r+1; i++){
                        for(int j=c-1; j<=c+1; j++){
                            try{
                                state = (int)(buttons[i][j].getClientProperty("mineVal"));
                                if(state == -1)
                                    count++;
                            }catch(Exception e){}
                        }
                    }
                    buttons[r][c].putClientProperty("mineVal", count);
                }
            }
        }

        // for(int r=0; r<rows; r++){
        //     for(int c=0; c<cols; c++){
        //         String s = (buttons[r][c].getClientProperty("mineVal")).toString();
        //         buttons[r][c].setText(s);
        //     }
        // }
    
        this.revalidate();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == beg){
            dimensR = 9;
            dimensC = 9;
            numOfMines = 10;
        }
        if(e.getSource() == inter){
            dimensR = 16;
            dimensC = 16;
            numOfMines = 40;
        }
        if(e.getSource() == exp){
            dimensR = 16;
            dimensC = 40;
            numOfMines = 99;
        }

        resetBoard();
        
        if(timer!=null)
		    timer.cancel();
        timePassed=0;
        timerDisplay.setText("   "+timePassed);
        
        revalidate();
    }   

    public void resetBoard(){
        this.remove(buttonPanel);
        resetButton.setIcon(wait);
        setGrid(dimensR, dimensC);
        firstClick = true;
        gameOver = false;
        clickedCount = 0;
        resetButton.setIcon(smile);
    }

    public static void main(String[] args){
        Minesweeper ms = new Minesweeper();
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}  

    @Override
    public void mouseExited(MouseEvent e){}

    public class UpdateTimer extends TimerTask {
		public void run() {
			if(!gameOver){
				timePassed++;
				timerDisplay.setText("  "+timePassed);
			}
		}
	}
}