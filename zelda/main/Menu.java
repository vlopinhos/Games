package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import world.World;

public class Menu {
	
	public String[] options = {"new game", "load game", "exit"};
	
	public int currentOption = 0;
	public int maxOption = options.length - 1;
	
	public boolean up, down, enter;
	public static boolean pause = false;

	public static boolean saveExists = false;
	public static boolean saveGame = false;
	
	public void tick() {
		File file = new File("save.txt");
		if(file.exists()) {
			saveExists = true;
		} else {
			saveExists = false;
		}
		
		if(up) {
			up = false;
			currentOption--;
			if(currentOption < 0) currentOption = maxOption;
		}
		if(down) {
			down = false;
			currentOption++;
			if(currentOption > maxOption) currentOption = 0;
		}
		if(enter) {
			enter = false;
			if(options[currentOption] == "new game" || options[currentOption] == "continue") {
				Game.gameState = "NORMAL";
				pause = false;
				file = new File("save.txt");
				file.delete();
			} else if(options[currentOption] == "load game") {
				file = new File("save.txt");
				if(saveExists) {
					String saver = loadGame(0);
					applySave(saver);
				}
			} else if(options[currentOption] == "exit") {
				System.exit(1);
			}
		}
	}
	
	public static void applySave(String str) {
		String[] spl = str.split("/");
		for(int i = 0; i < spl.length; i++) {
			String[] spl2 = spl[i].split(":");
			switch(spl2[0]) { 
				case "level":
					World.restartGame("level" + spl2[1] + ".png");
					Game.gameState = "NORMAL";
					pause = false;
					break;
				case "life":
					Game.player.life = Integer.parseInt(spl2[1]);
					break;
				case "x":
					Game.player.x = Integer.parseInt(spl2[1]);
					break;
				case "y":
					Game.player.y = Integer.parseInt(spl2[1]);
					break;
				case "ammo":
					Game.player.ammo = Integer.parseInt(spl2[1]);
					if(Game.player.ammo > 0) Game.player.hasGun = true;
					break;
				default:
					break;
			}
		}
	}
	
	public static String loadGame(int encode) {
		String line = "";
		File file = new File("save.txt");
		if(file.exists()) {
			try {
				String singleLine = null;
				BufferedReader reader = new BufferedReader(new FileReader("save.txt"));
				try {
					while((singleLine = reader.readLine()) != null) {
						String[] next = singleLine.split(":");
						char[] val = next[1].toCharArray();
						next[1] = "";
						for(int i = 0; i < val.length; i++) {
							val[i] -= encode;
							next[1] += val[i];
						}
						line += next[0];
						line += ":";
						line += next[1];
						line += "/";
					}
				} catch(IOException e) {}
			} catch(FileNotFoundException e) {}
		}
		
		return line;
	}
	
	public static void saveGame(String[] val1, int[] val2, int encode) {
		BufferedWriter write = null;
		try {
			write = new BufferedWriter(new FileWriter("save.txt"));
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		for(int i = 0; i < val1.length; i++) {
			String current = val1[i];
			current += ":";
			char[] value = Integer.toString(val2[i]).toCharArray();
			for(int j = 0; j < value.length; j++) {
				value[j] += encode;
				current += value[j];
			}
			try {
				write.write(current);
				if(i < val1.length - 1) write.newLine();
			} catch(IOException e) {}
		}
		try {
			write.flush();
			write.close();
		} catch(IOException e) {}
	}
	
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(new Color(0, 0, 0, 100));
		g.fillRect(0, 0, Game.WIDTH * Game.SCALE, Game.HEIGHT * Game.SCALE);
		g.setColor(Color.RED);
		g.setFont(new Font("arial", Font.BOLD, 36));
		g.drawString(">Game #01<", (Game.WIDTH * Game.SCALE) / 2 - 115, (Game.HEIGHT * Game.SCALE) / 2 - 150);
		
		g.setColor(Color.WHITE);
		g.setFont(new Font("arial", Font.BOLD, 24));
		if(pause == false) {
			g.drawString("New Game", (Game.WIDTH * Game.SCALE) / 2 - 70, 170);
		} else {
			g.drawString("Resume", (Game.WIDTH * Game.SCALE) / 2 - 55, 170);
		}
		g.drawString("Load Game", (Game.WIDTH * Game.SCALE) / 2 - 75, 210);
		g.drawString("Exit", (Game.WIDTH * Game.SCALE) / 2 - 32, 250);
		
		if(options[currentOption] == "new game") {
			g.drawString(">", (Game.WIDTH * Game.SCALE) / 2 - 90, 170);
		} else if(options[currentOption] == "load game") {
			g.drawString(">", (Game.WIDTH * Game.SCALE) / 2 - 95, 210);
		} else if(options[currentOption] == "exit") {
			g.drawString(">", (Game.WIDTH * Game.SCALE) / 2 - 52, 250);
		}
	}
}
