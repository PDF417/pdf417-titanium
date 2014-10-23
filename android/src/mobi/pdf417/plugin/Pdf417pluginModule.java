package mobi.pdf417.plugin;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;

import org.appcelerator.titanium.util.TiActivitySupport;
import org.appcelerator.titanium.util.TiActivityResultHandler;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;
import org.appcelerator.kroll.KrollDict;

import mobi.pdf417.activity.Pdf417ScanActivity;
import mobi.pdf417.Pdf417MobiSettings;
import mobi.pdf417.Pdf417MobiScanData;
import mobi.pdf417.plugin.RHelper;

import net.photopay.barcode.BarcodeDetailedData;
import net.photopay.base.BaseBarcodeActivity;
import net.photopay.hardware.camera.CameraType;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@Kroll.module(name="Pdf417plugin", id="mobi.pdf417.plugin")
public class Pdf417pluginModule extends KrollModule implements TiActivityResultHandler
{

	// Standard Debugging variables
	private static final String LCAT = "Pdf417pluginModule";
	private static final boolean DBG = TiConfig.LOGD;

	@Kroll.constant public static final String PDF417 = "PDF417";
	@Kroll.constant public static final String QR_CODE = "QR Code";
	@Kroll.constant public static final String CODE_128 = "Code 128";
	@Kroll.constant public static final String CODE_39 = "Code 39";
	@Kroll.constant public static final String EAN_13 = "EAN 13";
	@Kroll.constant public static final String EAN_8 = "EAN 8";
	@Kroll.constant public static final String ITF = "ITF";
	@Kroll.constant public static final String UPC_A = "UPCA";
	@Kroll.constant public static final String UPC_E = "UPCE";

	@Kroll.constant private static final String CANCELLED = "cancelled";
	@Kroll.constant private static final String RESULT_LIST = "resultList";
	@Kroll.constant private static final String TYPE = "type";
	@Kroll.constant private static final String DATA = "data";
	@Kroll.constant private static final String RAW_DATA = "raw";

	public Pdf417pluginModule()
	{
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
		Log.d(LCAT, "onAppCreate");
	}

	@Kroll.method
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void scan(@Kroll.argument(optional = false) HashMap args) {
	
		Log.d(LCAT, "scan called with arguments:" + args);

		TiApplication appContext = TiApplication.getInstance();
		Intent intent = new Intent(appContext, Pdf417ScanActivity.class);
		Pdf417MobiSettings sett = new Pdf417MobiSettings();
		
		// Parse scan arguments into settings and intent extras
		parseArguments(args, sett, intent);

		// put settings as intent extra
		intent.putExtra(BaseBarcodeActivity.EXTRAS_SETTINGS, sett);
		
		Activity activity = TiApplication.getAppCurrentActivity();
		TiActivitySupport activitySupport = (TiActivitySupport) activity;
		
		final int resultCode = activitySupport.getUniqueResultCode();
		activitySupport.launchActivityForResult(intent, resultCode, this);
	}

	private void parseArguments(HashMap args, Pdf417MobiSettings sett, Intent intent) {
		
		// Default values
		Boolean customUI = false;
		Boolean beep = true, noDialog = false, removeOverlay = false, uncertain = false, quietZone = false, highRes = false, frontFace = false;
		String license = null;
		Set<String> types = new HashSet<String>();

		if (args != null) {

			KrollDict options = new KrollDict(args);

			beep = options.optBoolean("beep", beep);
			noDialog = options.optBoolean("noDialog", noDialog);
			removeOverlay = options.optBoolean("removeOverlay", removeOverlay);
			uncertain = options.optBoolean("uncertain", uncertain);
			quietZone = options.optBoolean("quietZone", quietZone);
			highRes = options.optBoolean("highRes", highRes);
			frontFace = options.optBoolean("frontFace", frontFace);

			if (args.containsKey("types")) {
				Object[] typesList = (Object[]) args.get("types");				
				for (Object o : typesList) {
					types.add(o.toString());
				}
			}

			license = options.optString("licenseAndroid", null);
		}

		sett.setPdf417Enabled(types.contains(PDF417));
		sett.setQrCodeEnabled(types.contains(QR_CODE));
		sett.setCode128Enabled(types.contains(CODE_128));
		sett.setCode39Enabled(types.contains(CODE_39));
		sett.setEan13Enabled(types.contains(EAN_13));
		sett.setEan8Enabled(types.contains(EAN_8));
		sett.setItfEnabled(types.contains(ITF));
		sett.setUpcaEnabled(types.contains(UPC_A));
		sett.setUpceEnabled(types.contains(UPC_E));

		// set this to true to prevent showing dialog after successful scan
		sett.setDontShowDialog(noDialog);
		// if license permits this, remove Pdf417.mobi logo overlay on scan
		// activity
		// if license forbids this, this option has no effect
		sett.setRemoveOverlayEnabled(removeOverlay);

		// Set this to true to scan barcodes which don't have quiet zone (white area) around it
	    // Use only if necessary because it drastically slows down the recognition process 
		sett.setNullQuietZoneAllowed(quietZone);

		// Set this to true to scan even barcode not compliant with standards
	    // For example, malformed PDF417 barcodes which were incorrectly encoded
	    // Use only if necessary because it slows down the recognition process
		sett.setUncertainScanning(uncertain);

		// If you want sound to be played after the scanning process ends, 
		// put here the resource ID of your sound file. (optional)
		if (beep == true) {
			intent.putExtra(Pdf417ScanActivity.EXTRAS_BEEP_RESOURCE, RHelper.getRaw("beep_pdf417"));
		}

		// set EXTRAS_ALWAYS_USE_HIGH_RES to true if you want to always use highest 
		// possible camera resolution (enabled by default for all devices that support
		// at least 720p camera preview frame size)
		if (highRes == true) {
			intent.putExtra(Pdf417ScanActivity.EXTRAS_ALWAYS_USE_HIGH_RES, highRes);
		}

		// set EXTRAS_CAMERA_TYPE to use front facing camera
		// Note that front facing cameras do not have autofocus support, so it will not
		// be possible to scan denser and smaller codes.
		if (frontFace == true) {
			intent.putExtra(Pdf417ScanActivity.EXTRAS_CAMERA_TYPE, (Parcelable)CameraType.CAMERA_FRONTFACE);
		}

		// set the license key (for commercial versions only) - obtain your key at
		// http://pdf417.mobi
		if (license != null) {
			Log.d(LCAT, "Using Android license key: " + license);
			intent.putExtra(Pdf417ScanActivity.EXTRAS_LICENSE_KEY, license);
		}

		// put settings as intent extra
		intent.putExtra(BaseBarcodeActivity.EXTRAS_SETTINGS, sett);
	}

