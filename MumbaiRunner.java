import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;
import javax.swing.*;

public final class MumbaiRunner extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MumbaiRunner::new);
    }

    public MumbaiRunner() {

    setTitle("Mumbai Runner");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    setExtendedState(JFrame.MAXIMIZED_BOTH); // ✅ fullscreen
    setUndecorated(true);                    // ✅ remove title bar

    add(new GamePanel());

    setVisible(true);
}
}

// ================= GAME PANEL =================
final class GamePanel extends JPanel implements ActionListener {

    private int obstaclesInLane(int lane){

    int count = 0;

    for(Obstacle o : obstacles){
        if(o.lane == lane){
            count++;
        }
    }

    return count;
}

    private void triggerGameOver(){

    gameOver = true;
    timer.stop();

    if(clip!=null) clip.stop();

    if(gameOverClip!=null){
        gameOverClip.stop();
        gameOverClip.setFramePosition(0);
        gameOverClip.start();
    }

    restartButton.setVisible(true);
}

    Toolkit toolkit = Toolkit.getDefaultToolkit();
Dimension screen = toolkit.getScreenSize();

private final int PANEL_WIDTH = screen.width;
private final int PANEL_HEIGHT = screen.height;

    private enum Screen {
        TITLE, INTRO, HOW_TO_PLAY, GAME, COMPLETE
    }

    private Screen currentScreen = Screen.TITLE;

    private final int LANE_COUNT = 3;
private final int ROAD_WIDTH = 900;
private final int ROAD_LEFT =
        (PANEL_WIDTH - ROAD_WIDTH) / 2;
private final int LANE_WIDTH =
        ROAD_WIDTH / LANE_COUNT;

    private final int PLAYER_WIDTH = 150;
private final int PLAYER_HEIGHT = 220;
private final int OBSTACLE_WIDTH = 150;
private final int OBSTACLE_HEIGHT = 150;
private final int COIN_SIZE = 80;

    private int playerLane = 1;
    private int playerPosX;
    private int playerPosY;
    private int jumpHeight = 0;
    private boolean isJumping = false;
    private boolean isDucking = false;
    private int duckTimer = 0;

    private boolean gameStarted = false;
    private boolean gameOver = false;

    // ===== COUNTDOWN =====
private boolean countdownActive = false;
private int countdownTimer = 0;
private String countdownText = "";

    private int coins = 0;
    private int gameTime = 0;

    private boolean hardMode = false;

    private Image background, titleImage, finalImg;
private Image introImage, howToPlayImage;
private Image coinImg, potholeImg,
    trafficImg, autoImg, barricadeImg,
    playerImg, jumpImg, duckImg;

    private final JButton nextButton = new JButton("NEXT");
    private final JButton startButton = new JButton("START GAME");
    private final JButton restartButton = new JButton("RESTART");

    private  Timer timer;
    private final Random random = new Random();

    private final ArrayList<Obstacle> obstacles = new ArrayList<>();
    private final ArrayList<Coin> coinList = new ArrayList<>();

    private Clip clip, gameOverClip, coinClip;

    public GamePanel() {

        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setLayout(null);
        setFocusable(true);

        loadImages();
        loadMusic();
        loadCoinSound();
        loadGameOverSound();

        nextButton.setBounds(600,650,200,50);
        add(nextButton);

        nextButton.addActionListener(e->{
            if(currentScreen==Screen.TITLE)
                currentScreen=Screen.INTRO;
            else if(currentScreen==Screen.INTRO){
                currentScreen=Screen.HOW_TO_PLAY;
                nextButton.setVisible(false);
                startButton.setVisible(true);
            }
            repaint();
        });

        startButton.setBounds(600,650,200,50);
        startButton.setVisible(false);
        add(startButton);

        startButton.addActionListener(e->{
    currentScreen = Screen.GAME;
    startButton.setVisible(false);

    countdownActive = true;
    countdownTimer = 0;

    timer.start();   // start timer ONLY for countdown
    requestFocusInWindow();
});

        restartButton.setBounds(600,500,200,50);
        restartButton.setVisible(false);
        add(restartButton);

        restartButton.addActionListener(e->{
            restartButton.setVisible(false);
            startGame();
        });

        addKeyListener(new KeyAdapter(){
            @Override
public void keyPressed(KeyEvent e){

                if(!gameStarted||gameOver) return;

                switch(e.getKeyCode()){
                    case KeyEvent.VK_ESCAPE -> {
    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(GamePanel.this);

    frame.dispose();                 // destroy fullscreen
    frame.setUndecorated(false);     // restore window bar
    frame.setExtendedState(JFrame.NORMAL);
    frame.setVisible(true);
}
                    case KeyEvent.VK_LEFT ->
                            playerLane=Math.max(0,playerLane-1);
                    case KeyEvent.VK_RIGHT ->
                            playerLane=Math.min(2,playerLane+1);
                    case KeyEvent.VK_UP ->
                            isJumping=true;
                            case KeyEvent.VK_DOWN -> {
                  if(!isJumping && !isDucking){
                  isDucking = true;
                  duckTimer = 45; // duck duration
    }
}
                }
            }
        });

        timer=new Timer(20,this);
    }

