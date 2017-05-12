package io.anuke.ucore.lights;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

/**
 * Light is data container for all the light parameters. When created lights
 * are automatically added to rayHandler and could be removed by calling
 * {@link #remove()} and added manually by calling {@link #add(RayHandler)}.
 * 
 * <p>Implements {@link Disposable}
 * 
 * @author kalle_h
 */
public abstract class Light implements Disposable {

	static final Color DefaultColor = new Color(0.75f, 0.75f, 0.5f, 0.75f);
	static final float zeroColorBits = Color.toFloatBits(0f, 0f, 0f, 0f);
	static final int MIN_RAYS = 3;
	
	protected final Color color = new Color();
	protected final Vector2 tmpPosition = new Vector2();
	
	protected RayHandler rayHandler;
	
	protected boolean active = true;
	protected boolean soft = true;
	protected boolean xray = false;
	protected boolean staticLight = false;
	protected boolean culled = false;
	protected boolean dirty = true;
	protected boolean ignoreBody = false;

	protected int rayNum;
	protected int vertexNum;
	
	protected float distance;
	protected float direction;
	protected float colorF;
	protected float softShadowLength = 2.5f;
	
	protected float noisetime = 4f;
	protected float noisescl = 1f;
	protected float noisemag = 1.2f;
	
	protected Mesh lightMesh;
	protected Mesh softShadowMesh;

	protected float segments[];
	protected float[] mx;
	protected float[] my;
	protected float[] f;
	protected int m_index = 0;

	/** 
	 * Creates new active light and automatically adds it to the specified
	 * {@link RayHandler} instance.
	 * 
	 * @param rayHandler
	 *            not null instance of RayHandler
	 * @param rays
	 *            number of rays - more rays make light to look more realistic
	 *            but will decrease performance, can't be less than MIN_RAYS
	 * @param color
	 *            light color
	 * @param distance
	 *            light distance (if applicable), soft shadow length is set to distance * 0.1f
	 * @param directionDegree
	 *            direction in degrees (if applicable) 
	 */
	public Light(RayHandler rayHandler, int rays, Color color,
			float distance, float directionDegree) {
		rayHandler.lightList.add(this);
		this.rayHandler = rayHandler;
		setRayNum(rays);
		setColor(color);
		setDistance(distance);
		setSoftnessLength(distance * 0.1f);
		setDirection(directionDegree);
	}
	
	public void setNoise(float time, float scl, float mag){
		this.noisetime = time;
		this.noisescl = scl;
		this.noisemag = 0f;
	}

	/**
	 * Updates this light
	 */
	abstract void update();

	/**
	 * Render this light
	 */
	abstract void render();
	
	/**
	 * Sets light distance
	 * 
	 * <p>NOTE: MIN value should be capped to 0.1f meter
	 */
	public abstract void setDistance(float dist);

	/**
	 * Sets light direction
	 */
	public abstract void setDirection(float directionDegree);

	/**
	 * Sets light starting position
	 * 
	 * @see #setPosition(Vector2)
	 */
	public abstract void setPosition(float x, float y);

	/**
	 * Sets light starting position
	 * 
	 * @see #setPosition(float, float)
	 */
	public abstract void setPosition(Vector2 position);

	/**
	 * @return horizontal starting position of light in world coordinates
	 */
	public abstract float getX();

	/**
	 * @return vertical starting position of light in world coordinates
	 */
	public abstract float getY();
	
	/**
	 * @return starting position of light in world coordinates
	 *         <p>NOTE: changing this vector does nothing
	 */
	public Vector2 getPosition() {
		return tmpPosition;
	}
	
	/**
	 * Sets light color
	 * 
	 * <p>NOTE: you can also use colorless light with shadows, e.g. (0,0,0,1)
	 * 
	 * @param newColor
	 *            RGB set the color and Alpha set intensity
	 * 
	 * @see #setColor(float, float, float, float)
	 */
	public void setColor(Color newColor) {
		if (newColor != null) {
			color.set(newColor);
		} else {
			color.set(DefaultColor);
		}
		colorF = color.toFloatBits();
		if (staticLight) dirty = true;
	}

	/**
	 * Sets light color
	 * 
	 * <p>NOTE: you can also use colorless light with shadows, e.g. (0,0,0,1)
	 * 
	 * @param r
	 *            lights color red component
	 * @param g
	 *            lights color green component
	 * @param b
	 *            lights color blue component
	 * @param a
	 *            lights shadow intensity
	 * 
	 * @see #setColor(Color)
	 */
	public void setColor(float r, float g, float b, float a) {
		color.set(r, g, b, a);
		colorF = color.toFloatBits();
		if (staticLight) dirty = true;
	}
	
