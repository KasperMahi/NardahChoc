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
import org.tribot.api2007.types.RSInterfaceChild;
import org.tribot.api2007.types.RSItem;
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
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException; //this is needed for the loading of the image
import java.net.URL; //same as above
import java.nio.file.DirectoryStream.Filter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Random;

import javax.imageio.ImageIO; //same as above

import org.tribot.api.Timing; //to calculate time things
import org.tribot.api.input.Mouse;
import org.tribot.api2007.Skills; //to get XP/levels
import org.tribot.api2007.Trading;
import org.tribot.script.interfaces.MessageListening07;
import org.tribot.script.interfaces.Painting; //for onPaint()

public class DesertHorns extends Script {
	RSArea BANK_AREA = new RSArea(new RSTile(3427, 2889), new RSTile(3427, 2894));
	boolean hasPestle = true;
	public static int pestleID = 233;
	public int clickMod;
	public Point a = new Point(575, 223);
	public Point b = new Point(702, 443);
	public void bank() {
		if(Inventory.getCount(9736)==27 || Inventory.getCount(9736)==0 & Inventory.getCount(9735)==0) {
			if (!BANK_AREA.contains(Player.getPosition())) {
				WebWalking.walkTo(BANK_AREA.getRandomTile());
				General.sleep(500,3000);
			} 
			if (!Banking.isBankScreenOpen()) {
				Banking.openBank();
				General.sleep(500,1000);
			} 
			if (Inventory.getCount(233)!=1) {
				Banking.depositAll();
				if(Banking.find(233).length==0) {
					println("No pestle and mortar");
					hasPestle = false;
					Login.logout();
				}
				Banking.withdraw(1, 233);
			}
			if (Inventory.getCount(233)==1) {
				if(Banking.find(9735).length==0) {
					println("No goat horns");
					hasPestle = false;
					Login.logout();

				}
				Banking.depositAllExcept(233);
				Banking.withdraw(27, 9735);
				Banking.close();
				General.sleep(100,300);
				if(Inventory.find(233)[0].getIndex()!= 7*4+4) {
					dragItemToSlot(Inventory.find(233)[0], 7, 4, 6);
					General.sleep(250);
				}

			}
		}
	}

	public static boolean dragItemToSlot(RSItem item, int row, int col, int... dev){
		int x; int y;
		RSInterfaceChild i = Interfaces.get(149,0);
		if (i == null)
			return false;

		x = (int)i.getAbsoluteBounds().getX()+42*(col-1)+16+General.randomSD(-16, 16, 0, dev.length == 0 ? 6 : dev[0]);
		y = (int)i.getAbsoluteBounds().getY()+36*(row-1)+16+General.randomSD(-16, 16, 0, dev.length == 0 ? 6 : dev[0]);

		item.hover();
		Mouse.drag(Mouse.getPos(),new Point(x, y),1);
		return item.getIndex() == row*4+col;
	}

	public void grinder() {
		if(Inventory.getCount(9735)>0 && Inventory.getCount(233)==1 && Inventory.getCount(9736)<27) {
			if(!Banking.isBankScreenOpen()) {
				while(Inventory.getCount(9736)<27) {
					int random = (int)(Math.random() * 2 + 1);
					if(random==1) {
						Clicking.click(Inventory.getAll()[27]);
						General.sleep(50,100);
						Clicking.click(Inventory.getAll()[26]);
					}
					if(random==2) {
						Clicking.click(Inventory.getAll()[26]);
						General.sleep(50,100);
						Clicking.click(Inventory.getAll()[27]);
					}
				} 

			}



		}

	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(hasPestle) {
			if(Login.getLoginState()!=Login.STATE.INGAME || Login.getLoginState()!=Login.STATE.WELCOMESCREEN) {
				Login.login();
				General.sleep(5000);
			}
			bank();
			grinder();

		}
	}
}
