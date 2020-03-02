package com.scs.splitscreenfps.game.entities;

import com.badlogic.gdx.graphics.Camera;
import com.scs.basicecs.AbstractEntity;
import com.scs.basicecs.BasicECS;
import com.scs.splitscreenfps.game.Game;
import com.scs.splitscreenfps.game.input.IInputMethod;

public abstract class AbstractPlayersAvatar extends AbstractEntity {

	protected Game game;
	public Camera camera;
	protected IInputMethod inputMethod;

	public AbstractPlayersAvatar(BasicECS _ecs, String _name) {
		super(_ecs, _name);
	}

	
	public abstract void update();
	
}