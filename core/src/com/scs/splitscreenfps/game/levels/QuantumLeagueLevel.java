package com.scs.splitscreenfps.game.levels;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.scs.basicecs.AbstractEntity;
import com.scs.basicecs.BasicECS;
import com.scs.splitscreenfps.game.Game;
import com.scs.splitscreenfps.game.MapData;
import com.scs.splitscreenfps.game.components.towerdefence.CanBuildComponent;
import com.scs.splitscreenfps.game.components.towerdefence.CanBuildOnComponent;
import com.scs.splitscreenfps.game.components.towerdefence.ShowFloorSelectorComponent;
import com.scs.splitscreenfps.game.components.towerdefence.TowerDefencePlayerData;
import com.scs.splitscreenfps.game.data.MapSquare;
import com.scs.splitscreenfps.game.entities.Floor;
import com.scs.splitscreenfps.game.entities.Wall;
import com.scs.splitscreenfps.game.entities.towerdefence.TowerDefenceEntityFactory;
import com.scs.splitscreenfps.game.input.ControllerInputMethod;
import com.scs.splitscreenfps.game.input.IInputMethod;
import com.scs.splitscreenfps.game.input.MouseAndKeyboardInputMethod;
import com.scs.splitscreenfps.game.input.NoInputMethod;
import com.scs.splitscreenfps.game.systems.towerdefence.BuildDefenceSystem;
import com.scs.splitscreenfps.game.systems.towerdefence.BulletSystem;
import com.scs.splitscreenfps.game.systems.towerdefence.CheckAltarSystem;
import com.scs.splitscreenfps.game.systems.towerdefence.CollectCoinsSystem;
import com.scs.splitscreenfps.game.systems.towerdefence.ShowFloorSelectorSystem;
import com.scs.splitscreenfps.game.systems.towerdefence.TowerDefenceEnemySpawnSystem;
import com.scs.splitscreenfps.game.systems.towerdefence.TowerDefenceEnemySystem;
import com.scs.splitscreenfps.game.systems.towerdefence.TowerDefencePhaseSystem;
import com.scs.splitscreenfps.game.systems.towerdefence.TurretSystem;

import ssmith.lang.NumberFunctions;
import ssmith.libgdx.GridPoint2Static;

/*
public class QuantumLeagueLevel extends AbstractLevel {

	public static Properties prop;

	public int levelNum = 1;
	private List<String> instructions = new ArrayList<String>(); 
	private QLPhaseSystem qlPhaseSystem;

	public QuantumLeagueLevel(Game _game) {
		super(_game);

		prop = new Properties();
		try {
			prop.load(new FileInputStream("quantumleague/td_config.txt"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		instructions.add("Todo");

		this.qlPhaseSystem = new QLPhaseSystem(this);
	}


	@Override
	public void setupAvatars(AbstractEntity player, int playerIdx) {
		player.addComponent(new ShowFloorSelectorComponent());
		player.addComponent(new TowerDefencePlayerData());
		player.addComponent(new CanBuildComponent());
	}


	@Override
	public void setBackgroundColour() {
		Gdx.gl.glClearColor(.6f, .6f, 1, 1);
	}


	@Override
	public void load() {
		loadMapFromFile("quantumleague/map1.csv");
	}


	private void loadMapFromFile(String file) {
		String str = Gdx.files.internal(file).readString();
		String[] str2 = str.split("\n");

		this.map_width = str2[0].split("\t").length;
		this.map_height = str2.length;

		game.mapData = new MapData(map_width, map_height);

		int row = 0;
		for (String s : str2) {
			s = s.trim();
			if (s.length() > 0 && s.startsWith("#") == false) {
				String cells[] = s.split("\t");
				for (int col=0 ; col<cells.length ; col++) {
					game.mapData.map[col][row] = new MapSquare(game.ecs);

					String cell = cells[col];
					String tokens[] = cell.split(Pattern.quote("+"));
					for (String token : tokens) {
						if (token.equals("P")) { // Start pos
							this.startPositions.add(new GridPoint2Static(col, row));
							Floor floor = new Floor(game.ecs, "towerdefence/textures/corridor.jpg", col, row, 1, 1, false);
							game.ecs.addEntity(floor);
							game.mapData.map[col][row].entity.addComponent(new CanBuildOnComponent());
						} else if (token.equals("W")) { // Wall
							game.mapData.map[col][row].blocked = true;
							Wall wall = new Wall(game.ecs, "towerdefence/textures/ufo2_03.png", col, 0, row, false);
							game.ecs.addEntity(wall);
						} else if (token.equals("C")) { // Chasm
							game.mapData.map[col][row].blocked = true;
						} else if (token.equals("F")) { // Floor - can build
							Floor floor = new Floor(game.ecs, "towerdefence/textures/corridor.jpg", col, row, 1, 1, false);
							game.ecs.addEntity(floor);
							game.mapData.map[col][row].entity.addComponent(new CanBuildOnComponent());
						} else if (token.equals("E")) { // Empty floor - cannot build
							Floor floor = new Floor(game.ecs, "towerdefence/textures/wall2.jpg", col, row, 1, 1, false);
							game.ecs.addEntity(floor);
							if (NumberFunctions.rnd(1,  5) == 1) {
								AbstractEntity coin = TowerDefenceEntityFactory.createCoin(game.ecs, col+.5f, row+.5f);
								game.ecs.addEntity(coin);
							}
						} else if (token.equals("D")) { // Centre for defending!
							targetPos = new GridPoint2Static(col, row);
							Floor floor = new Floor(game.ecs, "towerdefence/textures/wall2.jpg", col, row, 1, 1, false);
							game.ecs.addEntity(floor);							
							AbstractEntity altar = TowerDefenceEntityFactory.createAltar(game.ecs, col, row);
							game.ecs.addEntity(altar);
							checkAltarSystem.altars.add(altar);
						} else if (token.equals("S")) { // Spawn point
							Floor floor = new Floor(game.ecs, "towerdefence/textures/wall2.jpg", col, row, 1, 1, false);
							game.ecs.addEntity(floor);
							spawnEnemiesSystem.enemy_spawn_points.add(new GridPoint2Static(col, row));
						} else {
							throw new RuntimeException("Unknown cell type: " + token);
						}
					}
				}
				row++;
			}
		}
	}


	@Override
	public void addSystems(BasicECS ecs) {
		ecs.addSystem(new BulletSystem(ecs));
	}


	@Override
	public void update() {
		game.ecs.processSystem(BulletSystem.class);
		this.qlPhaseSystem
	}


	public void renderUI(SpriteBatch batch2d, int viewIndex) {
	}


	@Override
	public void renderHelp(SpriteBatch batch2d, int viewIndex) {
		game.font_med.setColor(1, 1, 1, 1);
		int x = (int)(Gdx.graphics.getWidth()*0.4);
		int y = (int)(Gdx.graphics.getHeight()*0.8);
		for (String s : this.instructions) {
			game.font_med.draw(batch2d, s, x, y);
			y -= this.game.font_med.getLineHeight();
		}
	}


}

*/