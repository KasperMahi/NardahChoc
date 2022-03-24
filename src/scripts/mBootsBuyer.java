package scripts;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api2007.Player;
import org.tribot.api2007.Players;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.WorldHopper;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Equipment;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Login;
import org.tribot.api2007.NPCChat;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSInterface;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSPlayer;
import org.tribot.api2007.types.RSTile;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Objects;
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
import org.tribot.api2007.Walking;
import org.tribot.script.interfaces.MessageListening07;
import org.tribot.script.interfaces.Painting; //for onPaint()

public class mBootsBuyer extends Script implements Painting,MessageListening07 {
	RSArea MNTN_AREA= new RSArea(new RSTile(2823, 3556), new RSTile(2830, 3552));
	RSArea BANK_AREA= new RSArea(new RSTile(2437, 3082), new RSTile(2444, 3094));
	RSArea TENZING_AREA= new RSArea(new RSTile(2819, 3554), new RSTile(2822, 3557));
	RSArea START_AREA = new RSArea(new RSTile(2895, 3555), new RSTile(2902, 3550));
	RSArea MID_AREA= new RSArea(new RSTile(2832, 3586), new RSTile(2838, 3584));
	RSTile Chest = new RSTile(2443, 3083);
	RSTile Port = new RSTile(2825,3554);
	RSTile Midway = new RSTile(2834, 3570);
	RSTile House = new RSTile(2822, 3555);
	int totalBought;
	String currentState = "banking";

