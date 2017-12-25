import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;

public class Game implements Runnable{

    public static int               WEIDTH              = 1360;
    public static int               HIGHT               = 700;
    public static String            TITLE               = "Balls";
    public static int               CLEAR_COLOR         = 0xff00ffff;
    public static int               NUMBER_BUFFERS      = 4;
    public static final float       UPDATE_RATE         = 60.0f;
    public static final float       UPDATE_INTERVAL     = Time.SECOND/UPDATE_RATE;
    public static final long        IDLE_TIME           = 1;
    public static final String      ATLAS_FILE_NAME     = "textur_atlas.png";   
    private static final float      MIN_BALL_Level      = 0.25f;

    private boolean running;
    private Thread gameThread;
    private Render renderTread;
    private java.awt.Graphics2D graphics;
    //private TextureAtlas atlas;
    private static Player player;
    private Arrow arrow;
    private int fps = 0;
    Image image;
    private volatile boolean render;
    float level=5;   

    private List <Ball> balls = new CopyOnWriteArrayList<Ball>();
    private List<Entity> entitys = new CopyOnWriteArrayList<>();

    public Game(){
        running = false;
        Graphic.create(TITLE, WEIDTH, HIGHT, CLEAR_COLOR, NUMBER_BUFFERS, false);
        graphics = Graphic.getGraphics();
        Graphic.addInputListener(getKeyListener());
        //atlas = new TextureAtlas(ATLAS_FILE_NAME);
        entitys.add(new Interfase(this));
        arrow = new Arrow();
        entitys.add(arrow);
        player = new Player();
        entitys.add(player);    
        //level = new Level(atlas);
        newBall(0,(int)(400-level*40),level,3,0);//start_ball
        
        try {
			image = ImageIO.read(new File("Car.jpg"));
		} catch (IOException e) {e.printStackTrace();	}
    }

    public synchronized void start(){
        if(running)
            return;
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
        renderTread = new Render();
        renderTread.start();
    }

    public synchronized void stop(){
        if (!running)
            return;
        running = false;
        try {
            gameThread.join();
        }catch (InterruptedException e){
            System.err.println("Join gameThread: ");
            e.printStackTrace();
        }
        cleanUp();
    }

    private void update(){
        for (Ball ball : balls) {
            ball.aplay();
            CollisionBall(ball);
        }
        player.aplay();
        arrow.aplay();;
    }

    private class Render extends Thread {

        @Override
        public void run() {

            while (true) {
                    Graphic.clear();
                    entitys.stream().forEach(x -> x.render(graphics));//.filter()
                    Graphic.swapBuffers();
                    fps++;
            }
        }
    }

    private void render(){

    }

    private void cleanUp(){
        Graphic.destroy();
    }

    private boolean isCollisionBall(Ball ball, String parm){

        int ballX = ball.getX();
        int ballY = ball.getY();
        int ballXD = ballX + ball.getD();
        int ballYD = ballY + ball.getD();

        if(parm.equals("arow"))
            return ballX < arrow.getX() && arrow.getX() < ballXD
                    && ballY < arrow.getY() + arrow.getH() && arrow.getY() < ballYD;
        else
            return ballX < player.getX() + player.getWINDTH()
                    && player.getX() < ballXD && player.getY() < ballYD;
    }

    private void CollisionBall(Ball ball){
        if(ball.isTurn()) {
            if (isCollisionBall(ball, "arow")) {
                respawn(ball);
                removeBall(ball,true);
            } else if (isCollisionBall(ball, ""))
                removeBall(ball,false);
        } else ball.doTurn();
    }

    public void run() {
        fps = 0;
        int upd = 0;
        int updl = 0;
        long count = 0;
        float delta = 0;
        long lastTime = Time.get();

        while (running) {

            long now = Time.get();
            long elapsedTime = now - lastTime;
            lastTime = now;
            count += elapsedTime;
            delta += (elapsedTime / UPDATE_INTERVAL);

            while (delta > 1) {
                update();
                upd++;
                delta--;
            }

            if (count >= Time.SECOND) {
                Graphic.setTitle(TITLE + " || Fps: " + fps + " | Upd: " + upd  + "| " + entitys.size());
                upd = 0;
                fps = 0;
                updl = 0;
                count = 0;
            }
        }
    }

    private void respawn(Ball ball) {
    	float ballLevel=ball.level/2;    	
        if(ballLevel > MIN_BALL_Level) {
            newBall(ball.getX(), ball.getY(),ballLevel, 3, ball.getSpeed());
            newBall(ball.getX(), ball.getY(),ballLevel, -3, ball.getSpeed());
        }
    }

    private Ball newBall(int x, int y, float level, int direction, int speed){
    	Ball ball = new Ball(x, y, level, direction, speed);
        balls.add(ball);
        entitys.add(ball);
        System.out.println("P="+ball.y+" "+ball.d);
        return ball;
    }

    private void removeBall(Ball ball, boolean balles){
       if(balles) { 
    	   balls.remove(ball);    
    	   entitys.remove(ball);
    	   if(balls.size()==0) {
    		   level++;
    		   newBall(0, (int)(400-level*40), level, 3, 0);  }
       } else { 
    	   //balls.removeAll(ball);
    	   
       }
    }

