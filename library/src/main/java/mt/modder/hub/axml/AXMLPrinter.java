/*
* AxmlPrinter - An Advanced Axml Printer available with proper xml style/format feature
* Copyright 2024, developer-krushna
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
*     * Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimer.
*     * Redistributions in binary form must reproduce the above
* copyright notice, this list of conditions and the following disclaimer
* in the documentation and/or other materials provided with the
* distribution.
*     * Neither the name of developer-krushna nor the names of its
* contributors may be used to endorse or promote products derived from
* this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
* LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
* A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
* OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
* DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
* THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
* OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


*     Please contact Krushna by email mt.modder.hub@gmail.com if you need
*     additional information or have any questions
*/

package mt.modder.hub.axml;

import org.xmlpull.v1.XmlPullParser;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import mt.modder.hub.axmlTools.AXmlResourceParser;
import java.util.Map;
import java.util.HashMap;
/*
Author @developer-krushna
*/
public final class AXMLPrinter {
	// Constants for conversion factors and unit strings
	private  final float[] RADIX_MULTS = {0.00390625f, 3.051758E-5f, 1.192093E-7f, 4.656613E-10f};
	private  final String[] DIMENSION_UNITS = {"px", "dip", "sp", "pt", "in", "mm", "", ""};
	private  final String[] FRACTION_UNITS = {"%", "%p", "", "", "", "", "", ""};
	
	private boolean isAttrConversion = false;
	
	private static final Map<String, String[]> ENUM_ATTRIBUTE_MAP = new HashMap<>();
	