    private void loadImages(){
        background=new ImageIcon("assets/background.png").getImage();
        titleImage=new ImageIcon("assets/title.png").getImage();
        introImage=new ImageIcon("assets/introduction.png").getImage();
        howToPlayImage=new ImageIcon("assets/howtoplay.png").getImage();
        finalImg = new ImageIcon("assets/finalimg.png").getImage();

        coinImg=new ImageIcon("assets/coin.png").getImage();
        potholeImg=new ImageIcon("assets/pothole.png").getImage();
        trafficImg=new ImageIcon("assets/traffic.png").getImage();
        autoImg=new ImageIcon("assets/autorickshaw.png").getImage();
        barricadeImg=new ImageIcon("assets/barricades.png").getImage();
        playerImg = new ImageIcon("assets/run.png").getImage();
         jumpImg = new ImageIcon("assets/jump.png").getImage(); 
         duckImg = new ImageIcon("assets/duck.png").getImage();
    }

    private void loadMusic(){
        try{
            AudioInputStream audio=
                    AudioSystem.getAudioInputStream(
                            new File("assets/background_music.wav"));
            clip=AudioSystem.getClip();
            clip.open(audio);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }catch(IOException | UnsupportedAudioFileException |
               LineUnavailableException ignored){}
    }

    private void loadCoinSound(){
        try{
            AudioInputStream audio=
                    AudioSystem.getAudioInputStream(
                            new File("assets/coinmusic.wav"));
            coinClip=AudioSystem.getClip();
            coinClip.open(audio);
        }catch(IOException | UnsupportedAudioFileException |
               LineUnavailableException ignored){}
    }

    private void loadGameOverSound(){
        try{
            AudioInputStream audio=
                    AudioSystem.getAudioInputStream(
                            new File("assets/gameover.wav"));
            gameOverClip=AudioSystem.getClip();
            gameOverClip.open(audio);
        }catch(IOException | UnsupportedAudioFileException |
               LineUnavailableException ignored){}
    }

    private void startGame(){

        if(clip!=null) clip.start();

        coins=0;
        gameTime=0;
        hardMode=false;
        gameOver=false;

        playerPosY=PANEL_HEIGHT-200;

        obstacles.clear();
        coinList.clear();

        timer.start();
    }

    private boolean laneFree(int lane){

    for(Obstacle o : obstacles){

        // same lane check
        if(o.lane == lane){

            // minimum gap between obstacles
            int safeGap = hardMode ? 400 : 300;

            if(Math.abs(o.y - (-100)) < safeGap){
                return false;
            }
        }
    }
    return true;
}

private boolean rowBlocked(int spawnY){

    int sameRowCount = 0;

    for(Obstacle o : obstacles){

        // check obstacles close in vertical position
        if(Math.abs(o.y - spawnY) < 700){
            sameRowCount++;
        }
    }

    // if already 2 obstacles in same row
    // do not allow third
    return sameRowCount >= 2;
}