    public KeyListener getKeyListener(){
        return new KeyListener() {
            public void keyPressed(KeyEvent arg0) {
            	// System.out.println("P="+arg0.getKeyCode());
                switch(arg0.getKeyCode()){
                    //case 82:{newBall(0,400,40,3,0); break;}
                    case 37:{player.setLeft(); break;}
                    case 39:{player.setRight(); break;}
                    case 32:{arrow.fire(player.getX()); break;}
                }}
            public void keyReleased(KeyEvent arg0) {
                switch(arg0.getKeyCode()){
                    case 37:{player.setNon(); break;}
                    case 39:{player.setNon(); break;}
                }}
            public void keyTyped(KeyEvent arg0) {
            	//System.out.println("T="+arg0.getKeyChar());
            }
        };
    }

    public int getCountBall(){
        return balls.size();
    }
}
class Ball extends Entity implements Serializable{

    
	private static final int        ACCELERATION            = 1;
    private static final int        END_WINDOW_Y            = Game.HIGHT;
    private static final int        END_WINDOW_X            = Game.WEIDTH;
    private static final int        START_WINDOW_X          = 0;
    private final int constD=40;
    int d;
    private int speed;
    private int direction = 3;
    private int oldX;
    private boolean turn = false;
    public float level;
    public void aplay(){
        setSpeed(speed + ACCELERATION);
        setY(y + speed);
        setX(x + direction);
    }

    public Ball(int x, int y, float level, int direction, int speed) {
        super(x,y);
        this.level=level;
        this.d = (int)(level*constD);
        this.direction = direction;
        this.speed = speed;
        oldX = x;
    }

    void doTurn(){
        if(Math.abs(oldX - x) > d*2)
            turn = true;
    }

    @Override //Лучше через конструктор клонирования
    protected Ball clone() {
        return new Ball(x, y, d, direction, speed);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        if(x > END_WINDOW_X - d || x < START_WINDOW_X) {
            reDirection();
            setX(x + direction);
        }else
            this.x = x;
    }

    private void reDirection(){
        direction *= -1;
    }

    private void reSpeed(){
        speed *= -1;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        if(y > END_WINDOW_Y - d){
            reSpeed();
            setY(END_WINDOW_Y - d);
        } else {
            this.y = y;
        }
    }

    public int getD() {
        return d;
    }

    public void setD(int d) {
        this.d = d;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public boolean isTurn() {
        return turn;
    }


    public void render(Graphics2D g) {
            g.setColor(new Color(0xff0000ff));
            g.fillOval(x, y, d, d);

    }
}
class Arrow extends Entity{

    private static final int        HALF_PLAYER             = 13;
    private static final int        SPEAD                   = 10;
    private static final int        START_Y                 =  650;
    private static final int        HEDTH                   = 400;

    private boolean fire = false;

    protected Arrow(){
        super(-100, START_Y);
    }

    protected Arrow(int x, int y) {
        super(x, y);
    }

    public void fire(int x) {
        fire = true;
        this.x = x + HALF_PLAYER;
        this.y = START_Y;
    }

    public void aplay(){
        if(fire)
            setY(y - SPEAD);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
        if (y < - HEDTH)
            fire = false;
    }

    public int getH() {
        return HEDTH;
    }

    public boolean isFire() {
        return fire;
    }

    public void setFire(boolean fire) {
        this.fire = fire;
    }

    public void render(Graphics2D g) {
            g.setColor(new Color(0xff00ff00));
            g.fillRoundRect(x, y, 2, HEDTH, 10, 10);
    }
}
class Player extends Entity{

    private static final int        Y                   = 650;
    private static final int        WINDTH              = 30;
    private static final int        HIGHT               = 60;
    private static final int        SPEAD               = 12;
    private static final int        START_WINDOW        = 0;
    private static final int        END_WINDOW          = Game.WEIDTH;

    Heading heading;


    private enum Heading {
        LEFT,
        RITE,
        NON
    }

    protected Player(){
        super(600, Y);
    }

    protected Player(int x, int y) {
        super(x, y);
    }


    public void aplay(){
        if(isLeft())
            setX(x - SPEAD);
        if(isRight())
            setX(x + SPEAD);
    }

    public int getWINDTH() {
        return WINDTH;
    }

    public int getHIGHT() {
        return HIGHT;
    }

    public boolean isLeft() {
        return heading == Heading.LEFT;
    }

    public void setLeft() {
        heading = Heading.LEFT;
    }

    public boolean isRight() {
        return heading == Heading.RITE;
    }

    public void setRight() {
        heading = Heading.RITE;
    }

    public void setNon(){
        heading = Heading.NON;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        if(x > END_WINDOW - WINDTH)
            this.x = END_WINDOW - WINDTH;
        else if(x < START_WINDOW)
                this.x = 0;
            else this.x = x;
    }

    public int getY() {
        return Y;
    }

    public void render(Graphics2D g) {
                g.setColor(new Color(0xffff0000));//Вынести в константы графики
                g.fillRoundRect(x, Y, WINDTH, HIGHT, 10, 10);
    }
}
abstract class Entity {

    protected int x;
    protected int y;

    protected Entity(int x, int y){
        this.x = x;
        this.y = y;
    }

    public abstract void aplay();
    public abstract void render(Graphics2D g);
}
 class Interfase extends Entity{
	 
    Game game;
    Image image;

    public Interfase(Game game){
        super(0, 0);
        this.game = game;
        
        try {	image = ImageIO.read(new File("Car.jpg"));
		} catch (IOException e) {e.printStackTrace();	}
    }

    @Override
    public void aplay() {

    }

    @Override
    public void render(Graphics2D g) {
    	//g.drawImage(image, 0, 0,null);
        g.setColor(new Color(200, 0, 0));
        double size = 20;
        g.setFont(new Font("Serif", Font.BOLD | Font.LAYOUT_RIGHT_TO_LEFT, 24));
        g.drawString(game.getCountBall() + " ", 10, 20);
        
    }
}