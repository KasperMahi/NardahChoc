package scripts;

import org.tribot.api.General;
import org.tribot.api.input.Keyboard;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Login;
import org.tribot.api2007.Player;
import org.tribot.api2007.Players;
import org.tribot.api2007.Trading;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSPlayer;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.script.interfaces.MessageListening07;
import java.awt.Color; //to get different colors
import java.awt.Font; //to change font
import java.awt.Graphics; //paint
import java.awt.Graphics2D; //needed for the image
import java.awt.Image; //same as above
import java.io.IOException; //this is needed for the loading of the image
import java.net.URL; //same as above

import javax.imageio.ImageIO; //same as above

import org.tribot.api.Timing; //to calculate time things
import org.tribot.api2007.Skills; //to get XP/levels
import org.tribot.script.interfaces.Painting; //for onPaint()

public class NardahMule extends Script implements MessageListening07, Painting{
	public String tradeName;
	public int tradeStatus = 1;
	public long startTime2 = System.currentTimeMillis();
	public long startTime3 = System.currentTimeMillis();
	public int timeSinceLast;
	public int timeSinceLast2;
	RSArea BANK_AREA = new RSArea(new RSTile(3427, 2889), new RSTile(3430, 2894));
	public String muleName;
	public String muleMsg;
	public String currentStatus;
	public String serverMsg;

	@Override
	public void tradeRequestReceived(String s) {   
		tradeName = s;
		tradeStatus = 2;
	}
	@Override
	public void playerMessageReceived(String name, String message) {   
		muleName = name;
		muleMsg = message;
		if(name == Player.getRSPlayer().getName() && message.contains("Forcemule")) {
			timeSinceLast = 3600001;
			tradeStatus = 1;
		}
	}

	@Override
	public void serverMessageReceived(String msg) {   
		if(msg.contains("Other player declined the trade")) {
			serverMsg = msg;
		}
	}


	public void tradeAccepter() {
		if (tradeName!= null) {
			if (Trading.getWindowState() == null) {
				RSPlayer[] players = Players.findNearest(Filters.Players.nameEquals(tradeName));
				if(players[0] != null) {
					players[0].click("Trade with " + tradeName);
				} else { tradeName = null;  }
				General.sleep(500);
			} else {
				if(Trading.getCount(true, 1974)>0 || Trading.getCount(true,6795)>0) {
					Trading.accept();
					General.sleep(100,100);
					Trading.accept();
					tradeStatus = 1;
					println("Trade is done");
					tradeName = null;
					startTime3 = System.currentTimeMillis();
					General.sleep(500);

				} else if(serverMsg != null) {
					tradeAccepter();
				}
			}

		}
	}
	private static final long startTime = System.currentTimeMillis();

	Font font = new Font("Verdana", Font.BOLD, 14);
	public void onPaint(Graphics g) {

		long timeRan = System.currentTimeMillis() - startTime;
		g.setFont(font);

		g.setColor(new Color(255,255,255));
		g.drawString("Status: " + currentStatus, 340, 280);
		g.drawString("Runtime: " + Timing.msToString(timeRan), 340, 300);
		g.drawString("Bars collected: " + Inventory.getCount(1974), 340, 320);
		g.drawString("Ice collected: " + Inventory.getCount(6795), 340, 340);
		g.drawString("Time since last mule: " + Timing.msToString(timeSinceLast), 340, 360);
	}
	public void run() {

		while(true) {
			if (!BANK_AREA.contains(Player.getPosition())) {
				WebWalking.walkTo(BANK_AREA.getRandomTile());
				currentStatus = "Walking to bank";
			}
			timeSinceLast = (int) (System.currentTimeMillis() - startTime2);
			timeSinceLast2 = (int) (System.currentTimeMillis() - startTime3);
			// println("time since last mule: " + Timing.msToString(timeSinceLast));
			General.sleep(500);
			if (tradeStatus == 2) {
				println("Accepting trade from: " + tradeName);
				currentStatus = "Accepting trade";
				tradeAccepter();
			}
			if (timeSinceLast2 > 300000 && tradeStatus == 1 && Login.getLoginState()==Login.STATE.INGAME) {
				this.setLoginBotState(false);
				currentStatus = "Logging out";
				Login.logout();
				startTime3 = System.currentTimeMillis();
				General.sleep(5000);
			}
			if( timeSinceLast > 3600000 && tradeStatus == 1)	{
				this.setLoginBotState(true);
				startTime3 = System.currentTimeMillis();
				if(Login.getLoginState()!=Login.STATE.INGAME && Login.getLoginState()!=Login.STATE.WELCOMESCREEN ) {
					currentStatus = "Logging in";
					Login.login();
					General.sleep(20000);
				} else if (Login.getLoginState()==Login.STATE.INGAME) {
					println("Muling");
					currentStatus = "First attempt muling";
					Keyboard.typeString("/return");
					Keyboard.pressEnter();
					General.sleep(15000);
					if(muleName == Player.getRSPlayer().getName() && muleMsg.contains("/return")) {
						currentStatus = "First mule attempt failed, trying again";
						println("Failed first Mule attempt, trying again");
						Keyboard.typeString("/return");
						Keyboard.pressEnter();
						General.sleep(5000);
					}
					startTime2 = System.currentTimeMillis();
				}
			} else { currentStatus = "Waiting"; }

		}
	}
}