	static {
		ENUM_ATTRIBUTE_MAP.put("screenOrientation", new String[]{
		    "landscape", "portrait", "user", "behind", "sensor", "nosensor", 
			"sensorLandscape", "sensorPortrait", "reverseLandscape", "reversePortrait", 
			"fullSensor", "userLandscape", "userPortrait", "fullUser", "locked"
		});
		ENUM_ATTRIBUTE_MAP.put("scaleType", new String[]{
			"matrix", "fitXY", "fitStart", "fitCenter", "fitEnd", "center", "centerCrop", 
			"centerInside"
		});
		ENUM_ATTRIBUTE_MAP.put("reqNavigation", new String[]{
			"undefined", "nonav", "dpad", "trackball", "wheel"
		});
		ENUM_ATTRIBUTE_MAP.put("reqTouchScreen", new String[]{
			"undefined", "notouch", "stylus", "finger"
		});
		ENUM_ATTRIBUTE_MAP.put("visibility", new String[]{
			"visible", "invisible", "gone"
		});
		ENUM_ATTRIBUTE_MAP.put("orientation", new String[]{
			"horizontal", "vertical"
		});				   
		
		ENUM_ATTRIBUTE_MAP.put("importantForAccessibility", new String[]{
			"auto", "yes", "no", "noHideDescendants"
		});
		ENUM_ATTRIBUTE_MAP.put("layoutDirection", new String[]{
			"ltr", "rtl", "inherit", "locale"
		});
		ENUM_ATTRIBUTE_MAP.put("layerType", new String[]{
			"none", "software", "hardware"
		});
		ENUM_ATTRIBUTE_MAP.put("launchMode", new String[]{
								   "standard", "singleTop", "singleTask", "singleInstance"
							   });
		ENUM_ATTRIBUTE_MAP.put("ellipsize", new String[]{
			"none", "start", "middle", "end", "marquee"
		});		
		ENUM_ATTRIBUTE_MAP.put("transcriptMode", new String[]{
			"disabled", "normal", "alwaysScroll"
		});
		ENUM_ATTRIBUTE_MAP.put("stretchMode", new String[]{
			"none", "spacingWidth", "columnWidth", "spacingWidthUniform"
		});
		
		ENUM_ATTRIBUTE_MAP.put("choiceMode", new String[]{
			"none", "singleChoice", "multipleChoice", "multipleChoiceModal"
		});
		
		ENUM_ATTRIBUTE_MAP.put("shape", new String[]{
			"rectangle", "oval", "line", "ring"
		});
		ENUM_ATTRIBUTE_MAP.put("streamType", new String[]{
			"voice", "system", "ring", "music", "alarm"
		});
		ENUM_ATTRIBUTE_MAP.put("layoutDirection", new String[]{
			"ltr", "rtl", "inherit", "locale"
		});
		ENUM_ATTRIBUTE_MAP.put("type", new String[]{
			"linear", "radial", "sweep"
		});
		ENUM_ATTRIBUTE_MAP.put("repeatMode", new String[]{
			"none", "restart", "reverse"
		});
		ENUM_ATTRIBUTE_MAP.put("checkableBehavior", new String[]{
			"none" ,"all", "single"
		});
		ENUM_ATTRIBUTE_MAP.put("animationOrder", new String[]{
			"normal", "reverse", "random"
		});
		ENUM_ATTRIBUTE_MAP.put("directionPriority", new String[]{
			"none", "column", "row"
		});
		ENUM_ATTRIBUTE_MAP.put("tileMode", new String[]{
			"clamp", "repeat", "mirror"
		});
		
		ENUM_ATTRIBUTE_MAP.put("bufferType", new String[]{
			"normal", "spannable", "editable"
		});
		
		ENUM_ATTRIBUTE_MAP.put("indeterminateBehavior", new String[]{
			"none","repeat", "cycle"
		});
		ENUM_ATTRIBUTE_MAP.put("mode", new String[]{
			"none","oneLine", "collapsing", "twoLine"
		});
		ENUM_ATTRIBUTE_MAP.put("capitalize", new String[]{
			"none", "sentences", "words", "characters"
		});
		ENUM_ATTRIBUTE_MAP.put("paddingMode", new String[]{
			"nest", "stack"
		});
		ENUM_ATTRIBUTE_MAP.put("datePickerMode", new String[]{
			"none","spinner", "calendar"
		});
		ENUM_ATTRIBUTE_MAP.put("colorMode", new String[]{
			"default","wideColorGamut", "hdr"
		});
		ENUM_ATTRIBUTE_MAP.put("fontStyle", new String[]{
			"normal","italic"
		});
		ENUM_ATTRIBUTE_MAP.put("autoSizeTextType", new String[]{
			"none","uniform"
		});
		ENUM_ATTRIBUTE_MAP.put("alignmentMode", new String[]{
			"alignBounds","alignMargins"
		});
		ENUM_ATTRIBUTE_MAP.put("thumbPosition", new String[]{
			"midpoint","inside"
		});
		ENUM_ATTRIBUTE_MAP.put("timePickerMode", new String[]{
			"none","spinner","clock"
		});
		
		ENUM_ATTRIBUTE_MAP.put("strokeLineCap", new String[]{
			"butt","round","square"
		});
		ENUM_ATTRIBUTE_MAP.put("strokeLineJoin", new String[]{
			"miter","round","bevel"
		});
		ENUM_ATTRIBUTE_MAP.put("transitionOrdering", new String[]{
			"together","sequential"
		});
		ENUM_ATTRIBUTE_MAP.put("fadingMode", new String[]{
			"none","fade_in","fade_out", "fade_in_out"
		});
		ENUM_ATTRIBUTE_MAP.put("verticalScrollbarPosition", new String[]{
			"defaultPosition","left","right"
		});
		ENUM_ATTRIBUTE_MAP.put("persistableMode", new String[]{
			"persistRootOnly","persistNever","persistAcrossReboots"
		});
		ENUM_ATTRIBUTE_MAP.put("spinnerMode", new String[]{
			"dialog","dropdown"
		});
		ENUM_ATTRIBUTE_MAP.put("actionBarSize", new String[]{
			"wrap_content"
		});
		ENUM_ATTRIBUTE_MAP.put("fastScrollOverlayPosition", new String[]{
			"floating","atThumb","aboveThumb"
		});
		ENUM_ATTRIBUTE_MAP.put("layerType", new String[]{
			"none","software","hardware"
		});
		ENUM_ATTRIBUTE_MAP.put("ordering", new String[]{
			"together","sequentially"
		});
		ENUM_ATTRIBUTE_MAP.put("valueType", new String[]{
			"floatType","intType","pathType", "colorType"
		});
		ENUM_ATTRIBUTE_MAP.put("navigationMode", new String[]{
			"normal","listMode","tabMode"
		});
		ENUM_ATTRIBUTE_MAP.put("overScrollMode", new String[]{
			"always","ifContentScrolls","never"
		});
		ENUM_ATTRIBUTE_MAP.put("installLocation", new String[]{
			"auto","internalOnly","preferExternal"
		});
		ENUM_ATTRIBUTE_MAP.put("gestureStrokeType", new String[]{
			"single","multiple"
		});
		ENUM_ATTRIBUTE_MAP.put("reqKeyboardType", new String[]{
			"undefined","nokeys", "qwerty","twelvekey"
		});
		ENUM_ATTRIBUTE_MAP.put("autoRevokePermissions", new String[]{
			"allowed","discouraged", "disallowed"
		});
		ENUM_ATTRIBUTE_MAP.put("hyphenationFrequency", new String[]{
			"none","normal", "full"
		});
		ENUM_ATTRIBUTE_MAP.put("tileModeX", new String[]{
			"clamp","repeat", "mirror"
		});
		ENUM_ATTRIBUTE_MAP.put("breakStrategy", new String[]{
			"simple","high_quality", "balanced"
		});
		ENUM_ATTRIBUTE_MAP.put("windowLayoutInDisplayCutoutMode", new String[]{
			"default","shortEdges", "never","always"
		});
		ENUM_ATTRIBUTE_MAP.put("outlineProvider", new String[]{
			"background","none", "bounds","paddedBounds"
		});
		ENUM_ATTRIBUTE_MAP.put("lockTaskMode", new String[]{
			"normal","never", "always","if_whitelisted"
		});
		ENUM_ATTRIBUTE_MAP.put("documentLaunchMode", new String[]{
			"none","intoExisting", "always","never"
		});
		ENUM_ATTRIBUTE_MAP.put("appCategory", new String[]{
			"hidden","audio", "video",
			"image","social", "news",
			"maps","productivity"
		});
		ENUM_ATTRIBUTE_MAP.put("restrictionType", new String[]{
			"game","bool", "choice",
			"","multi-select", "integer",
			"string","bundle","bundle_array"
		});
		ENUM_ATTRIBUTE_MAP.put("justificationMode", new String[]{
			"none","inter_word"
		});
		ENUM_ATTRIBUTE_MAP.put("layout_width", new String[]{
			"match_parent","wrap_content"
		});
		ENUM_ATTRIBUTE_MAP.put("layout_height", new String[]{
			"match_parent","wrap_content"
		});
		// You can add more
		// Dont try to put multi attr value (Separted with "|" ) based enum map
	}
	
	
	// Converts a complex number to a float
	public  float complexToFloat(int complex) {
		return (complex & (-256)) * RADIX_MULTS[(complex >> 4) & 3];
	}
	
