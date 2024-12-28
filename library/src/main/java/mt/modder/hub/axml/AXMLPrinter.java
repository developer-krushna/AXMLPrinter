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

import java.io.*;
import java.util.*;
import mt.modder.hub.axmlTools.*;
import mt.modder.hub.axmlTools.utils.*;
import org.xmlpull.v1.*;
import java.util.regex.*;
import mt.modder.hub.axmlTools.arsc.*;


/*
 Author @developer-krushna
 Also thanks to ChatGPT for giving ideas about code enhancement
 */
public final class AXMLPrinter {
	
	private static final String COPYRIGHT = "AXMLPrinter\nCopyright (C) developer-krushna [https://github.com/developer-krushna/](krushnachandramaharna57@gmail.com)\nThis project is distributed under the Apache License v2.0 license";
	
	// Constants for conversion factors and unit strings
	private static final float MANTISSA_MULT =
	1.0f / (1 << TypedValue.COMPLEX_MANTISSA_SHIFT);
	
    private static final float[] RADIX_MULTS = new float[]{
		1.0f * MANTISSA_MULT /* 0.00390625f */, 
		1.0f / (1 << 7) * MANTISSA_MULT /* 3.051758E-5f */,
		1.0f / (1 << 15) * MANTISSA_MULT /* 1.192093E-7f */, 
		1.0f / (1 << 23) * MANTISSA_MULT /* 4.656613E-10f */
    };
    private static final String[] DIMENSION_UNIT_STRS = new String[]{
		"px", "dp", "sp", "pt", "in", "mm"
    };
    private static final String[] FRACTION_UNIT_STRS = new String[]{
		"%", "%p"
    };
	
	private boolean isId2Name = false;
	private boolean isAttrConversion = false;
	
	private ResourceIdExtractor systemResFile = new ResourceIdExtractor(); 
	private ResourceIdExtractor customResFile = new ResourceIdExtractor();
	public String customAttributeTag = "Custom";
	public String systemAttributeTag = "System";
	public boolean isCustomResFileExist = false;
	
	private NamespaceChecker namespaceChecker = new NamespaceChecker();
	public static final String android = "android";
	
	private Map<String, String> permissionInfoMap;
	private boolean isPermissionInfoLoaded = false;
	private String usesPermission = "uses-permission";
	private boolean isExtractPermissionDescription = false;
	
	
	// Set check attribute and convert hex value to its corresponding entry value @id/.., ?attr/.., etc
	public void setEnableID2Name(boolean isId2name){
		isId2Name = isId2name;
		if(isId2name){
			try {
				// Load System resource file
				loadSystenRes();
			} catch (Exception e){
				systemResFile = null;
			}
		}
	}
	
	
	public void setAttrValueTranslation(boolean isAttrConvert){
		isAttrConversion = isAttrConvert;
		if(isAttrConvert){
			
		}
	}
	
	public void setExtractPermissionDescription(boolean isExtract){
		isExtractPermissionDescription = isExtract;
	}
	
	//Load system res from resource folder
	private void loadSystenRes() throws Exception{
		try (InputStream arscStream = AXMLPrinter.class.getResourceAsStream("/assets/resources.arsc")) {
			systemResFile.loadArscData(arscStream);
		}
	}
	
	public String readFromFile(String path) throws Exception{
		FileInputStream fis = new FileInputStream(path);
		byte[] byteArray = new byte[fis.available()];
		fis.read(byteArray);
		fis.close();
		
		if(isId2Name){
			File file = new File(path);
			String resourceFile = file.getParent() + "/resources.arsc";
			System.out.println(resourceFile);
			if(new File(resourceFile).exists()){
				try{
					try (InputStream arscStream = new FileInputStream(resourceFile)) {
						customResFile.loadArscData(arscStream);
					}
					isCustomResFileExist = true;
				}catch(Exception e){
					isCustomResFileExist = false;
				}
			}
		}
		
		//decompile xml2Axml
		return convertXml(byteArray);
	}