	public void bank() {
		if(currentState == "banking") {
			RSTile playerPos = Player.getPosition();
			if(BANK_AREA.contains(Player.getPosition())) {
				if(Player.getPosition() != Chest) {
					WebWalking.walkTo(Chest);
					General.sleep(1000);
				}
				if(playerPos.equals(Chest)) {
					Banking.openBank();
					totalBought = totalBought + Inventory.getCount(3105);
					Banking.depositAllExcept(995);
					RSItem ring = Equipment.SLOTS.RING.getItem();
					RSItem amulet = Equipment.SLOTS.AMULET.getItem();
					if(ring==null && amulet == null) {
						Banking.withdraw(1, "Ring of dueling(8)");
						Banking.withdraw(1, "Games necklace(8)");
						Banking.close();
						General.sleep(500);
						RSItem[] ring2 = Inventory.find("Ring of dueling(8)");
						RSItem[] amulet2 = Inventory.find("Games necklace(8)");
						General.sleep(500);
						ring2[0].click("Wear");
						General.sleep(200);
						amulet2[0].click("Wear");
						General.sleep(200);
					}
					if(ring==null && amulet != null) {
						Banking.withdraw(1, "Ring of dueling(8)");
						Banking.close();
						RSItem[] ring2 = Inventory.find("Ring of dueling(8)");
						ring2[0].click("Wear");
						General.sleep(200);
					} 
					if(ring!=null && amulet == null) {
						Banking.withdraw(1, "Games necklace(8)");
						Banking.close();
						RSItem[] amulet2 = Inventory.find("Games necklace(8)");
						amulet2[0].click("Wear");
						General.sleep(200);
					}
					if(ring!=null && amulet !=null && Inventory.getCount(995)==336) {
						Banking.close();
						RSItem amulet3 = Equipment.SLOTS.AMULET.getItem();
						amulet3.click("Burthorpe");
						currentState = "buying";
						General.sleep(3000);
					}
					if(Inventory.find(995)==null) {
						Banking.openBank();
						Banking.withdraw(336, 995);
						Banking.close();
					}
					if(Inventory.getCount(995)!= 336) {
						Banking.openBank();
						Banking.depositAll();
						Banking.withdraw(336, 995);
						Banking.close();
					}

				}
			}
		}
	}
	public long perHour(int value, long totalT) {

		return (long) (value * 3600000D / totalT);

	}
	public void buy() {
		if(currentState == "buying") {
			if(START_AREA.contains(Player.getPosition())) {
				WebWalking.walkTo(MID_AREA.getRandomTile());
				General.sleep(500);
				if(!MID_AREA.contains(Player.getPosition())) {
					WebWalking.walkTo(MID_AREA.getRandomTile());
				}
			}
			if(MID_AREA.contains(Player.getPosition())) {
				Walking.blindWalkTo(Midway);
				General.sleep(100,500);
				Walking.blindWalkTo(Port);
				General.sleep(100,500);
				if(!MNTN_AREA.contains(Player.getPosition())) {
					WebWalking.walkTo(MNTN_AREA.getRandomTile());
				}
			}
		}
		if(MNTN_AREA.contains(Player.getPosition())) {
			RSObject[] gate = Objects.findNearest(5, 3726); //get the array of door
			if (gate.length > 0) { // check if there are actually doors within the 40 tile radius.
				RSObject target = gate[0]; //let's find the first door out of our nearest doors as our target
				if (target != null) { //we don't trust trilez and/or we don't know if the object still exists so let's make sure findNearest doesn't return null values.
					if (!target.isOnScreen()) { //the target isn't on screen so let's turn to it.
						Camera.setCameraAngle(0); // set the camera angle
						Camera.turnToTile(target.getPosition()); //turn so you can see the door
					} else {
						if (target.click("Open")) { //click open door, and make sure we clicked
							Camera.setCameraAngle(100); //set the camera back up
							General.sleep(50); //let's sleep for the heck of it since the OP wanted to.
						}
					}
				}
			}
			RSObject[] gateOpen = Objects.findNearest(5, 3728); //get the array of door
			RSObject[] door = Objects.findNearest(5, 3745); //get the array of door
			if (gateOpen.length > 0 && door.length > 0) { // check if there are actually doors within the 40 tile radius.
				RSObject target = door[0]; //let's find the first door out of our nearest doors as our target
				if (target != null) { //we don't trust trilez and/or we don't know if the object still exists so let's make sure findNearest doesn't return null values.
					if (!target.isOnScreen()) { //the target isn't on screen so let's turn to it.
						Camera.setCameraAngle(0); // set the camera angle
						Camera.turnToTile(target.getPosition()); //turn so you can see the door
					} else {
						if (target.click("Open")) { //click open door, and make sure we clicked
							Camera.setCameraAngle(100); //set the camera back up
							General.sleep(50); //let's sleep for the heck of it since the OP wanted to.
						}
					}
				}
			}
		}

		if(TENZING_AREA.contains(Player.getPosition())) {
			RSNPC[] tenzing = NPCs.findNearest(4094);
			tenzing[0].click("Talk-to");
			General.sleep(2000);
			while(!Inventory.isFull() && Inventory.getCount(995) >= 11) { 
				tenzing[0].click("Talk-to");
				General.sleep(500);
					NPCChat.clickContinue(true);
					General.sleep(300,600);
					NPCChat.clickContinue(true);
					General.sleep(300,600);
					NPCChat.selectOption("Can I buy some Climbing boots?", true);
					General.sleep(300,600);
					NPCChat.clickContinue(true);
					General.sleep(300,600);
					NPCChat.clickContinue(true);
					General.sleep(300,600);
					NPCChat.selectOption("OK, sounds good.", true);
					General.sleep(300,600);
					NPCChat.clickContinue(true);
					General.sleep(300,600);
			}
		}
		if(Inventory.isFull()) {
			RSItem ring = Equipment.SLOTS.RING.getItem();
			ring.click("Castle wars");
			currentState = "banking";

		}


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
			if(currentState == "banking") {
				bank();
			}
			if(currentState == "buying") {
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
		g.drawString("Total Boots: " + totalBought, 340, 320);
		g.drawString("Per hour: " + perHour(totalBought, timeRan), 340, 340);
	}
}