    @Override
    public void actionPerformed(ActionEvent e){

        // ===== COUNTDOWN SYSTEM =====
if(countdownActive){

    countdownTimer++;

    if(countdownTimer < 50)
    countdownText = "3";
else if(countdownTimer < 100)
    countdownText = "2";
else if(countdownTimer < 150)
    countdownText = "1";
else if(countdownTimer < 200)
    countdownText = "GO!";

    else{
        countdownActive = false;
        gameStarted = true;
        startGame();   // actual game begins
    }

    repaint();
    return;
}

        if(!gameStarted||gameOver) return;

        gameTime++;

        if(!hardMode && gameTime >= 2250)
    hardMode = true;

       // ✅ LEVEL COMPLETE AFTER 90 SECONDS
if(gameTime >= 4500){
            timer.stop();
            if(clip!=null) clip.stop();
            currentScreen = Screen.COMPLETE;
            repaint();
            return;
        }

        if(isJumping){
            jumpHeight+=8;
            if(jumpHeight>=240) isJumping=false;
        }else if(jumpHeight>0)
            jumpHeight-=8;

            // ===== DUCK SYSTEM =====
if(isDucking){
    duckTimer--;
    if(duckTimer <= 0){
        isDucking = false;
    }
}

       playerPosX =
        ROAD_LEFT +
        playerLane * LANE_WIDTH +
        LANE_WIDTH / 2 -
        PLAYER_WIDTH / 2;

       if(random.nextInt(45)==0){

    int lane = random.nextInt(3);
    int spawnY = -100;

    if(
        laneFree(lane)
        && obstaclesInLane(lane) < 2
        && !rowBlocked(spawnY)   // ✅ NEW CONDITION
    ){
        obstacles.add(new Obstacle(lane, spawnY));
    }
}


        if(random.nextInt(70)==0)
            coinList.add(new Coin(random.nextInt(3),-100));

        obstacles.forEach(Obstacle::move);
        // ✅ remove obstacles that passed screen
obstacles.removeIf(o ->
        o.y > PANEL_HEIGHT);
        coinList.forEach(Coin::move);
        coinList.removeIf(c ->
        c.y > PANEL_HEIGHT);

       Rectangle playerRect =
    new Rectangle(
        playerPosX + 70,
        playerPosY - jumpHeight + 100,
        PLAYER_WIDTH - 140,
        PLAYER_HEIGHT - 140
);

        coinList.removeIf(c->{
            Rectangle r=new Rectangle(c.x,c.y,COIN_SIZE,COIN_SIZE);
            if(playerRect.intersects(r)){
                coins+=hardMode?2:1;

                if(coinClip!=null){
                    coinClip.stop();
                    coinClip.setFramePosition(0);
                    coinClip.start();
                }
                return true;
            }
            return false;
        });

       for(Obstacle o : obstacles){

    // ✅ check ONLY same lane
    if(o.lane != playerLane){
        continue;
    }

    int playerHeadY =
            playerPosY - jumpHeight;

    int playerFeetY =
            playerHeadY + PLAYER_HEIGHT;

            // ✅ obstacle fully crossed player
if(o.y > playerFeetY){
    continue;
}
 boolean verticalHit =
        o.y + OBSTACLE_HEIGHT > playerHeadY &&
        o.y < playerFeetY;

if(!verticalHit){
    continue;
}

    int potholeTop = o.y;
    int potholeBottom = o.y + OBSTACLE_HEIGHT;

    
    if(o.type == 0){

        // player enters danger zone ONLY when feet inside pothole
        boolean insidePothole =
                playerFeetY > potholeTop + 100 &&
                playerFeetY < potholeBottom - 20;

        // SAFE jump clearance
        if(jumpHeight > 100){
            continue; // jumped safely ✅
        }

        if(insidePothole){
            triggerGameOver();
        }
    }

    // ================= OTHER OBSTACLES =================
    else{

    if(o.type == 3){
        // safely ducked
        if(isDucking){
            continue;
        }

        Rectangle obstacleRect =
                new Rectangle(o.x, o.y,
                OBSTACLE_WIDTH,
                OBSTACLE_HEIGHT);

        if(playerRect.intersects(obstacleRect)){
            triggerGameOver();
        }
    }

    // ===== NORMAL OBSTACLES =====
    else{
        Rectangle obstacleRect =
                new Rectangle(o.x, o.y,
                OBSTACLE_WIDTH,
                OBSTACLE_HEIGHT);

        if(playerRect.intersects(obstacleRect)){
            triggerGameOver();
        }
    }
}
}

                

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g;

        if(currentScreen==Screen.COMPLETE){
            // ===== FINAL LEVEL COMPLETE SCREEN =====
g2.drawImage(finalImg, 0, 0,
        PANEL_WIDTH, PANEL_HEIGHT, this);

// ---------- LEVEL COMPLETED TEXT ----------
String title = "LEVEL COMPLETED!";

Font titleFont = new Font("Arial", Font.BOLD, 80);
g2.setFont(titleFont);
g2.setColor(Color.WHITE);

FontMetrics fm1 = g2.getFontMetrics();

int titleX =
        (PANEL_WIDTH - fm1.stringWidth(title)) / 2;

int titleY = PANEL_HEIGHT / 2 - 40;

g2.drawString(title, titleX, titleY);

// ---------- FINAL COINS SCORE ----------
String scoreText =
        "FINAL COINS SCORE: " + coins;

Font scoreFont =
        new Font("Arial", Font.BOLD, 45);

g2.setFont(scoreFont);

FontMetrics fm2 = g2.getFontMetrics();

int scoreX =
        (PANEL_WIDTH - fm2.stringWidth(scoreText)) / 2;

int scoreY = PANEL_HEIGHT / 2 + 50;

g2.drawString(scoreText, scoreX, scoreY);

return;
        }

        if(currentScreen==Screen.TITLE){
            g2.drawImage(titleImage,0,0,PANEL_WIDTH,PANEL_HEIGHT,this);
            return;
        }

        if(currentScreen==Screen.INTRO){
            g2.drawImage(introImage,0,0,PANEL_WIDTH,PANEL_HEIGHT,this);
            return;
        }

        if(currentScreen==Screen.HOW_TO_PLAY){
            g2.drawImage(howToPlayImage,0,0,PANEL_WIDTH,PANEL_HEIGHT,this);
            return;
        }

        g2.drawImage(background,0,0,PANEL_WIDTH,PANEL_HEIGHT,this);

        // ===== DRAW COUNTDOWN =====
if(countdownActive){
    g2.setColor(new Color(0,0,0,180));
    g2.fillRect(0,0,PANEL_WIDTH,PANEL_HEIGHT);

    g2.setColor(Color.WHITE);
    g2.setFont(new Font("Arial",Font.BOLD,120));

    FontMetrics fm = g2.getFontMetrics();
    int x = (PANEL_WIDTH - fm.stringWidth(countdownText))/2;

    g2.drawString(countdownText, x, PANEL_HEIGHT/2);
    return;
}

       Image currentPlayer;

if(isDucking)
    currentPlayer = duckImg;
else if(jumpHeight > 0)
    currentPlayer = jumpImg;
else
    currentPlayer = playerImg;

    int drawHeight =
        isDucking ? PLAYER_HEIGHT - 100 : PLAYER_HEIGHT;

g2.drawImage(currentPlayer,
        playerPosX,
        playerPosY - jumpHeight +
            (isDucking ? 80 : 0),
        PLAYER_WIDTH,
        drawHeight,
        this);

        for(Obstacle o:obstacles){

    Image img;
    int width = OBSTACLE_WIDTH;
    int height = OBSTACLE_HEIGHT;

    switch(o.type){
        case 0 -> img = potholeImg;

        case 1 -> {   // ✅ Autorickshaw
            img = autoImg;
            width = 170;     // increase size
            height = 190;
        }

        case 2 -> img = trafficImg;

        default -> img = barricadeImg;
    }

    g2.drawImage(img, o.x, o.y, width, height, this);
}

        for(Coin c:coinList)
            g2.drawImage(coinImg,c.x,c.y,
                    COIN_SIZE,COIN_SIZE,this);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial",Font.BOLD,24));
        g2.drawString("Coins: "+coins,20,40);
        g2.drawString("Time: "+gameTime/50+" s",20,80);

        if(gameOver){
            g2.setColor(new Color(0,0,0,180));
            g2.fillRect(0,0,PANEL_WIDTH,PANEL_HEIGHT);

            g2.setColor(Color.RED);
Font font = new Font("Arial", Font.BOLD, 80);
g2.setFont(font);

FontMetrics fm = g2.getFontMetrics();

String text = "GAME OVER";

int x = (PANEL_WIDTH - fm.stringWidth(text)) / 2;
int y = PANEL_HEIGHT / 2;

g2.drawString(text, x, y);
        }
    }

    private final class Obstacle{
        int x,y,lane,type,speed;
        Obstacle(int lane,int y){
            this.lane=lane;
            this.y=y;
            this.type=random.nextInt(4);
            speed=hardMode?12:8;
            updateX();
        }
        void updateX(){
            x=ROAD_LEFT+
                    lane*LANE_WIDTH+
                    LANE_WIDTH/2-
                    OBSTACLE_WIDTH/2;
        }
        void move(){ y+=speed; }
    }

    private final class Coin{
        int x,y;
        Coin(int lane,int y){
            x=ROAD_LEFT+
                    lane*LANE_WIDTH+
                    LANE_WIDTH/2-
                    COIN_SIZE/2;
            this.y=y;
        }
        void move(){y+=8;}
    }
}

