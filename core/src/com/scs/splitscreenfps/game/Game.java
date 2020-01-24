package com.scs.splitscreenfps.game;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import com.bitfire.postprocessing.PostProcessing;
import com.scs.basicecs.BasicECS;
import com.scs.splitscreenfps.Audio;
import com.scs.splitscreenfps.IModule;
import com.scs.splitscreenfps.Settings;
import com.scs.splitscreenfps.game.components.PositionData;
import com.scs.splitscreenfps.game.input.IInputMethod;
import com.scs.splitscreenfps.game.input.MouseAndKeyboardInputMethod;
import com.scs.splitscreenfps.game.input.NoInputMethod;
import com.scs.splitscreenfps.game.levels.AbstractLevel;
import com.scs.splitscreenfps.game.levels.MonsterMazeLevel;
import com.scs.splitscreenfps.game.player.Player;
import com.scs.splitscreenfps.game.systems.CollectionSystem;
import com.scs.splitscreenfps.game.systems.CollisionCheckSystem;
import com.scs.splitscreenfps.game.systems.CycleThroughModelsSystem;
import com.scs.splitscreenfps.game.systems.CycleThruDecalsSystem;
import com.scs.splitscreenfps.game.systems.DrawDecalSystem;
import com.scs.splitscreenfps.game.systems.DrawModelSystem;
import com.scs.splitscreenfps.game.systems.DrawTextSystem;
import com.scs.splitscreenfps.game.systems.InputSystem;
import com.scs.splitscreenfps.game.systems.MobAISystem;
import com.scs.splitscreenfps.game.systems.MovementSystem;
import com.scs.splitscreenfps.game.systems.RemoveAfterTimeSystem;

public class Game implements IModule {

	public static final float UNIT = 16f; // Square/box size - todo - remove

	public static final Art art = new Art();
	public static final Audio audio = new Audio();

	private SpriteBatch batch2d;
	private final BitmapFont font_white, font_black;
	private ModelBatch batch;
	public final ViewportData[] viewports;

	public Player[] players;
	public MapData mapData;
	public BasicECS ecs;

	private AbstractLevel gameLevel;

	private PostProcessing post; // todo - add
	
	public int viewid; // todo - rename
	
	public Game() {
		batch2d = new SpriteBatch();
		font_white = new BitmapFont(Gdx.files.internal("font/spectrum1white.fnt"));
		font_black = new BitmapFont(Gdx.files.internal("font/spectrum1black.fnt"));

		batch = new ModelBatch();

		viewports = new ViewportData[4];
		players = new Player[4];
		for (int i=0 ; i<viewports.length ; i++) {
			this.viewports[i] = new ViewportData(false, i);
			IInputMethod input = i==0 ? new MouseAndKeyboardInputMethod() : new NoInputMethod();
			players[i] = new Player(i, this.viewports[i], input);
		}

		mapData = new MapData();

		this.createECS();

		gameLevel = new MonsterMazeLevel(this);
		loadLevel();
		
		for (int i=0 ; i<4 ; i++) {
			ecs.addEntity(players[i]);
		}

		if (Gdx.app.getType() != ApplicationType.WebGL) {
			//post = new PostProcessing();
		}

	}


	public void resizeViewports(boolean full_screen) {
		for (int i=0 ; i<viewports.length ; i++) {
			this.viewports[i].resize(full_screen, i);
		}
	}


	private void createECS() {
		ecs = new BasicECS();
		ecs.addSystem(new InputSystem(this));
		ecs.addSystem(new CollisionCheckSystem(ecs));
		ecs.addSystem(new DrawDecalSystem(this, ecs));
		ecs.addSystem(new CycleThruDecalsSystem(ecs));
		ecs.addSystem(new CycleThroughModelsSystem(ecs));
		ecs.addSystem(new MobAISystem(this, ecs));
		ecs.addSystem(new MovementSystem(this, ecs));
		ecs.addSystem(new DrawModelSystem(ecs, batch));
		ecs.addSystem(new RemoveAfterTimeSystem(ecs));
		ecs.addSystem(new CollectionSystem(ecs));
		ecs.addSystem(new DrawTextSystem(ecs, batch2d, font_white));
	}