	/**
	 * Adds light to specified RayHandler
	 */
	public void add(RayHandler rayHandler) {
		this.rayHandler = rayHandler;
		if (active) {
			rayHandler.lightList.add(this);
		} else {
			rayHandler.disabledLights.add(this);
		}
	}

	/**
	 * Removes light from specified RayHandler and disposes it
	 */
	public void remove() {
		remove(true);
	}
	
	/**
	 * Removes light from specified RayHandler and disposes it if requested
	 */
	public void remove(boolean doDispose) {
		if (active) {
			rayHandler.lightList.removeValue(this, false);
		} else {
			rayHandler.disabledLights.removeValue(this, false);
		}
		rayHandler = null;
		if (doDispose) dispose();
	}

	/**
	 * Disposes all light resources
	 */
	public void dispose() {
		lightMesh.dispose();
		softShadowMesh.dispose();
	}

	/**
	 * @return if this light is active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Enables/disables this light update and rendering
	 */
	public void setActive(boolean active) {
		if (active == this.active)
			return;

		this.active = active;
		if (rayHandler == null)
			return;
		
		if (active) {
			rayHandler.lightList.add(this);
			rayHandler.disabledLights.removeValue(this, true);
		} else {
			rayHandler.disabledLights.add(this);
			rayHandler.lightList.removeValue(this, true);
		}
	}

	/**
	 * @return if this light beams go through obstacles
	 */
	public boolean isXray() {
		return xray;
	}

	/**
	 * Enables/disables x-ray beams for this light
	 * 
	 * <p>Enabling this will allow beams go through obstacles that reduce CPU
	 * burden of light about 70%.
	 * 
	 * <p>Use the combination of x-ray and non x-ray lights wisely
	 */
	public void setXray(boolean xray) {
		this.xray = xray;
		if (staticLight) dirty = true;
	}

	/**
	 * @return if this light is static
	 *         <p>Static light do not get any automatic updates but setting
	 *         any parameters will update it. Static lights are useful for
	 *         lights that you want to collide with static geometry but ignore
	 *         all the dynamic objects.
	 */
	public boolean isStaticLight() {
		return staticLight;
	}

	/**
	 * Enables/disables this light static behavior
	 * 
	 * <p>Static light do not get any automatic updates but setting any
	 * parameters will update it. Static lights are useful for lights that you
	 * want to collide with static geometry but ignore all the dynamic objects
	 * 
	 * <p>Reduce CPU burden of light about 90%
	 */
	public void setStaticLight(boolean staticLight) {
		this.staticLight = staticLight;
		if (staticLight) dirty = true;
	}

	/**
	 * @return if tips of this light beams are soft
	 */
	public boolean isSoft() {
		return soft;
	}

	/**
	 * Enables/disables softness on tips of this light beams
	 */
	public void setSoft(boolean soft) {
		this.soft = soft;
		if (staticLight) dirty = true;
	}

	/**
	 * @return softness value for beams tips
	 *         <p>Default: {@code 2.5f}
	 */
	public float getSoftShadowLength() {
		return softShadowLength;
	}

	/**
	 * Sets softness value for beams tips
	 * 
	 * <p>Default: {@code 2.5f}
	 */
	public void setSoftnessLength(float softShadowLength) {
		this.softShadowLength = softShadowLength;
		if (staticLight) dirty = true;
	}
	
	/**
	 * @return current color of this light
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @return rays distance of this light (without gamma correction)
	 */
	public float getDistance() {
		return distance / RayHandler.gammaCorrectionParameter;
	}


	/**
	 * @return direction in degrees (0 if not applicable)
	 */
	public float getDirection(){
		return direction;
	}

	/**
	 * Checks if given point is inside of this light area
	 * 
	 * @param x - horizontal position of point in world coordinates
	 * @param y - vertical position of point in world coordinates
	 */
	public boolean contains(float x, float y) {
		return false;
	}
	
	/**
	 * Sets if the attached body fixtures should be ignored during raycasting
	 * 
	 * @param flag - if {@code true} all the fixtures of attached body
	 *               will be ignored and will not create any shadows for this
	 *               light. By default is set to {@code false}. 
	 */
	public void setIgnoreAttachedBody(boolean flag) {
		ignoreBody = flag;
	}
	
	/**
	 * @return if the attached body fixtures will be ignored during raycasting
	 */
	public boolean getIgnoreAttachedBody() {
		return ignoreBody;
	}
	
	/**
	 * Internal method for mesh update depending on ray number
	 */
	void setRayNum(int rays) {
		if (rays < MIN_RAYS)
			rays = MIN_RAYS;

		rayNum = rays;
		vertexNum = rays + 1;

		segments = new float[vertexNum * 8];
		mx = new float[vertexNum];
		my = new float[vertexNum];
		f = new float[vertexNum];
	}
	
	/** 
	 * @return number of rays set for this light
	 */
	public int getRayNum(){
		return rayNum;
	}
	
}
