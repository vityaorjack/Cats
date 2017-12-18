import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.*;


/**
 * Created by Andrej on 10.12.2017.
 */
public class Main {
    public static void main(String[] args) {
        new Game().start();
    }
}

/**
 * Created by Andrej on 10.12.2017.
 */
class Graphic {
    private static boolean created = false;
    private static JFrame window;
    private static Canvas content;
    private static BufferedImage buffer;
    private static int[] bufferData;
    private static Graphics bufferGraphics;
    private static int clearColor;
    private static BufferStrategy bufferStrategy;

    private static void initWindow(String title, int width, int height, boolean reSize){
        window = new JFrame(title);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        content = new Canvas();
        Dimension size = new Dimension(width, height);
        content.setPreferredSize(size);
        window.setResizable(reSize);
        window.getContentPane().add(content);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    private Graphic(){}

    private static void initGraphic(int width, int height, int numberBuffers){
        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bufferData = ((DataBufferInt) buffer.getRaster().getDataBuffer()).getData();
        bufferGraphics = buffer.getGraphics();
        ((Graphics2D) bufferGraphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        content.createBufferStrategy(numberBuffers);
        bufferStrategy = content.getBufferStrategy();
        window.setFocusable(true);
        content.setFocusable(true);
    }

    public static void create (String title, int width, int height, int _clearColor, int numberBuffers, boolean reSize){
        if (created)
            return;
        initWindow(title, width, height, reSize);
        initGraphic(width, height, numberBuffers);
        clearColor = _clearColor;

        created = true;
    }

    public static void clear() {
        Arrays.fill(bufferData, clearColor);
    }

    public static void swapBuffers() {
        Graphics g = bufferStrategy.getDrawGraphics();
        g.drawImage(buffer, 0, 0, null);
        bufferStrategy.show();
    }

    public static Graphics2D getGraphics() {
        return (Graphics2D) bufferGraphics;
    }

    public static void destroy() {
        if (!created)
            return;
        window.dispose();
    }

    public static void setTitle(String title) {
        window.setTitle(title);
    }

    public static void addInputListener(KeyListener inputListener) {
        content.addKeyListener(inputListener);
    }
}
/**
 * Created by Andrej on 12.12.2017.
 */

 class Time {
    public static final long SECOND = 1000000000;

    public static long get(){
        return System.nanoTime();
    }
}