	// Main method to decompile an XML byte array
	public String convertXml(byte[] byteArray) {
		System.out.println(COPYRIGHT);
		if(!isId2Name){
			try {
				loadSystenRes();
			}catch (Exception e){
				systemResFile = null;
			}
		}
		try {
			// Initialize the XML parser with the byte array input
			AXmlResourceParser xmlParser = new AXmlResourceParser();
			xmlParser.open(new ByteArrayInputStream(byteArray));
			StringBuilder indentation = new StringBuilder();
			StringBuilder xmlContent = new StringBuilder();
			boolean isExistAndroidNamespace = false;
			
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
						xmlContent.append("<!-- This is a modified version of AXMLPrinter2(By Google) library. Check out how many changes are made at https://github.com/developer-krushna/AXMLPrinter by (@developer-krushna) -->\n");
						break;

					case XmlPullParser.START_TAG:
						// Handle the start of a new XML tag
						if (xmlParser.getPrevious().type == XmlPullParser.START_TAG) {
							xmlContent.append(">\n");
						}
						
						String prefix = xmlParser.getName();
						
						int attributeCount = xmlParser.getAttributeCount(); // count attributes
						
						//check if the user-permission prefix found (We know its available only in AndroidManifest.xml)
						if(isExtractPermissionDescription){
							if (prefix.contains(usesPermission)) {
								if(!isPermissionInfoLoaded){
									//load permissionInfo one time only
									permissionInfoMap = loadPermissionsInfo();
									isPermissionInfoLoaded = true;
								}
								//extract permission description from corresponding permissionName
								if(attributeCount > 0){
									for (int i = 0; i < attributeCount; i++) {
										String permissionName = xmlParser.getAttributeValue(i);
										String description = permissionInfoMap.get(permissionName);
										if (description != null) {
											// Print permission description
											xmlContent.append(indentation).append("<!-- ").append(description).append(" -->\n");
										}
									}
								}
							}
						}
						
						xmlContent.append(String.format("%s<%s%s", 
						                                indentation, 
														getMainNodeNamespacePrefix(xmlParser.getPrefix()), 
														prefix));
						indentation.append("    ");

						// Handle namespaces
						int depth = xmlParser.getDepth();
						int namespaceStart = xmlParser.getNamespaceCount(depth - 1);
						int namespaceEnd = xmlParser.getNamespaceCount(depth);

						for (int i = namespaceStart; i < namespaceEnd; i++) {
							String namespaceFormat = (i == namespaceStart) ? "%sxmlns:%s=\"%s\"" : "\n%sxmlns:%s=\"%s\"";
							xmlContent.append(String.format(namespaceFormat, 
							                               (i == namespaceStart) ? " " : indentation, 
														   xmlParser.getNamespacePrefix(i), 
														   xmlParser.getNamespaceUri(i)));
							isExistAndroidNamespace = true; // make it true as it completed the above task							   
						}
						
						// If the android header namespace is not exist then add it manually
						if(!isExistAndroidNamespace && prefix.equals("manifest")){
							String namespaceFormat = "%sxmlns:%s=\"%s\"";
							xmlContent.append(String.format(namespaceFormat, " ", android, xmlParser.NS_ANDROID));
							//xmlContent.append("xmlns:android="+ xmlParser.NS_ANDROID + "\"");
							isExistAndroidNamespace = true;
						}

						// Handle attributes
						
						if (attributeCount > 0) {
							if(attributeCount == 1 && prefix.equals(usesPermission)) {
								xmlContent.append("");
								for (int i = 0; i < attributeCount; i++) {
									// Skip attributes with a dot (.)
									if (xmlParser.getAttributeName(i).contains(".")) {
										continue; // Skip this attribute if its name contains a dot
									}

									String attributeFormat = (i == attributeCount - 1) ? "%s%s%s=\"%s\"" : "%s%s%s=\"%s\"\n";
									String attributeName = getAttributeName(xmlParser, i);
									// Final Addition of namespace , attribute along with its corresponding value
									// Indention is not needed because it has 1 attribute only its main node
									xmlContent.append(String.format(attributeFormat, 
									                                " ", 
																	getAttrNamespacePrefix(xmlParser, i, attributeName), 
																	attributeName.replaceAll(customAttributeTag, "").replaceAll(systemAttributeTag, ""), 
																	getAttributeValue(xmlParser, i)));
								}
							} else {
							    xmlContent.append('\n');
								for (int i = 0; i < attributeCount; i++) {
									// Skip attributes with a dot (.)
									if (xmlParser.getAttributeName(i).contains(".")) {
										continue; // Skip this attribute if its name contains a dot
									}

									String attributeFormat = (i == attributeCount - 1) ? "%s%s%s=\"%s\"" : "%s%s%s=\"%s\"\n";
									String attributeName = getAttributeName(xmlParser, i); //Attribute name
									// Final Addition of namespace , attribute along with its corresponding value
									// Indention is needed because it 2 or more attributes
									
									xmlContent.append(String.format(attributeFormat, 
									                                indentation, 
																	getAttrNamespacePrefix(xmlParser, i, attributeName), 
																	attributeName.replaceAll(customAttributeTag, "").replaceAll(systemAttributeTag, ""), 
																	getAttributeValue(xmlParser, i)));

								}
							}
						}
						
						break;

					case XmlPullParser.END_TAG:
						// Handle the end of an XML tag
						indentation.setLength(indentation.length() - "    ".length());
						if (!isEndOfPrecededXmlTag(xmlParser, xmlParser.getPrevious())) {
							
							xmlContent.append(String.format("%s</%s%s>\n", 
							                                 indentation, 
															 getMainNodeNamespacePrefix(xmlParser.getPrefix()), 
															 xmlParser.getName()));
						} else {
							xmlContent.append(" />\n");
						}
						break;

					case XmlPullParser.TEXT:
						// Handle text within an XML tag
						if (xmlParser.getPrevious().type == XmlPullParser.START_TAG) {
							xmlContent.append(">\n");
						}
						xmlContent.append(String.format("%s%s\n", 
						                                indentation, 
														xmlParser.getText()));
						break;
				}
			}
		} catch (Exception e) {
			// Handle exceptions and return the stack trace
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String exceptionDetails = sw.toString();
			return "----StackTrace----\n" + exceptionDetails;
		}
	}

	// Retrieves the attribute value in a human-readable format based on its type
	private String getAttributeValue(AXmlResourceParser xmlParser, int index) {
		
		String attributeName = getAttributeName(xmlParser, index).replaceAll(customAttributeTag, "").replaceAll(systemAttributeTag, "");
		// String attributeName = getAttributeName(xmlParser, index);
		
		int attributeValueType = xmlParser.getAttributeValueType(index);
		
		int attributeValueData = xmlParser.getAttributeValueData(index);
		
		switch (attributeValueType) {
			case TypedValue.TYPE_STRING /* 3 */:
				// String value
				
				String stringValue = xmlParser.getAttributeValue(index);
				// Preserve newlines as \n for XML
				return stringValue.replace("\n", "\\n");
				
			case TypedValue.TYPE_ATTRIBUTE /* 2 */:
				// Resource ID
				if(isId2Name){
				    return "?" + extractResourecID(attributeValueData);
				}else{
				    return "?" + String.format("%08x", attributeValueData);
				}
				
			case TypedValue.TYPE_REFERENCE /* 1 */:
				// Reference
				if(isId2Name){
					return "@" + extractResourecID(attributeValueData);
				}else{
					return "@" + String.format("%08x", attributeValueData);
				}
				
			case TypedValue.TYPE_FLOAT /* 4 */:
				// Float value
				return Float.toString(Float.intBitsToFloat(attributeValueData));
				
			case TypedValue.TYPE_INT_HEX /* 17 */:
				// Hex integer value or flag values
				if (isAttrConversion) {
					String decodedValue = AttributesExtractor.getInstance().decode(attributeName, attributeValueData);
					if (decodedValue != null && !decodedValue.isEmpty() ) {
						return decodedValue; // Return the decoded value if found
					} else {
						return "0x" + Integer.toHexString(attributeValueData);
					}
				} else{
					return "0x" + Integer.toHexString(attributeValueData);
				}
					
			case TypedValue.TYPE_INT_BOOLEAN /* 18 */:
				// Boolean value
				return attributeValueData != 0 ? "true" : "false";
				
			case TypedValue.TYPE_DIMENSION /* 5 */:
				// Dimension value
				
				return TypedValue.complexToFloat(attributeValueData) + DIMENSION_UNIT_STRS[attributeValueData & 15];
				
			case TypedValue.TYPE_FRACTION /* 6 */:
				// Fraction value
				return (TypedValue.complexToFloat(attributeValueData) * 100.0f) + FRACTION_UNIT_STRS[attributeValueData & 15];
				
			default:
				// Handle enum or flag values and other cases 
				if (isAttrConversion) {
					String decodedValue = AttributesExtractor.getInstance().decode(attributeName, attributeValueData);
					if (decodedValue != null) {
						return decodedValue; // Return the decoded value if found
					}
				}
				// For unhandled types or cases
				return (attributeValueType >= 28 && attributeValueType <= 31) ?
					String.format("#%08x", attributeValueData) :
					(attributeValueType >= 16 && attributeValueType <= 31) ?
					String.valueOf(attributeValueData) :
					String.format("<0x%X, type 0x%02X>", attributeValueData, attributeValueType);
		}
	}

	// Checks if the current XML tag is the end of the previous tag
	private  boolean isEndOfPrecededXmlTag(AXmlResourceParser xmlParser, AXmlResourceParser.PrecededXmlToken precededXmlToken) {
		return precededXmlToken.type == XmlPullParser.START_TAG &&
			xmlParser.getEventType() == XmlPullParser.END_TAG &&
			xmlParser.getName().equals(precededXmlToken.name) &&
			((precededXmlToken.namespace == null && xmlParser.getPrefix() == null) ||
			(precededXmlToken.namespace != null && xmlParser.getPrefix() != null && xmlParser.getPrefix().equals(precededXmlToken.namespace)));
	}

	// Retrieves the main node namespace prefix if it exists
	private  String getMainNodeNamespacePrefix(String prefix) {
		return (prefix == null || prefix.length() == 0) ? "" : prefix + ":";
	}
	
	// Retrieves the attribute namespace prefix if it exists
	private String getAttrNamespacePrefix(AXmlResourceParser xmlParser, int position, String attributeName) {
		String namespace = xmlParser.getAttributePrefix(position);
		if(attributeName.contains(customAttributeTag)){
			return "";
			// check if any unknown attributes are found and it will start from "id"
		} else if(isUnknownAttrMatched(attributeName)){
		    return "";
		} else if(attributeName.contains(systemAttributeTag)) {
			return android + ":";
		} else if(namespace.isEmpty()) {
			if(namespaceChecker.isAttributeExist(attributeName)){
				return "";
			}
			return android + ":";
		}
		return namespace + ":";
	}
	
	// Get attribute name dyanemically
	public String getAttributeName(AXmlResourceParser xmlParser, int position) {
		String attributeName = xmlParser.getAttributeName(position);

		//check if the attributes are encrypted with attribute hex id 
		if (xmlParser.isChunkResourceIDs || isUnknownAttrMatched(attributeName)) {
			try {
				String extractedName = getAttributeNameFromResources(attributeName.replace("id", ""));
				return extractedName != null ? extractedName.replaceAll("attr/", "") : getFallbackAttributeName(attributeName);
			} catch (Exception e) {
				return getFallbackAttributeName(attributeName);
			}
		} else {
			return attributeName;
		}
	}

	// Get attribute name from either system resource file or custom resource file
	private String getAttributeNameFromResources(String attribute_hexId) throws Exception {
		String systemAttribute = systemResFile.getNameForHexId("0" + attribute_hexId);
		String extractedAttributeName = null;
		if(systemAttribute != null){
			extractedAttributeName = systemAttributeTag + systemAttribute;
		}
        // Process custom resource file if exist and also check if the system resource file don't have target hex id
		if (isCustomResFileExist && extractedAttributeName == null) {
			extractedAttributeName = customResFile.getNameForHexId(attribute_hexId);
			if (extractedAttributeName != null) {  //Only add if a name was found
				extractedAttributeName = customAttributeTag + extractedAttributeName;
			}
		}

		return extractedAttributeName;
	}
	
    //check the without namespace based specific attributes if matched
	private String getFallbackAttributeName(String attributeName) {
		if (namespaceChecker.isAttributeExist(attributeName)) {
			return attributeName;
		} else if (attributeName != null && attributeName.startsWith("id")) {
			return attributeName;
		} else {
			return "id" + attributeName;
		}
	}
	
	// match unknown attributes starting from "id"
	public boolean isUnknownAttrMatched(String attributeName){
		String regex = "^id[a-zA-Z0-9]+";
        Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(attributeName);
		if(matcher.matches()){
			return true;
		} else {
			return false;
		}
	}
	
	// Extract resource id2Name according to hex id
	// It is only enable if "isId2Name" is true
	public String extractResourecID(int i) {
		String resHexId = String.format("%08x", new Object[]{Integer.valueOf(i)});
		String systemId2Name = null;
		String customResId2Name = null;
		try{
			if(isId2Name){
				// Load system resource file
				systemId2Name = systemResFile.getNameForHexId(resHexId);
				
				// If System don't have the id then lets move to custom resource file 
				if (isCustomResFileExist && systemId2Name == null) {
					customResId2Name = customResFile.getNameForHexId(resHexId);
				}
				// If id name is extracted from system resource then add "android:" before the attribute name
				if(systemId2Name != null){
					return android + ":" + systemId2Name;
				}
				// Check if the custom id2name is not null .. and return the entry name without name space
				if(customResId2Name != null){
					return customResId2Name;
				}
				return resHexId;
			} else{
				return resHexId;
			}
		  }catch(Exception e){
			  return resHexId;
		  }
     }
	 
	// Converts a complex number to a float
	public String complexToFloat(int complex) {
		return (TypedValue.complexToFloat(complex) * 100.0f) + FRACTION_UNIT_STRS[complex & 15];
	}
	
	 //Load manifest permission description
	private Map<String, String> loadPermissionsInfo() throws Exception {
		Map<String, String> map = new HashMap<>();
		InputStream is = AXMLPrinter.class.getResourceAsStream("/assets/permissions_info.txt");
		InputStreamReader reader = new InputStreamReader(is);
		BufferedReader bufferedReader = new BufferedReader(reader);
		String permission = null;
		String description = null;
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			line = line.trim();
			// If the line is empty, we can skip it
			if (line.isEmpty()) {
				continue;
			}
			// match the permission and description with regex
			if (line.matches("^[a-zA-Z0-9._]+$")) {
				// If there's an existing permission and description, store it in the map
				if (permission != null && description != null) {
					map.put(permission, description);
				}
				// Now the new permission starts
				permission = line;
				description = null;
			} else {
				// If the line is a description, append it to the current description
				if (description != null) {
					description += " " + line;
				} else {
					description = line;
				}
			}
		}
		// Add the last permission entry to the map
		if (permission != null && description != null) {
			map.put(permission, description);
		}

		return map;
	}
}
