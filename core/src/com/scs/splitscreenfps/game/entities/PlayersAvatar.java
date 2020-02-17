package com.scs.splitscreenfps.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.scs.basicecs.AbstractEntity;
import com.scs.splitscreenfps.Settings;
import com.scs.splitscreenfps.game.CameraController;
import com.scs.splitscreenfps.game.Game;
import com.scs.splitscreenfps.game.ViewportData;
import com.scs.splitscreenfps.game.components.AnimatedComponent;
import com.scs.splitscreenfps.game.components.AnimatedForAvatarComponent;
import com.scs.splitscreenfps.game.components.CanBeHarmedComponent;
import com.scs.splitscreenfps.game.components.CanCarryComponent;
import com.scs.splitscreenfps.game.components.CollidesComponent;
import com.scs.splitscreenfps.game.components.HasModel;
import com.scs.splitscreenfps.game.components.MovementData;
import com.scs.splitscreenfps.game.components.PositionComponent;
import com.scs.splitscreenfps.game.components.monstermaze.CanUseMonsterMazeExitComponent;
import com.scs.splitscreenfps.game.input.IInputMethod;

public class PlayersAvatar extends AbstractEntity {

	private static final float moveSpeed = 1.5f;

	private Game game;
	public Camera camera;
	public CameraController cameraController;
	private Vector3 tmpVector = new Vector3();
	//private float footstepTimer;

	private IInputMethod inputMethod;

	public PlayersAvatar(Game _game, int playerIdx, ViewportData _viewportData, IInputMethod _inputMethod) {
		super(_game.ecs, PlayersAvatar.class.getSimpleName() + "_" + playerIdx);

		game = _game;
		inputMethod = _inputMethod;

		this.addComponent(new MovementData(0.5f));
		this.addComponent(new PositionComponent());
		this.addComponent(new CanCarryComponent(playerIdx));

		// Model stuff
		this.addSmooth_Male_ShirtComponents(playerIdx);

		this.addComponent(new CollidesComponent(false, .3f, Settings.PLAYER_HEIGHT, .3f));

		this.addComponent(new CanBeHarmedComponent(playerIdx));
		this.addComponent(new CanUseMonsterMazeExitComponent(playerIdx));

		camera = _viewportData.camera;

		cameraController = new CameraController(game, camera, inputMethod);
	}


	private ModelInstance addSmooth_Male_ShirtComponents(int idx) {
		AssetManager am = game.assetManager;

		am.load("models/Smooth_Male_Shirt.g3db", Model.class);
		am.finishLoading();
		Model model = am.get("models/Smooth_Male_Shirt.g3db");

		ModelInstance instance = new ModelInstance(model);

		//instance.transform.scl(.0016f);
		//instance.transform.rotate(Vector3.Y, 90f); // Model is facing the wrong way
		HasModel hasModel = new HasModel("SmoothMale", instance, -.3f, 90, 0.0016f);
		hasModel.dontDrawInViewId = idx;
		this.addComponent(hasModel);

		AnimatedForAvatarComponent avatarAnim = new AnimatedForAvatarComponent();
		avatarAnim.idle_anim = "HumanArmature|Man_Idle"; // Standing
		avatarAnim.walk_anim = "HumanArmature|Man_Walk";
		this.addComponent(avatarAnim);

		AnimationController animation = new AnimationController(instance);
		AnimatedComponent anim = new AnimatedComponent(animation, avatarAnim.idle_anim);
		anim.animationController = animation;
		this.addComponent(anim);

		return instance;
	}


	public void update() {
		checkMovementInput();
		cameraController.update();

		// Rotate model to direction of camera
		HasModel hasModel = (HasModel)getComponent(HasModel.class);
		if (hasModel != null) {
			PositionComponent pos = (PositionComponent)getComponent(PositionComponent.class);
			Vector2 v2 = new Vector2(camera.direction.x, camera.direction.z);
			pos.angle = -v2.angle();
		}
	}


	private void checkMovementInput() {
		float delta = Gdx.graphics.getDeltaTime();

		MovementData movementData = (MovementData)this.getComponent(MovementData.class);
		movementData.offset.setZero();

		if (this.inputMethod.isForwardsPressed()) {
			tmpVector.set(camera.direction);
			tmpVector.y = 0;
			movementData.offset.add(tmpVector.nor().scl(delta * moveSpeed));
		} else if (this.inputMethod.isBackwardsPressed()) {
			tmpVector.set(camera.direction);
			tmpVector.y = 0;
			movementData.offset.add(tmpVector.nor().scl(delta * -moveSpeed));
		}
		if (this.inputMethod.isStrafeLeftPressed()) {
			tmpVector.set(camera.direction).crs(camera.up);
			tmpVector.y = 0;
			movementData.offset.add(tmpVector.nor().scl(delta * -moveSpeed));
		} else if (this.inputMethod.isStrafeRightPressed()) {
			tmpVector.set(camera.direction).crs(camera.up);
			tmpVector.y = 0;
			movementData.offset.add(tmpVector.nor().scl(delta * moveSpeed));
		}

		if (this.inputMethod.isPickupDropPressed()) {
			CanCarryComponent cc = (CanCarryComponent)this.getComponent(CanCarryComponent.class);
			if (cc != null) {
				cc.wantsToCarry = true;
			}
		} else {
			CanCarryComponent cc = (CanCarryComponent)this.getComponent(CanCarryComponent.class);
			if (cc != null) {
				cc.wantsToCarry = false;
			}
		}

		PositionComponent posData = (PositionComponent)this.getComponent(PositionComponent.class);
		camera.position.set(posData.position.x, posData.position.y + (Settings.PLAYER_HEIGHT/2), posData.position.z);

		if (Settings.TEST_START_IN_WALL) {
			if (game.mapData.isMapSquareTraversable((int)posData.position.x, (int)posData.position.z) == false) {
				Settings.p("Blocked!");
			}
		}
		
		
		// Animate and footstep sfx
		AnimatedComponent anim = (AnimatedComponent)this.getComponent(AnimatedComponent.class);
		AnimatedForAvatarComponent avatarAnim = (AnimatedForAvatarComponent)this.getComponent(AnimatedForAvatarComponent.class);
		if (movementData.offset.len2() > 0) {
			if (anim != null) {
				anim.new_animation = avatarAnim.walk_anim;
			}
			/*footstepTimer += Gdx.graphics.getDeltaTime();
			if (footstepTimer > 0.45f) {
				footstepTimer -= 0.45f;
				//Game.audio.play("step");
			}*/
		} else {
			if (anim != null) {
				anim.new_animation = avatarAnim.idle_anim;
			}
		}
	}

/*
	public void renderUI(SpriteBatch batch, BitmapFont font) {
		/*TagableComponent tc = (TagableComponent)this.getComponent(TagableComponent.class);
		if (tc != null) {
			if (tc.timeLeftAsIt < 20) {
				font.setColor(1, 0, 0, 1);
			} else {
				font.setColor(0, 0, 0, 1);
			}
			font.draw(batch, "Time Left: " + (int)tc.timeLeftAsIt, 10, font.getLineHeight());
			font.setColor(1, 1, 1 ,1);
		}
	}
*/
}

