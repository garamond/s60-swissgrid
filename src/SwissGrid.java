import java.io.OutputStream;
import java.util.Date;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
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

	private final String version = "1.1.1";

	private Display display;
	private Form form;
	private Command exitCommand;
	private Command saveCommand;
	private Command refreshCommand;
	private StringItem status;
	private StringItem lv03;
	private StringItem wgs84;

	private Location location;
	private double[] swissCoords;

	private LocationProvider locationProvider;
	private Criteria criteria;

	/**
	 * Constructor. Constructs the object and initializes displayables.
	 */
	public SwissGrid() {
		form = new Form("SwissGrid");

		exitCommand = new Command("Exit", Command.EXIT, 2);
		saveCommand = new Command("Save", Command.OK, 3);
		refreshCommand = new Command("Refresh", Command.OK, 1);

		status = new StringItem("SwissGrid v" + version, "Welcome");
		lv03 = new StringItem("", "");
		wgs84 = new StringItem("", "");

		form.append(status);
		form.append(lv03);
		form.append(wgs84);

		form.addCommand(exitCommand);
		form.addCommand(refreshCommand);
		form.addCommand(saveCommand);
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
		displayLocation();
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
		} else if (c == saveCommand) {
			saveLocation();
		} else if (c == exitCommand) {
			notifyDestroyed();
		}
	}

	private void updateLocation() throws LocationException,
			InterruptedException {
		location = locationProvider.getLocation(600);
		Coordinates coordinates = location.getQualifiedCoordinates();
		swissCoords = ApproxSwissProj.WGS84toLV03(coordinates.getLatitude(),
				coordinates.getLongitude(), coordinates.getAltitude());
	}

	private void saveLocation() {
		Coordinates coords = location.getQualifiedCoordinates();
		try {
			String string = location.getTimestamp() + ","
					+ coords.getLatitude() + "," + coords.getLongitude() + ","
					+ coords.getAltitude() + "," + swissCoords[0] + ","
					+ swissCoords[1] + "\n";
			byte data[] = string.getBytes();
			FileConnection fconn = (FileConnection) Connector.open(
					"file:///E:/swissgrid.txt", Connector.READ_WRITE);
			if (!fconn.exists()) {
				fconn.create();
			}
			OutputStream ops = fconn.openOutputStream(fconn.fileSize());
			ops.write(data);
			ops.close();
			fconn.close();
			status.setLabel("Success");
			status.setText("Saved location");
		} catch (Exception e) {
			status.setLabel("Error");
			status.setText("Failed to save location: " + e.getMessage());
		}
	}

	private void clearDisplay() {
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
		clearDisplay();
		status.setLabel("Processing");
		status.setText("Updating location...");
		try {
			updateLocation();
			status.setLabel("Last location");
			status.setText(new Date(location.getTimestamp()).toString());
			lv03.setLabel("CH1903");
			lv03.setText((int) swissCoords[0] + "\n" + (int) swissCoords[1]);
			wgs84.setLabel("WGS84");
			wgs84.setText(location.getQualifiedCoordinates().getLatitude()
					+ "N\n" + location.getQualifiedCoordinates().getLongitude()
					+ "E");
		} catch (Exception e) {
			status.setLabel("Error");
			status.setText("Unable to determine location: " + e.getMessage());
		}
	}

}