	@Override
	public void render() {
		this.ecs.getSystem(RemoveAfterTimeSystem.class).process();
		this.ecs.addAndRemoveEntities();
		this.ecs.getSystem(InputSystem.class).process();
		this.ecs.getSystem(MobAISystem.class).process();
		this.ecs.getSystem(MovementSystem.class).process();
		this.ecs.getSystem(CollectionSystem.class).process();
		gameLevel.update(this, mapData);

		if (post != null) {
			post.update(Gdx.graphics.getDeltaTime());
		}

		for (viewid=0 ; viewid<viewports.length ; viewid++) {
			ViewportData viewportData = this.viewports[viewid];
			Gdx.gl.glViewport(viewportData.viewPos.x, viewportData.viewPos.y, viewportData.viewPos.width, viewportData.viewPos.height);

			viewportData.frameBuffer.begin();
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
			this.gameLevel.setBackgroundColour();

			batch.begin(viewportData.camera);
			if (ecs != null) {
				this.ecs.getSystem(DrawModelSystem.class).process();
			}
			batch.end();

			if (ecs != null) {
				this.ecs.getSystem(CycleThruDecalsSystem.class).process();
				this.ecs.getSystem(DrawDecalSystem.class).process();
				this.ecs.getSystem(CycleThroughModelsSystem.class).process();
			}

			if (ecs != null) {
				batch2d.begin();
				this.ecs.getSystem(DrawTextSystem.class).process();
				batch2d.end();
			}

			viewportData.frameBuffer.end();

			if (post != null) {
				post.begin();
			}

			//Draw buffer and FPS
			batch2d.begin();

			float c = 1.0f;
			batch2d.setColor(c,c,c,1);
			//batch2d.draw(frameBuffer.getColorBufferTexture(), 0, Gdx.graphics.getHeight(), Gdx.graphics.getWidth(), - Gdx.graphics.getHeight());
			//batch2d.draw(viewportData.frameBuffer.getColorBufferTexture(), 0, viewportData.viewPos.height, viewportData.viewPos.width, -viewportData.viewPos.height);
			//batch2d.draw(viewportData.frameBuffer.getColorBufferTexture(), viewportData.viewPos.x, viewportData.viewPos.y);
			batch2d.draw(viewportData.frameBuffer.getColorBufferTexture(), viewportData.viewPos.x, viewportData.viewPos.y+viewportData.viewPos.height, viewportData.viewPos.width, -viewportData.viewPos.height);

				if (players[viewid] != null) {
					players[viewid].renderUI(batch2d, font_white);
				}
				
			gameLevel.renderUI(batch2d, font_white, font_black);

			if (Settings.SHOW_FPS) {
				font_white.draw(batch2d, "FPS: "+Gdx.graphics.getFramesPerSecond(), 10, 20);
			}

			batch2d.end();
			if (post != null) {
				post.end();
			}
		}
	}


	private void loadLevel() {
		gameLevel.load();

		if (gameLevel.getPlayerStartMapX() < 0 || gameLevel.getPlayerStartMapY() < 0) {
			throw new RuntimeException ("No player start position set");
		}
		PositionData posData = (PositionData)this.players[0].getComponent(PositionData.class); // todo - diff start positions
		posData.position.set(gameLevel.getPlayerStartMapX()*Game.UNIT+(Game.UNIT/2), 0, gameLevel.getPlayerStartMapY()*Game.UNIT+(Game.UNIT/2)); // Start in middle of square

		for (int i=0 ; i<4 ; i++) { // todo - remove?
			players[i].update();
		}

		for (int viewid=0 ; viewid<viewports.length ; viewid++) {
			ViewportData viewport = this.viewports[viewid];
			Camera camera = viewport.camera;
			camera.rotate(Vector3.Y, (float)Math.toDegrees(Math.atan2(camera.direction.z, camera.direction.x)));
			//this.viewports[0].camera.rotate(Vector3.Y, (float)Math.toDegrees(Math.atan2(camera.direction.z, camera.direction.x)));

			camera.update();
			//this.viewports[0].camera.update();
		}
	}


	@Override
	public void resize(int w, int h) {
		//this.resizeViewports(false);
	}


	@Override
	public void dispose() {
		if (post != null) {
			post.dispose();
		}
		font_white.dispose(); 
		font_black.dispose();
		audio.dipose();
		batch.dispose();
		batch2d.dispose();
	}


	/*	@Override
	public boolean isFinished() {
		return false; // Never finishes
	}
	 */

	@Override
	public void setFullScreen(boolean fullscreen) {
		batch2d.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		this.resizeViewports(true);
	}

}

