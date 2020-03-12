package com.scs.splitscreenfps.game.systems.towerdefence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.scs.basicecs.AbstractEntity;
import com.scs.basicecs.AbstractSystem;
import com.scs.basicecs.BasicECS;
import com.scs.splitscreenfps.game.Game;
import com.scs.splitscreenfps.game.components.towerdefence.CanBuildComponent;
import com.scs.splitscreenfps.game.components.towerdefence.CanBuildOnComponent;
import com.scs.splitscreenfps.game.components.towerdefence.ShowFloorSelectorComponent;
import com.scs.splitscreenfps.game.components.towerdefence.TowerDefencePlayerData;
import com.scs.splitscreenfps.game.entities.AbstractPlayersAvatar;
import com.scs.splitscreenfps.game.entities.TextEntity;
import com.scs.splitscreenfps.game.entities.towerdefence.TowerDefenceEntityFactory;

public class BuildDefenceSystem extends AbstractSystem {

	private static final int TOWER_COST = 5;
	private static final int WALL_COST = 2;

	private Game game;

	public BuildDefenceSystem(BasicECS ecs, Game _game) {
		super(ecs, CanBuildComponent.class);

		game = _game;
	}


	@Override
	public void processEntity(AbstractEntity carrier) {
		CanBuildComponent cc = (CanBuildComponent)carrier.getComponent(CanBuildComponent.class);
		if (cc.lastBuildTime + 1000 > System.currentTimeMillis()) {
			return;
		}

		AbstractPlayersAvatar player = (AbstractPlayersAvatar)carrier;
		ShowFloorSelectorComponent sfsc = (ShowFloorSelectorComponent)carrier.getComponent(ShowFloorSelectorComponent.class);

		if (player.inputMethod.isCirclePressed()) {
			TowerDefencePlayerData playerData = (TowerDefencePlayerData)carrier.getComponent(TowerDefencePlayerData.class);
			if (playerData.coins >= TOWER_COST) {

				// Check we can build
				CanBuildOnComponent cbboc = (CanBuildOnComponent)game.mapData.map[sfsc.pos.x][sfsc.pos.y].entity.getComponent(CanBuildOnComponent.class);
				if (cbboc != null) {
					//game.mapData.map[sfsc.pos.x][sfsc.pos.y].entity.hideComponent(CanBuildOnComponent.class);

					AbstractEntity turret = TowerDefenceEntityFactory.createTurret(game.ecs, sfsc.pos.x, sfsc.pos.y);
					// Check map is empty
					if (game.isAreaEmpty(turret)) {//if (game.collCheckSystem.collided(turret, 0, 0, false) == false) {
						game.ecs.addEntity(turret);
						playerData.coins -= TOWER_COST;
					} else {
						TextEntity te = new TextEntity(ecs, "Area not clear", Gdx.graphics.getBackBufferHeight()/2, 4, new Color(0, 0, 0, 1), player.playerIdx, 2);
						ecs.addEntity(te);
					}
					cc.lastBuildTime = System.currentTimeMillis() + 1000;
				}
			} else {
				TextEntity te = new TextEntity(ecs, "Not enough creds", Gdx.graphics.getBackBufferHeight()/2, 4, new Color(0, 0, 0, 1), player.playerIdx, 2);
				ecs.addEntity(te);
			}

		}

		if (player.inputMethod.isTrianglePressed()) {
			TowerDefencePlayerData playerData = (TowerDefencePlayerData)carrier.getComponent(TowerDefencePlayerData.class);
			if (playerData.coins >= WALL_COST) {
				// Check map is empty
				CanBuildOnComponent cbboc = (CanBuildOnComponent)game.mapData.map[sfsc.pos.x][sfsc.pos.y].entity.getComponent(CanBuildOnComponent.class);
				if (cbboc != null) {
					game.mapData.map[sfsc.pos.x][sfsc.pos.y].entity.hideComponent(CanBuildOnComponent.class);

					AbstractEntity wall = TowerDefenceEntityFactory.createLowWall(game.ecs, sfsc.pos.x, sfsc.pos.y);
					// Check map is empty - todo - this won't work!
					if (game.isAreaEmpty(wall)) {//.collCheckSystem.collided(wall, 0, 0, false) == false) {
						game.ecs.addEntity(wall);
						playerData.coins -= WALL_COST;
					} else {
						TextEntity te = new TextEntity(ecs, "Area not clear", Gdx.graphics.getBackBufferHeight()/2, 4, new Color(0, 0, 0, 1), player.playerIdx, 2);
						ecs.addEntity(te);
					}
				}
				cc.lastBuildTime = System.currentTimeMillis() + 1000;
			} else {
				TextEntity te = new TextEntity(ecs, "Not enough creds", Gdx.graphics.getBackBufferHeight()/2, 4, new Color(0, 0, 0, 1), player.playerIdx, 2);
				ecs.addEntity(te);
			}

		}

	}

}
