package edu.gatech.grits.mdln.lang.util;

public class ControlParam {

	private float translationVel;
	private float rotationVel;
	
	
	
	/**
	 * 
	 */
	public ControlParam() {
		super();
		translationVel = 0;
		rotationVel = 0;
	}
	public ControlParam(float translationVel, float rotationVel) {
		super();
		this.translationVel = translationVel;
		this.rotationVel = rotationVel;
	}
	public float getTranslationVel() {
		return translationVel;
	}
	public float getRotationVel() {
		return rotationVel;
	}
	
	public void setTranslationVel(float translationVel) {
		this.translationVel = translationVel;
	}
	public void setRotationVel(float rotationVel) {
		this.rotationVel = rotationVel;
	}
	@Override
	public String toString() {
		String str = "(trans=";
		str += this.translationVel + ", rotation=" + this.rotationVel + ")";
		return str;
	}
	
	
}
