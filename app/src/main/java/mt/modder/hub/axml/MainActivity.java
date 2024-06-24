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

import android.app.*;
import android.os.*;
import java.io.FileInputStream;
import java.io.IOException;
import mt.modder.hub.axml.AXMLPrinter;
import android.widget.TextView;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import android.widget.ScrollView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
	
	public String Input_Path = "/storage/emulated/0/MT2/apks/AndroidManifest.xml";
	
	public String outPath = "sdcard/MyDecompiledAXML.xml";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
		final ScrollView scroll = new ScrollView(this);	
		scroll.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
		scroll.setFillViewport(true);
		scroll.setPadding(8,8,8,8);
		
		TextView text = new TextView(this);
		text.setText("Processing "+ Input_Path +" ...");
		try {
            // Read the binary XML file into a byte array
            FileInputStream fis = new FileInputStream(Input_Path);
            /*byte[] byteArray = new byte[fis.available()];
            fis.read(byteArray);
            fis.close();*/
			
			// initialize the axmlprinter class
			AXMLPrinter axmlPrinter = new AXMLPrinter();
			axmlPrinter.setAttributeIntConversion(true);

            // Use the XMLDecompiler to decompile to an XML string
            String xmlString = axmlPrinter.convertXml(fis);

            // Output the XML string
            saveAsFile(xmlString, outPath);
			text.setText("Processing complete .File saved in " + outPath);
        } catch (IOException e) {
			// Complete extraction of error 
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String exceptionDetails = sw.toString();
			text.setText(exceptionDetails);
            e.printStackTrace();
        }
		scroll.addView(text);
        setContentView(scroll);
    }
	
	public void saveAsFile(String data, String path) throws IOException{
		File outputFile = new File(path);
		FileWriter fileWriter = new FileWriter(outputFile);
		fileWriter.write(data.toString());
		fileWriter.close();
	}
}
