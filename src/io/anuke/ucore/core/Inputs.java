package io.anuke.ucore.core;

import com.badlogic.gdx.*;

public class Inputs{
	private static boolean[] buttons = new boolean[5];
	private static InputMultiplexer plex = new InputMultiplexer();
	private static int scroll = 0;
	private static InputProcessor listen = new InputAdapter(){
		@Override
		public boolean scrolled(int amount){
			scroll = -amount;
			return false;
		}
	};
	
	static{
		plex.addProcessor(listen);
	}
	
	public static void update(){
		for(int i = 0; i < buttons.length; i ++){
			buttons[i] = Gdx.input.isButtonPressed(i);
		}
		scroll = 0;
	}
	
	/**Adds another input processor to the chain.*/
	public static void addProcessor(InputProcessor listener){
		plex.addProcessor(listener);
		Gdx.input.setInputProcessor(plex);
	}
	
	public static void flipProcessors(){
		plex.getProcessors().reverse();
	}
	
	public static boolean keyDown(int key){
		return Gdx.input.isKeyPressed(key);
	}
	
	public static boolean keyUp(int key){
		return Gdx.input.isKeyJustPressed(key);
	}
	
	public static boolean keyDown(String name){
		return keyDown(KeyBinds.get(name));
	}
	
	public static boolean keyUp(String name){
		return keyUp(KeyBinds.get(name));
	}
	
	public static boolean buttonDown(int button){
		return Gdx.input.isButtonPressed(button);
	}
	
	public static boolean buttonUp(int button){
		return Gdx.input.isButtonPressed(button) && !buttons[button];
	}
	
	public static boolean buttonRelease(int button){
		return !Gdx.input.isButtonPressed(button) && buttons[button];
	}
	
	public static int scroll(){
		return scroll;
	}
	
	public static boolean scrolled(){
		return Math.abs(scroll) > 0;
	}
}
