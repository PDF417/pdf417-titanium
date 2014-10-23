/**
 * Example: How to configure and start scaning activity
 * This demo uses demo license key valid only for packageId mobi.pdf417.demo, although 
 * you can use pdf417 SDK free of change and without license key for development and non-commercial projects. 
 * Once you obtain a commercial license key from www.pdf417.mobi add key "licenseAndroid" to options.
 */
function FirstView() {
	//create object instance, a parasitic subclass of Observable
	var self = Ti.UI.createView();

	//label using localization-ready strings from <app dir>/i18n/en/strings.xml
	var label = Ti.UI.createLabel({
		color:'#000000',
		text:String.format(L('welcome'),'Titanium'),
		height:'auto',
		width:'auto'
	});
	self.add(label);
	
	// implement your decoding as you need it, this just does ASCII decoding
	function hex2a(hex) {
	    var str = '';
	    for (var i = 0; i < hex.length; i += 2) {
	        str += String.fromCharCode(parseInt(hex.substr(i, 2), 16));
	    }
	    return str;
	}
	
	// Initialize Pdf417 Plugin
	// Import module  
	var pdf417 = require('mobi.pdf417.plugin');
	  
	// This license is only valid for package name "mobi.pdf417.demo"
	var licenseAndroid = "BTH7-L4JO-UI5T-JAFP-YSKX-BXZT-SDKE-LKIZ";   

	// Prepare settings object to tweak additional scanning parameters.
    // These properties are optional. If you don't send set them all default
    // scanning parameters will be used.
    // Types defined which code types will be read. This property is required. 
    // Available types are: pdf417.PDF417, pdf417.QR_CODE, pdf417.CODE_128, 
    // pdf417.CODE_39, pdf417.EAN_13, pdf417.EAN_8, pdf417.ITF, pdf417.UPC_A, pdf417.UPC_E
	var settings = {
	    beep: true,
	    noDialog : false,
	    uncertain: false,
	    quietZone: false,
	    highRes: false,
	    frontFace : false,
	    types : [ pdf417.QR_CODE, pdf417.PDF417 ],
	    licenseAndroid : licenseAndroid    
	};
	  
	// Event handlers
	pdf417.addEventListener('error', function (e) {
	    label.text = "Error: " + e.message;
	});
	
	pdf417.addEventListener('cancel', function (e) {
	    Ti.API.info('Cancel received');
	    label.text = "Cancelled!";
	});
	
	pdf417.addEventListener('success', function (e) {
		Ti.API.info('Scan result: ' + e);		    
	    var result = "Success: ";
	    
	    var list = e.resultList;		    
	    if (list && list.length > 0) {
	    	Ti.API.info('Scan results count: ' + list.length);
	    	for (index = 0; index < list.length; ++index) {
	    		var data = list[index];
	    		result += "Result " + index + ": ";
	    		result +=  data.data + " (raw: " + hex2a(data.raw) + ") (Type: " + data.type + ") ";
	    	}
	    } else {
	    	Ti.API.info('Scan did not return any results');
	    	result += "No results";
	    }
	    
	    label.text = result;
	});

	// Add behavior for UI
	label.addEventListener('click', function(e) {				  
		// Start scan
		pdf417.scan(settings);
	});

	return self;
}

module.exports = FirstView;