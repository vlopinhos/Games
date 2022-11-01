package main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import entities.*;
import graficos.*;
import world.*;

public class Game extends Canvas implements Runnable, KeyListener, MouseListener { 

	private static final long serialVersionUID = 1L; // Para evitar um warning
	public static JFrame frame; // Janela do jogo
	private Thread thread; // Thread do jogo
	private boolean isRunning = true; // Se o jogo está rodando ou não
	public static final int WIDTH = 240; // Largura da tela
	public static final int HEIGHT = 160; // Altura da tela
	public static final int SCALE = 3; // Escala da tela
	
	private int CUR_LEVEL = 1, MAX_LEVEL = 2; // Nível atual e máximo
	private BufferedImage image; // Imagem que será renderizada
	
	public static List<Entity> entities; // Lista de entidades
	public static List<Enemy> enemies; // Lista de inimigos
	public static List<BulletShoot> bullets; // Lista de tiros
	public static Spritesheet spritesheet; // Spritesheet
	
	public static World world; // Mundo
	public static Player player; // Jogador
	public static Random rand; // Gerador de números aleatórios
	public UI ui; // Interface do usuário
	
	public static String gameState = "MENU"; // Estado do jogo
	private boolean showMessageGameOver = true; // Mostrar mensagem de game over
	private int framesGameOver = 0; // Contador de frames para mostrar a mensagem de game over
	private boolean restartGame = false; // Reiniciar o jogo
	
	public Menu menu; // Menu
	
	public boolean saveGame = false; // Salvar o jogo

