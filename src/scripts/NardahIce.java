package scripts;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api2007.Player;
import org.tribot.api2007.Players;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.WorldHopper;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Login;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSInterface;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSPlayer;
import org.tribot.api2007.types.RSTile;
import org.tribot.api2007.NPCs;
import org.tribot.script.Script;
import java.awt.Color; //to get different colors
import java.awt.Font; //to change font
import java.awt.Graphics; //paint
import java.awt.Graphics2D; //needed for the image
import java.awt.Image; //same as above
import java.io.IOException; //this is needed for the loading of the image
import java.net.URL; //same as above
import java.nio.file.DirectoryStream.Filter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO; //same as above

import org.tribot.api.Timing; //to calculate time things
import org.tribot.api2007.Skills; //to get XP/levels
import org.tribot.api2007.Trading;
import org.tribot.script.interfaces.MessageListening07;
import org.tribot.script.interfaces.Painting; //for onPaint()

public class NardahIce extends Script implements Painting,MessageListening07 {
	RSArea BANK_AREA = new RSArea(new RSTile(3427, 2889), new RSTile(3430, 2894));
	RSArea ROKUH_AREA = new RSArea(new RSTile(3433, 2914), new RSTile(3430, 2916));
	public int bars;
	int muleState = 0;
	String mule;
	String servermsg;
	@Override
	public void clanMessageReceived(String name, String message) {
		if (message.contains("Return")){
			muleState = 1;
			mule = name;
		}
	}
	public void serverMessageReceived(String message2) {
		if (message2.contains("Other player is busy at the moment.") || message2.contains("Sending trade offer...") ) {
			servermsg = message2;
		}
	}
	public void bank() {
		if (!BANK_AREA.contains(Player.getPosition())) {
			krydser();
			WebWalking.walkTo(BANK_AREA.getRandomTile());
			General.sleep(500,3000);
		} else {
			if (!Banking.isBankScreenOpen()) {
				Banking.openBank();
				General.sleep(500,3000);
			} else {
				Banking.depositAllExcept(995);
				bars = bars + 27;
				long totalT = this.getRunningTime();
				println("Choc-ice bought: " + bars);
				println("Choc-ice per hour: " + perHour(bars, totalT));
			}
		}
	}
	public long perHour(int value, long totalT) {

		return (long) (value * 3600000D / totalT);

	}
	public void buy() {
		if (!ROKUH_AREA.contains(Player.getPosition())) {
			WebWalking.walkTo(ROKUH_AREA.getRandomTile());
			General.sleep(1500);
		} else {
			RSNPC[] rokuh = NPCs.findNearest(4761);
			if (isOpen()==false && !Player.isMoving()) {
				rokuh[0].click("Trade Rokuh");
				General.sleep(1000);
			} else if (!Inventory.isFull() && isOpen()) {
				// int missing = limit(27 - Inventory.getCount("Choc-ice")); 
				RSInterface choc = Interfaces.get(300, 16, 1);
				int shopAmount = choc.getComponentStack();
				if (shopAmount <= 20) {
					int world = WorldHopper.getRandomWorld(true);
					krydser();
					WorldHopper.changeWorld(world);
					General.sleep(5000);
				} else {
					buy(choc,19,"Buy 50");
				}

			}
		} 
	}
	public void trade(String names2) {
		if (WorldHopper.getWorld() != 95) {
			if(Banking.isBankScreenOpen()) {
				Banking.close();
				General.sleep(1500);
			}
			WorldHopper.changeWorld(395);
		}
		if (!BANK_AREA.contains(Player.getPosition())) {
			krydser();
			WebWalking.walkTo(BANK_AREA.getRandomTile());
		} 
		if (BANK_AREA.contains(Player.getPosition())) {
			if (!Banking.isBankScreenOpen() && Inventory.getCount(6795)==0 ) {
				Banking.openBank();
				Banking.depositAllExcept(995);
				if((Banking.find("Choc-ice").length > 0)) {
					Interfaces.get(12, 23).click("Note");
					Banking.withdraw(0, 6794);
					Interfaces.get(12, 22).click("Item");
					Banking.close();
				} else { muleState = 0;}

			}
			if(Trading.getWindowState() == null) {
				RSPlayer[] players = Players.findNearest(Filters.Players.nameEquals(mule));
				if(players.length>0) {
					players[0].click("Trade with " + mule);
				} else { mule = null; muleState = 0;}
				General.sleep(1000);
				if(servermsg != null && WorldHopper.getWorld()==95) {
					General.sleep(5000);
					servermsg = null;
					players[0].click("Trade with " + mule);
				} else { WorldHopper.changeWorld(395);}
			} 
			if(Trading.getWindowState() != null) {
				Trading.offer(0, 6795);
				General.sleep(500);
				Trading.accept();
				General.sleep(100);
				Trading.accept();
				if(Inventory.getCount(6795)==0) {
					muleState = 0;
				} else {
					Trading.close();
					General.sleep(5000);
					trade(mule);
				}
				
			}

		}
	}
	public static boolean isOpen(){return Interfaces.get(300, 16) != null;}
	public static boolean buy(RSInterface shopItem, int minStock, String ... amountToBuy) {

		String[] actions;
		if (Interfaces.isInterfaceSubstantiated(shopItem) && (actions = shopItem.getActions()) != null && actions.length > 0 && shopItem.getComponentStack() > minStock) {
			General.sleep(500, 1200);
			return Clicking.click(amountToBuy, shopItem);
		}
		return false;
	}
	public static  boolean krydser() {
		RSInterface kryds = Interfaces.get(300,1,11);
		if (Interfaces.isInterfaceSubstantiated(kryds)) {
			return Clicking.click(kryds);
		}
		return false;
	}
	public int limit(int value) {
		return Math.max(2, Math.min(value, 25));
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true) {
			General.sleep(1000);
			if(Login.getLoginState()!=Login.STATE.INGAME) {
				Login.login();
				General.sleep(5000);
			}
			if (muleState==1 && mule!=null) {
				trade(mule);
			}
			if (Inventory.isFull() && muleState==0) {
				krydser();
				bank();
			} else if (!Inventory.isFull() && muleState ==0){
				buy();
			}
		}
	}



	private static final long startTime = System.currentTimeMillis();
	Font font = new Font("Verdana", Font.BOLD, 14);
	public void onPaint(Graphics g) {

		long timeRan = System.currentTimeMillis() - startTime;
		g.setFont(font);

		g.setColor(new Color(255,255,255));
		g.drawString("Runtime: " + Timing.msToString(timeRan), 340, 300);
		g.drawString("Total Ice: " + bars, 340, 320);
		g.drawString("Per hour: " + perHour(bars, timeRan), 340, 340);
	}
}
