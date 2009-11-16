package fr.crnan.videso3d.graphics;

public interface Route {

	/**
	 * Type de la route
	 */
	public static enum Type {FIR, UIR};
	
	public void setType(Type type);
}
