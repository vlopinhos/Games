package graficos;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import main.Game;

public class UI {

	public void render(Graphics g) {
		g.setColor(Color.RED);
		g.fillRect(8, 4, 70, 8);
		g.setColor(Color.GREEN);
		g.fillRect(8, 4, (int)((Game.player.life / Game.player.maxLife) * 70), 8);
		g.setColor(Color.WHITE);
		g.setFont(new Font("arial", Font.BOLD, 8));
		g.drawString((int)Game.player.life + "/" + (int)Game.player.maxLife, 30, 11);
	}
}
