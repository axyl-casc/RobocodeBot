package infinite.mind;

import javax.swing.JOptionPane;

/**
 * Simple launcher that prompts for the server URL and secret before starting
 * the bot.
 */
public class TankBotLauncher {
    public static void main(String[] args) {
        String url = JOptionPane.showInputDialog(null, "Server URL", "ws://localhost:7654");
        if (url == null || url.isEmpty()) {
            url = "ws://localhost:7654";
        }
        String secret = JOptionPane.showInputDialog(null, "Server Secret");
        if (secret == null) {
            secret = "";
        }
        TankBot bot = new TankBot(url, secret);
        bot.start();
    }
}
