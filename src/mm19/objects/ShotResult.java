package mm19.objects;

public class ShotResult extends ActionResult {

	public int x;
	public int y;
	
	/**
	 * Creates a new ShotResult from an ActionResult.
	 * The x of the shot.
	 * The y of the shot.
	 * 
	 * @param ar
	 */
	public ShotResult(ActionResult ar, int x, int y) {
		super(ar.ID, ar.result);
		this.x = x;
		this.y = y;
	}
	
}