	@Override
	public void onError(Activity activity, int requestCode, Exception e) {
		Log.e(LCAT, "onError: " + e.getMessage());

		HashMap<String, Object> errdict = new HashMap<String, Object>();
		errdict.put("message", "Scan failed");
		errdict.put("code", requestCode);
		fireEvent("error", errdict);
	}

	@Override
	public void onResult(Activity activity, int requestCode, int resultCode, Intent data) {
		Log.d(LCAT, "onResult: " + resultCode);

		if (resultCode == BaseBarcodeActivity.RESULT_OK) {
			processResult(resultCode, data);

		} else if (resultCode == BaseBarcodeActivity.RESULT_CANCELED) {
			processCanceled(resultCode);

		} else {
			processFailed(resultCode, "Unknown result code");
		}
	}

	public void processFailed(int resultCode, String message) {
		Log.e(LCAT, "processFailed: " + resultCode);
		HashMap<String, Object> errdict = new HashMap<String, Object>();
		errdict.put("message", "Scan failed: " + message);
		errdict.put("code", resultCode);
		fireEvent("error", errdict);
	}

	public void processCanceled(int resultCode) {
		Log.w(LCAT, "processCanceled: " + resultCode);
		HashMap<String, Object> cancelDict = new HashMap<String, Object>();
		cancelDict.put("message", "Scan was canceled");
		cancelDict.put("code", resultCode);
		fireEvent("cancel", cancelDict);
	}

	public void processResult(int resultCode, Intent data) {
		try {	
			// Put results to map tree
			HashMap<String, Object> root = new HashMap<String, Object>();
			
			// read scan results
			ArrayList<Pdf417MobiScanData> scanDataList = data.getParcelableArrayListExtra(BaseBarcodeActivity.EXTRAS_RESULT_LIST);

			// List of all results in a separate element
			List<Object> resultsList = new ArrayList<Object>();
			for (Pdf417MobiScanData scanData : scanDataList) {
				HashMap<String, Object> elem = new HashMap<String, Object>();
				setScanData(scanData, elem);
				resultsList.add(elem);
			}
			root.put(RESULT_LIST, resultsList.toArray());
			
			root.put(CANCELLED, false);
			fireEvent("success", root);
			
		} catch (Exception e) {
			processFailed(resultCode, e.getMessage());
		}			
	}

	private void setScanData(Pdf417MobiScanData scanData, HashMap<String, Object> obj) throws Exception {
		// read scanned barcode type (PDF417 or QR code)
		String barcodeType = scanData.getBarcodeType();

		// read the data contained in barcode
		String barcodeData = scanData.getBarcodeData();

		// read raw barcode data
		BarcodeDetailedData rawData = scanData.getBarcodeRawData();
		
		obj.put(TYPE, barcodeType);
		obj.put(DATA, barcodeData);
		obj.put(RAW_DATA, byteArrayToHex(rawData.getAllData()));
	}

	private String byteArrayToHex(byte[] data) {
		StringBuilder sb = new StringBuilder();
		for (byte b : data) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
}