	// Set check attribute and convert int to its string value
	public void setAttributeIntConversion(boolean isAttrConvert){
		isAttrConversion = isAttrConvert;
	}
	
	// Main method to decompile an XML byte array
	public  String convertXml(byte[] byteArray) {
		try {
			// Initialize the XML parser with the byte array input
			AXmlResourceParser xmlParser = new AXmlResourceParser();
			xmlParser.open(new ByteArrayInputStream(byteArray));
			StringBuilder indentation = new StringBuilder();
			StringBuilder xmlContent = new StringBuilder();
			while (true) {
				int eventType = xmlParser.next();
				if (eventType == XmlPullParser.END_DOCUMENT) {
					// End of document
					String result = xmlContent.toString();
					xmlParser.close();
					return result;
				}
				
				switch (eventType) {
					case XmlPullParser.START_DOCUMENT:
					// Append XML declaration at the start of the document
					xmlContent.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
					break;
					
					case XmlPullParser.START_TAG:
					// Handle the start of a new XML tag
					if (xmlParser.getPrevious().type == XmlPullParser.START_TAG) {
						xmlContent.append(">\n");
					}
					xmlContent.append(String.format("%s<%s%s", indentation, getNamespacePrefix(xmlParser.getPrefix()), xmlParser.getName()));
					indentation.append("    ");
					
					// Handle namespaces
					int depth = xmlParser.getDepth();
					int namespaceStart = xmlParser.getNamespaceCount(depth - 1);
					int namespaceEnd = xmlParser.getNamespaceCount(depth);
					
					for (int i = namespaceStart; i < namespaceEnd; i++) {
						String namespaceFormat = (i == namespaceStart) ? "%sxmlns:%s=\"%s\"" : "\n%sxmlns:%s=\"%s\"";
						xmlContent.append(String.format(namespaceFormat, (i == namespaceStart) ? " " : indentation, xmlParser.getNamespacePrefix(i), xmlParser.getNamespaceUri(i)));
					}
					
					// Handle attributes
					int attributeCount = xmlParser.getAttributeCount();
					if (attributeCount > 0) {
						xmlContent.append('\n');
					}
					for (int i = 0; i < attributeCount; i++) {
						String attributeFormat = (i == attributeCount - 1) ? "%s%s%s=\"%s\"" : "%s%s%s=\"%s\"\n";
						xmlContent.append(String.format(attributeFormat, indentation, getNamespacePrefix(xmlParser.getAttributePrefix(i)), xmlParser.getAttributeName(i), getAttributeValue(xmlParser, i)));
					}
					break;
					
					case XmlPullParser.END_TAG:
					// Handle the end of an XML tag
					indentation.setLength(indentation.length() - "    ".length());
					if (!isEndOf(xmlParser, xmlParser.getPrevious())) {
						xmlContent.append(String.format("%s</%s%s>\n", indentation, getNamespacePrefix(xmlParser.getPrefix()), xmlParser.getName()));
					} else {
						xmlContent.append("/>\n");
					}
					break;
					
					case XmlPullParser.TEXT:
					// Handle text within an XML tag
					if (xmlParser.getPrevious().type == XmlPullParser.START_TAG) {
						xmlContent.append(">\n");
					}
					xmlContent.append(String.format("%s%s\n", indentation, xmlParser.getText()));
					break;
				}
			}
		} catch (Exception e) {
			// Handle exceptions and return the stack trace
			return "----StackTrace----\n" + e + "\n" + Arrays.toString(e.getStackTrace());
		}
	}
	
