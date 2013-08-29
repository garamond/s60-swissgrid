import java.util.Date;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.location.Coordinates;
import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;
import javax.microedition.midlet.MIDlet;

/**
 * Adapted from:
 * http://developer.nokia.com/Community/Wiki/Finding_position_in_Java_ME
 *
 */
public class SwissGrid extends MIDlet implements CommandListener {

	private final String version = "1.0.1";
	
	private Display display;
	private Form form;
	private Command exitCommand;
	private Command refreshCommand;
	private StringItem status;
	private StringItem lv03;
	private StringItem wgs84;

	private Location location;
	private LocationProvider locationProvider;
	private Coordinates coordinates;
	private Criteria criteria;

	/**
	 * Constructor. Constructs the object and initializes displayables.
	 */
	public SwissGrid() {
		form = new Form("SwissGrid");
		
		exitCommand = new Command("Exit", Command.EXIT, 2);
		refreshCommand = new Command("Refresh", Command.OK, 1);

		status = new StringItem("SwissGrid v"+version, "Welcome");
		lv03 = new StringItem("", "");
		wgs84 = new StringItem("", "");
		
		form.append(status);
		form.append(lv03);
		form.append(wgs84);

		form.addCommand(exitCommand);
		form.addCommand(refreshCommand);
		form.setCommandListener(this);

		display = Display.getDisplay(this);
		display.setCurrent(form);

		criteria = new Criteria();
		criteria.setHorizontalAccuracy(30);
		criteria.setVerticalAccuracy(30);

		try {
			locationProvider = LocationProvider.getInstance(criteria);
		} catch (LocationException e) {
			// TODO: Handle location exception.
			return;
		}
	}

	/**
	 * From MIDlet. Called when the MIDlet is started.
	 */
	public void startApp() {
	}

	/**
	 * From MIDlet. Called to signal the MIDlet to enter the Paused state.
	 */
	public void pauseApp() {
		// No implementation required.
	}

	/**
	 * From MIDlet. Called to signal the MIDlet to terminate.
	 * 
	 * @param unconditional
	 *            whether the MIDlet has to be unconditionally terminated
	 */
	public void destroyApp(boolean unconditional) {
		// No implementation required
	}

	/**
	 * From CommandListener. Called by the system to indicate that a command has
	 * been invoked on a particular displayable.
	 * 
	 * @param cmd
	 *            the command that was invoked
	 * @param displayable
	 *            the displayable where the command was invoked
	 */
	public void commandAction(Command c, Displayable d) {
		if (c == refreshCommand) {
			displayLocation();
		} else if (c == exitCommand) {
			notifyDestroyed();
		}
	}

	private double[] getLocation() throws LocationException, InterruptedException {
		location = locationProvider.getLocation(180);
		coordinates = location.getQualifiedCoordinates();
		// Use coordinate information
		double lat = coordinates.getLatitude();
		double lon = coordinates.getLongitude();
		double alt = coordinates.getAltitude();
		return ApproxSwissProj.WGS84toLV03(lat, lon, alt);
	}
	
	private void clear() {
		status.setLabel("");
		status.setText("");
		lv03.setLabel("");
		lv03.setText("");
		wgs84.setLabel("");
		wgs84.setText("");
	}
	
	/**
	 * Called to read current location.
	 */
	private void displayLocation() {
		clear();
		status.setLabel("Processing");
		status.setText("Finding location...");
		double[] swissCoords;
		try {
			swissCoords = getLocation();
			status.setLabel("Last location:");
			status.setText(new Date().toString());
			lv03.setLabel("LV03");
			lv03.setText((int)swissCoords[0] + "\n" + (int)swissCoords[1]);
			wgs84.setLabel("WGS84");
			wgs84.setText("Lat:\n"+coordinates.getLatitude() + "\n" +
						  "Lon:\n"+coordinates.getLongitude());
		} catch (Exception e) {
			status.setLabel("Oops!");
			status.setText("Unable to determine location: "+e.getMessage());
		}
	}

}
