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

import android.content.*;
import android.widget.*;
import java.io.*;
import mt.modder.hub.axmlTools.utils.*;


/*
 Author @developer-krushna
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
	public static String saveManifestPrefix = null;
	
	
	
	// Converts a complex number to a float
	public float complexToFloat(int complex) {
		return (complex & (-256)) * RADIX_MULTS[(complex >> 4) & 3];
	}

	// Set check attribute and convert int to its string value
	public void setEnableID2Name(boolean isId2name){
		isId2Name = isId2name;
		
		
	}

	// Main method to decompile an XML byte array
	public String convertXml(byte[] byteArray) {
		System.out.println(COPYRIGHT);
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
					saveManifestPrefix = null;
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
						
						xmlContent.append(String.format("%s<%s%s", indentation, getNamespacePrefix_1(xmlParser.getPrefix()), prefix));
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
							// Skip attributes with a dot (.)
							if (xmlParser.getAttributeName(i).contains(".")) {
								continue; // Skip this attribute if its name contains a dot
							}

							String attributeFormat = (i == attributeCount - 1) ? "%s%s%s=\"%s\"" : "%s%s%s=\"%s\"\n";
							xmlContent.append(String.format(attributeFormat, indentation, getNamespacePrefix_2(xmlParser, i), xmlParser.getAttributeName(i), getAttributeValue(xmlParser, i)));
						}
						break;

					case XmlPullParser.END_TAG:
						// Handle the end of an XML tag
						indentation.setLength(indentation.length() - "    ".length());
						if (!isEndOf(xmlParser, xmlParser.getPrevious())) {
							xmlContent.append(String.format("%s</%s%s>\n", indentation, getNamespacePrefix_1(xmlParser.getPrefix()), xmlParser.getName()));
						} else {
							xmlContent.append(" />\n");
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
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String exceptionDetails = sw.toString();
			return "----StackTrace----\n" + exceptionDetails;
		}
	}

	// Retrieves the attribute value in a human-readable format based on its type
	private String getAttributeValue(AXmlResourceParser xmlParser, int index) {
		
		String attributeName = xmlParser.getAttributeName(index);
		
		int attributeValueType = xmlParser.getAttributeValueType(index);
		
		int attributeValueData = xmlParser.getAttributeValueData(index);
		
		switch (attributeValueType) {
			case TypedValue.TYPE_STRING /* 3 */:
				// String value
				
				return xmlParser.getAttributeValue(index);
				
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
					String decodedValue = AttributesExtractor.getInstance().decode(attributeName, attributeValueData);
					if (decodedValue != null && !decodedValue.isEmpty() ) {
						return decodedValue; // Return the decoded value if found
					} else {
						return String.format("0x%08x", attributeValueData);
					}
					
			case TypedValue.TYPE_INT_BOOLEAN /* 18 */:
				// Boolean value
				return attributeValueData != 0 ? "true" : "false";
				
			case TypedValue.TYPE_DIMENSION /* 5 */:
				// Dimension value
				return complexToFloat(attributeValueData) + DIMENSION_UNIT_STRS[attributeValueData & 15];
				
			case TypedValue.TYPE_FRACTION /* 6 */:
				// Fraction value
				return complexToFloat(attributeValueData) + FRACTION_UNIT_STRS[attributeValueData & 15];
				
			default:
				// Handle enum or flag values and other cases 
					String decodedValue2 = AttributesExtractor.getInstance().decode(attributeName, attributeValueData);
					if (decodedValue2 != null) {
						return decodedValue2; // Return the decoded value if found
					}
				// For unhandled types or cases
				return (attributeValueType >= 28 && attributeValueType <= 31) ?
					String.format("#%08x", attributeValueData) :
					(attributeValueType >= 16 && attributeValueType <= 31) ?
					String.valueOf(attributeValueData) :
					String.format("<0x%X, type 0x%02X>", attributeValueData, attributeValueType);
		}
	}


	// Retrieves the namespace prefix if it exists
	private  String getNamespacePrefix_1(String prefix) {
		return (prefix == null || prefix.length() == 0) ? "" : prefix + ":";
	}
	
	// for handling null namespace
	private String getNamespacePrefix_2(AXmlResourceParser xmlParser, int position){
		String namespace = xmlParser.getAttributePrefix(position);
		String attributeName = xmlParser.getAttributeName(position);
		if(xmlParser.isChunkResourceIDs = true){
		if (namespace.isEmpty()) {
			NamespaceChecker namespaceChecker = new NamespaceChecker();
			namespace = namespaceChecker.getNamespace(attributeName);
         }
		}
        if (namespace != null && !namespace.isEmpty()) {
            return namespace + ":";
        }
		
		return namespace;
	}

	// Checks if the current XML tag is the end of the previous tag
	private  boolean isEndOf(AXmlResourceParser xmlParser, AXmlResourceParser.OldXMLToken oldXmlToken) {
		return oldXmlToken.type == XmlPullParser.START_TAG &&
			xmlParser.getEventType() == XmlPullParser.END_TAG &&
			xmlParser.getName().equals(oldXmlToken.name) &&
			((oldXmlToken.namespace == null && xmlParser.getPrefix() == null) ||
			(oldXmlToken.namespace != null && xmlParser.getPrefix() != null && xmlParser.getPrefix().equals(oldXmlToken.namespace)));
	}
	
	public String extractResourecID(int i) {
		//TODO
		  return String.format("%08x", new Object[]{Integer.valueOf(i)});
     }

}