	// Retrieves the attribute value in a human-readable format based on its type
	private  String getAttributeValue(AXmlResourceParser xmlParser, int index) {
		int attributeValueType = xmlParser.getAttributeValueType(index);
		int attributeValueData = xmlParser.getAttributeValueData(index);
		switch (attributeValueType) {
			case 3:
			return xmlParser.getAttributeValue(index);
			case 2:
			return String.format("?%08X", attributeValueData);
			case 1:
			return String.format("@%08X", attributeValueData);
			case 4:
			return String.valueOf(Float.intBitsToFloat(attributeValueData));
			case 17:
			return String.valueOf(attributeValueData);
			case 18:
			return attributeValueData != 0 ? "true" : "false";
			case 5:
			return complexToFloat(attributeValueData) + DIMENSION_UNITS[attributeValueData & 15];
			case 6:
			return complexToFloat(attributeValueData) + FRACTION_UNITS[attributeValueData & 15];
			default:
			
			if(isAttrConversion){
				// Check for specific attributes and convert integer values to their string equivalents
				String attributeName = xmlParser.getAttributeName(index);
				String[] enumValues = ENUM_ATTRIBUTE_MAP.get(attributeName);
				if (enumValues != null) {
					// Handle layout_width and layout_height attributes separately
					if ((attributeName.equals("layout_width") || attributeName.equals("layout_height")) && (attributeValueType == 16 || attributeValueType == 17)) {
						if (attributeValueData == -1) {
							return "match_parent";
						} else if (attributeValueData == -2) {
							return "wrap_content";
						} else {
							return String.valueOf(attributeValueData);  // For dimensions
						}
					}
					if (attributeValueType == 16 && attributeValueData >= 0 && attributeValueData < enumValues.length) {
						return enumValues[attributeValueData];
					}
				}
			}
			
			return (attributeValueType >= 28 && attributeValueType <= 31) ?
			String.format("#%08X", attributeValueData) :
			(attributeValueType >= 16 && attributeValueType <= 31) ?
			String.valueOf(attributeValueData) :
			String.format("<0x%X, type 0x%02X>", attributeValueData, attributeValueType);
		}
	}
	
	
	// Retrieves the namespace prefix if it exists
	private  String getNamespacePrefix(String prefix) {
		return (prefix == null || prefix.length() == 0) ? "" : prefix + ":";
	}
	
	// Checks if the current XML tag is the end of the previous tag
	private  boolean isEndOf(AXmlResourceParser xmlParser, AXmlResourceParser.OldXMLToken oldXmlToken) {
		return oldXmlToken.type == XmlPullParser.START_TAG &&
		xmlParser.getEventType() == XmlPullParser.END_TAG &&
		xmlParser.getName().equals(oldXmlToken.name) &&
		((oldXmlToken.namespace == null && xmlParser.getPrefix() == null) ||
		(oldXmlToken.namespace != null && xmlParser.getPrefix() != null && xmlParser.getPrefix().equals(oldXmlToken.namespace)));
	}
}