	public Game() { // Construtor
		// Sound.musicBackground.play(); // Tocar música de fundo
		rand = new Random(); // Instanciar gerador de números aleatórios
		addKeyListener(this); // Adicionar o KeyListener
		addMouseListener(this); // Adicionar o MouseListener
		setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE)); // Definir o tamanho da tela
		initFrame(); // Iniciar a janela
		
		ui = new UI(); // Instanciar a interface do usuário
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB); // Instanciar a imagem
		entities = new ArrayList<Entity>(); // Instanciar a lista de entidades
		enemies = new ArrayList<Enemy>(); // Instanciar a lista de inimigos
		bullets = new ArrayList<BulletShoot>(); // Instanciar a lista de tiros
		spritesheet = new Spritesheet("/spritesheet.png"); // Instanciar a spritesheet
		player = new Player(0, 0, 16, 16, spritesheet.getSprite(32, 0, 16, 16)); // Instanciar o jogador
		entities.add(player); // Adicionar o jogador à lista de entidades
		world = new World("/level1.png"); // Instanciar o mundo
		menu = new Menu(); // Instanciar o menu
	}
	
	public void initFrame() { // Iniciar a janela
		frame = new JFrame("Game #1"); // Instanciar a janela
		frame.add(this); // Adicionar o Game à janela
		frame.setResizable(false); // Não permitir redimensionar a janela
		frame.pack(); // Ajustar o tamanho da janela
		frame.setLocationRelativeTo(null); // Centralizar a janela
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Fechar o programa ao fechar a janela
		frame.setVisible(true); // Tornar a janela visível
	}
	
	public synchronized void start() { // Iniciar o jogo
		thread = new Thread(this); // Instanciar a thread
		isRunning = true; // Definir que o jogo está rodando
		thread.start(); // Iniciar a thread
	}
	
	public synchronized void stop() { // Parar o jogo
		isRunning = false; // Definir que o jogo não está rodando
		try {
			thread.join(); // Parar a thread
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) { // Método principal
		Game game = new Game(); // Instanciar o Game
		game.start(); // Iniciar o jogo
	}
	
	public void tick() { // Atualizar o jogo
		if(gameState == "NORMAL") { // Se o estado do jogo for normal
			restartGame = false; // Definir que o jogo não será reiniciado
			for(int i = 0; i < entities.size(); i++) { // Para cada entidade
				Entity e = entities.get(i); // Pegar a entidade
				e.tick(); // Atualizar a entidade
			}
			
			for(int i = 0; i < bullets.size(); i++) { // Para cada tiro
				bullets.get(i).tick(); // Atualizar o tiro
			}
			
			if(enemies.size() == 0) { // Se não houver inimigos
				CUR_LEVEL++; // Passar para o próximo nível
				if(CUR_LEVEL > MAX_LEVEL) { // Se o nível atual for maior que o máximo
					CUR_LEVEL = 1; // Voltar para o primeiro nível
				}
				String newWorld = "level" + CUR_LEVEL + ".png"; // Pegar o nome do novo mundo
				World.restartGame(newWorld); // Reiniciar o jogo
			}
			if(saveGame) { // Se o jogo deve ser salvo
				saveGame = false; // Definir que o jogo não deve ser salvo
				String[] opt1 = {"level", "life", "x", "y", "ammo"}; // Opções
				int[] opt2 = {CUR_LEVEL, (int) player.life, (int) player.x, (int) player.y, player.ammo}; // Valores
				Menu.saveGame(opt1, opt2, 0); // Salvar o jogo
				System.out.println("Jogo Salvo!"); // Mostrar mensagem de jogo salvo
			} 
		} else if(gameState == "GAME_OVER") { // Se o estado do jogo for game over
			framesGameOver++; // Incrementar o contador de frames
			if(framesGameOver == 30) { // Se o contador de frames for igual a 30
				framesGameOver = 0; // Zerar o contador de frames
				if(showMessageGameOver) showMessageGameOver = false; // Se a mensagem de game over deve ser mostrada, não mostrar 
				else showMessageGameOver = true; // Se a mensagem de game over não deve ser mostrada, mostrar
			}
			
			if(restartGame) { // Se o jogo deve ser reiniciado
				restartGame = false; // Definir que o jogo não deve ser reiniciado
				gameState = "NORMAL"; // Definir que o estado do jogo é normal
				CUR_LEVEL = 1; // Voltar para o primeiro nível
				String newWorld = "level" + CUR_LEVEL + ".png"; // Pegar o nome do novo mundo
				World.restartGame(newWorld); // Reiniciar o jogo
			}
		} else if(gameState == "MENU") { // Se o estado do jogo for menu
			menu.tick(); // Atualizar o menu
		}
	}
	
	public void render() { // Renderizar o jogo
		BufferStrategy bs = this.getBufferStrategy(); // Pegar a estratégia de buffer
		if(bs == null) { // Se a estratégia de buffer for nula
			this.createBufferStrategy(3); // Criar a estratégia de buffer
			return; 
		}
		
		Graphics g = image.getGraphics(); // Pegar o gráfico da imagem
		g.setColor(new Color(0, 0, 0)); // Definir a cor do gráfico
		g.fillRect(0, 0, WIDTH, HEIGHT); // Preencher o gráfico com a cor definida
		
		world.render(g); // Renderizar o mundo
		for(int i = 0; i < entities.size(); i++) { // Para cada entidade
			Entity e = entities.get(i); // Pegar a entidade
			e.render(g); // Renderizar a entidade
		}
		
		for(int i = 0; i < bullets.size(); i++) { // Para cada tiro
			bullets.get(i).render(g); // Renderizar o tiro
		}
		
		ui.render(g); // Renderizar a interface do usuário
				
		g.dispose(); // Liberar o gráfico
		g = bs.getDrawGraphics(); // Pegar o gráfico da estratégia de buffer
		g.drawImage(image, 0, 0, WIDTH * SCALE, HEIGHT * SCALE, null); // Desenhar a imagem na tela
		g.setFont(new Font("arial", Font.BOLD, 20)); // Definir a fonte do gráfico
		g.setColor(Color.WHITE); // Definir a cor do gráfico
		g.drawString("Ammo: " + player.ammo, 580, 20); // Desenhar a quantidade de munição na tela
		if(gameState == "GAME_OVER") { // Se o estado do jogo for game over	
			Graphics2D g2 = (Graphics2D) g; // Pegar o gráfico 2D
			g2.setColor(new Color(0, 0, 0, 100)); // Definir a cor do gráfico
			g2.fillRect(0, 0, WIDTH * SCALE, HEIGHT * SCALE); // Preencher o gráfico com a cor definida
			g.setFont(new Font("arial", Font.BOLD, 36)); // Definir a fonte do gráfico
			g.setColor(Color.WHITE); // Definir a cor do gráfico
			g.drawString("Game Over", (WIDTH * SCALE) / 2 - 100, (HEIGHT * SCALE) / 2 - 20); // Desenhar a mensagem de game over na tela
			g.setFont(new Font("arial", Font.BOLD, 32)); // Definir a fonte do gráfico
			if(showMessageGameOver) g.drawString(">Press 'Enter' to restart<", (WIDTH * SCALE) / 2 - 190, (HEIGHT * SCALE) / 2 + 40); // Desenhar a mensagem de reiniciar o jogo na tela
		} else if(gameState == "MENU") { // Se o estado do jogo for menu
			menu.render(g); // Renderizar o menu
		}
		bs.show(); // Mostrar a estratégia de buffer
	}
	
	public void run() { // Rodar o jogo
		long lastTime = System.nanoTime(); // Pegar o tempo atual em nanossegundos
		double amountOfTicks = 60.0; // Definir a quantidade de ticks
		double ns = 1000000000 / amountOfTicks; // Definir a quantidade de nanossegundos
		double delta = 0; // Definir o delta
		int frames = 0; // Definir o contador de frames
		double timer = System.currentTimeMillis(); // Pegar o tempo atual em milissegundos
		requestFocus(); // Pedir foco para a janela
		while(isRunning) { // Enquanto o jogo estiver rodando
			long now = System.nanoTime(); // Pegar o tempo atual em nanossegundos
			delta += (now - lastTime) / ns; // Calcular o delta
			lastTime = now; // Definir o tempo atual como o tempo anterior
			
			if(delta >= 1) { // Se o delta for maior ou igual a 1
				tick(); // Atualizar o jogo
				render(); // Renderizar o jogo
				frames++; // Incrementar o contador de frames
				delta--; // Decrementar o delta
			}
			
			if(System.currentTimeMillis() - timer >= 1000) { // Se o tempo atual em milissegundos for maior ou igual a 1000
				System.out.println("FPS: " + frames); // Mostrar a quantidade de frames por segundo
				frames = 0; // Zerar o contador de frames
				timer +=1000; // Incrementar o timer
			}
		}
		
		stop(); // Parar o jogo
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) player.right = true; 
		else if(e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) player.left = true;
		
		if(e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
			player.up = true;
			if(gameState == "MENU") menu.up = true;
		} else if(e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
			player.down = true;
			if(gameState == "MENU") menu.down = true;
		}
		
		if(e.getKeyCode() == KeyEvent.VK_X) player.shoot = true;
		if(e.getKeyCode() == KeyEvent.VK_Z) player.jump = true;
		
		if(e.getKeyCode() == KeyEvent.VK_ENTER) {
			restartGame = true; 
			if(gameState == "MENU") {
				menu.enter = true;
			}
		}
		
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			gameState = "MENU";
			Menu.pause = true;
		}
		
		if(e.getKeyCode() == KeyEvent.VK_SPACE) {
			if(gameState == "NORMAL") {				
				saveGame = true;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) player.right = false;
		else if(e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) player.left = false;
		
		if(e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) player.up = false;
		else if(e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) player.down = false;
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		player.mouseShoot = true;
		player.mx = e.getX() / SCALE;
		player.my = e.getY() / SCALE;
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}
}
