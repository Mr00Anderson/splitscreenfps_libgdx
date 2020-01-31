package com.scs.splitscreenfps.game.levels;

import com.badlogic.gdx.Gdx;
import com.scs.splitscreenfps.game.Game;
import com.scs.splitscreenfps.game.MapData;
import com.scs.splitscreenfps.game.data.MapSquare;
import com.scs.splitscreenfps.game.entities.Floor;
import com.scs.splitscreenfps.mapgen.MazeGen1;

public class CleanTheLitterLevel extends AbstractLevel {

	private Game game;

	public CleanTheLitterLevel(Game _game) {
		super();
		
		game = _game;
	}


	public void setBackgroundColour() {
		Gdx.gl.glClearColor(1, 1, 1, 1);
	}


	@Override
	public void load() {
		loadMapFromMazegen(game);

	}


	private void loadMapFromMazegen(Game game) {
		this.map_width = 16;
		this.map_height = 16;

		game.mapData = new MapData(map_width, map_height);

		MazeGen1 maze = new MazeGen1(map_width, map_height, 10);

		for (int i=0 ; i<this.startPositions.length ;i++) {
			this.startPositions[i] = maze.start_pos;
		}

		for (int z=0 ; z<map_height ; z++) {
			for (int x=0 ; x<map_width ; x++) {
				game.mapData.map[x][z] = new MapSquare();
				game.mapData.map[x][z].blocked = maze.map[x][z] == MazeGen1.WALL;
				if (game.mapData.map[x][z].blocked) {
					//Wall wall = new Wall("monstermaze/wall.png", x, z, false);
					//game.ecs.addEntity(wall);
				} else {
					Floor floor = new Floor();
					game.ecs.addEntity(wall);
				}
			}
		}

		game.ecs.addEntity(new Floor("colours/white.png", map_width, map_height, false));
	}

}