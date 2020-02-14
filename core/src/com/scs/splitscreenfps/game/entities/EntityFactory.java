package com.scs.splitscreenfps.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.scs.basicecs.AbstractEntity;
import com.scs.basicecs.BasicECS;
import com.scs.splitscreenfps.game.components.CanBeCarried;
import com.scs.splitscreenfps.game.components.CollidesComponent;
import com.scs.splitscreenfps.game.components.HasDecal;
import com.scs.splitscreenfps.game.components.HasGuiSpriteComponent;
import com.scs.splitscreenfps.game.components.HasModel;
import com.scs.splitscreenfps.game.components.PositionComponent;
import com.scs.splitscreenfps.game.components.RemoveEntityAfterTimeComponent;
import com.scs.splitscreenfps.game.components.litter.CombinesWithLitterComponent;
import com.scs.splitscreenfps.game.levels.MonsterMazeLevel;

import ssmith.lang.NumberFunctions;

public class EntityFactory {
	
	private BasicECS ecs;

	public EntityFactory(BasicECS _ecs) {
		this.ecs = _ecs;
	}
	
	
	public AbstractEntity createCrate(float map_x, float map_z) {
		float SIZE = 0.3f;
		AbstractEntity entity = new AbstractEntity(ecs, "Crate");

		Material black_material = new Material(TextureAttribute.createDiffuse(new Texture("tag/scifi_crate.jpg")));
		ModelBuilder modelBuilder = new ModelBuilder();
		Model box_model = modelBuilder.createBox(SIZE, SIZE, SIZE, black_material, VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates);

		PositionComponent posData = new PositionComponent(map_x+(SIZE/2), SIZE/2, map_z+(SIZE/2));
		entity.addComponent(posData);

		ModelInstance instance = new ModelInstance(box_model, new Vector3(map_x+SIZE/2, SIZE/2, map_z+SIZE/2));
		//instance.transform.rotate(Vector3.Z, 90); // Rotates cube so textures are upright
		//instance.transform.rotate(Vector3.Y, NumberFunctions.rnd(0, 90));

		HasModel model = new HasModel(this.getClass().getSimpleName(), instance);
		//model.yOffset += SIZE/2;
		model.angleOffset = NumberFunctions.rnd(0, 90);
		entity.addComponent(model);

        CollidesComponent cc = new CollidesComponent(true, instance);
        entity.addComponent(cc);	

		return entity;	
		
	}


	public AbstractEntity createDoor(float map_x, float map_z, boolean rot90) {
		AbstractEntity entity = new AbstractEntity(ecs, "Door");

		PositionComponent posData = new PositionComponent((map_x)+(0.5f), (map_z)+(0.5f));
		entity.addComponent(posData);

		HasDecal hasDecal = new HasDecal();
		Texture tex = new Texture(Gdx.files.internal("sf/door1.jpg"));
		TextureRegion tr = new TextureRegion(tex, 0, 0, tex.getWidth(), tex.getHeight());
        hasDecal.decal = Decal.newDecal(tr, true);
        hasDecal.decal.setScale(1f / tr.getRegionWidth());
        hasDecal.decal.setPosition(posData.position);
        hasDecal.faceCamera = false;
        hasDecal.dontLockYAxis = false;        
        entity.addComponent(hasDecal);	
		
        CollidesComponent cc = new CollidesComponent(true, .5f);
        entity.addComponent(cc);	
		
		return entity;	
		
	}


	public AbstractEntity createLitter(int type, float map_x, float map_z) {
		AbstractEntity entity = new AbstractEntity(ecs, "Litter");

		PositionComponent posData = new PositionComponent((map_x)+(0.5f), (map_z)+(0.5f));
		entity.addComponent(posData);

		HasDecal hasDecal = new HasDecal();
		Texture tex = new Texture(Gdx.files.internal("heart.png")); // todo
		TextureRegion tr = new TextureRegion(tex, 0, 0, tex.getWidth(), tex.getHeight());
        hasDecal.decal = Decal.newDecal(tr, true);
        hasDecal.decal.setScale(1f / tr.getRegionWidth());
        hasDecal.decal.setPosition(posData.position);
        hasDecal.faceCamera = true;
        hasDecal.dontLockYAxis = true;
        entity.addComponent(hasDecal);	
		
        entity.addComponent(new CombinesWithLitterComponent(true, type));
        
        CollidesComponent cc = new CollidesComponent(false, .5f);
        entity.addComponent(cc);	
		
        entity.addComponent(new CanBeCarried());

        Texture weaponTex = new Texture(Gdx.files.internal("heart.png"));		
		Sprite sprite = new Sprite(weaponTex);
		sprite.setOrigin(sprite.getWidth()/2f, 0);
		//weaponSprite.setScale(7.5f, 5f);
		//float scale = (float)Settings.WINDOW_WIDTH_PIXELS / (float)weaponTex.getWidth() / 3f;
		sprite.setScale(10);
		//sprite.setPosition((Gdx.graphics.getWidth()-sprite.getWidth())/2, 0);		
		sprite.setPosition(100, 100);		
		HasGuiSpriteComponent hgsc = new HasGuiSpriteComponent(sprite, HasGuiSpriteComponent.Z_CARRIED);
        entity.addComponent(hgsc);
        entity.hideComponent(HasGuiSpriteComponent.class); // Don't show it until picked up!
		
		return entity;	
		
	}


	public AbstractEntity createLitterBin(int type, float map_x, float map_z) {
		AbstractEntity entity = new AbstractEntity(ecs, "LitterBin");

		PositionComponent posData = new PositionComponent((map_x)+(0.5f), (map_z)+(0.5f));
		entity.addComponent(posData);

		HasDecal hasDecal = new HasDecal();
		Texture tex = new Texture(Gdx.files.internal("monstermaze/exit1.png")); // todo
		TextureRegion tr = new TextureRegion(tex, 0, 0, tex.getWidth(), tex.getHeight());
        hasDecal.decal = Decal.newDecal(tr, true);
        hasDecal.decal.setScale(1f / tr.getRegionWidth());
        hasDecal.decal.setPosition(posData.position);
        hasDecal.faceCamera = true;
        hasDecal.dontLockYAxis = true;        
        entity.addComponent(hasDecal);	
		
        entity.addComponent(new CombinesWithLitterComponent(false, type));
		
        CollidesComponent cc = new CollidesComponent(false, .5f);
        entity.addComponent(cc);	
		
		return entity;	
		
	}


	public AbstractEntity createRedFilter(int viewId) {
		AbstractEntity entity = new AbstractEntity(ecs, "RedFilter");

        Texture weaponTex = new Texture(Gdx.files.internal("colours/white.png"));		
		Sprite sprite = new Sprite(weaponTex);
		//sprite.setSize(rect.width, rect.height);
		sprite.setSize(Gdx.graphics.getWidth(),  Gdx.graphics.getHeight());
		//sprite.setPosition((Gdx.graphics.getWidth()-sprite.getWidth())/2, 0);		
		//sprite.setPosition(rect.x, rect.y);
		sprite.setColor(1, 0, 0, .5f);
		
		HasGuiSpriteComponent hgsc = new HasGuiSpriteComponent(sprite, HasGuiSpriteComponent.Z_FILTER);
        entity.addComponent(hgsc);
        hgsc.onlyViewId = viewId;
        
        RemoveEntityAfterTimeComponent rat = new RemoveEntityAfterTimeComponent(3);
        entity.addComponent(rat);
		
		return entity;	
		
	}


	public AbstractEntity createKey(float map_x, float map_z) {
		AbstractEntity entity = new AbstractEntity(ecs, MonsterMazeLevel.KEY_NAME);

		PositionComponent posData = new PositionComponent((map_x)+(0.5f), (map_z)+(0.5f));
		entity.addComponent(posData);

		HasDecal hasDecal = new HasDecal();
		Texture tex = new Texture(Gdx.files.internal("monstermaze/key.png"));
		TextureRegion tr = new TextureRegion(tex, 0, 0, tex.getWidth(), tex.getHeight());
        hasDecal.decal = Decal.newDecal(tr, true);
        hasDecal.decal.setScale(1f / tr.getRegionWidth());
        hasDecal.decal.setPosition(posData.position);
        hasDecal.faceCamera = true;
        hasDecal.dontLockYAxis = true;
        entity.addComponent(hasDecal);
        
        Texture weaponTex = new Texture(Gdx.files.internal("monstermaze/key.png"));		
		Sprite sprite = new Sprite(weaponTex);
		sprite.setOrigin(sprite.getWidth()/2f, 0);
		//weaponSprite.setScale(7.5f, 5f);
		//float scale = (float)Settings.WINDOW_WIDTH_PIXELS / (float)weaponTex.getWidth() / 3f;
		sprite.setScale(3);
		sprite.setPosition((Gdx.graphics.getWidth()-sprite.getWidth())/2, 0);		
		//sprite.setPosition(100, 100);		
		HasGuiSpriteComponent hgsc = new HasGuiSpriteComponent(sprite, HasGuiSpriteComponent.Z_CARRIED);
        entity.addComponent(hgsc);
        entity.hideComponent(HasGuiSpriteComponent.class); // Don't show it until picked up!

        /*IsCollectableComponent ic = new IsCollectableComponent();
        ic.disappearsWhenCollected = false;
        entity.addComponent(ic);*/

        CollidesComponent cc = new CollidesComponent(false, .5f);
        entity.addComponent(cc);	
		
        CanBeCarried cbc = new CanBeCarried();
        cbc.auto_pickedup = true;
        entity.addComponent(cbc);

        return entity;	
		
	}


}
