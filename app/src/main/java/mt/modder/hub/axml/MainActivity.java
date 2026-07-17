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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;


public class MainActivity extends Activity {

	private static final int REQUEST_CODE_STORAGE = 1001;
	private static final int REQUEST_CODE_MANAGE_STORAGE = 1002;

	public String Input_Path = "/storage/emulated/0/AndroidManifest.xml";

	// Use an absolute path - a bare "sdcard/..." relative path is not
	// guaranteed to resolve to external storage on every device/API level.
	public String outPath = "/storage/emulated/0/MyDecompiledAXML.xml";

	private TextView text;
	private ScrollView scroll;

	@SuppressLint("SetTextI18n")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		scroll = new ScrollView(this);
		scroll.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
		scroll.setFillViewport(true);
		scroll.setPadding(8, 8, 8, 8);
		text = new TextView(this);
		text.setTextIsSelectable(true);
		text.setPadding(8, 8, 8, 8);
		text.setText("Checking storage permission ...");

		scroll.addView(text);
		setContentView(scroll);

		// Catch absolutely everything (including Errors like
		// NoClassDefFoundError/ExceptionInInitializerError, which are NOT
		// subclasses of Exception) so the app can never crash silently.
		// Whatever goes wrong gets printed on screen instead - this is the
		// only "log" available when adb/logcat isn't accessible.
		try {
			checkPermissionAndRun();
		} catch (Throwable t) {
			showError(t);
		}
	}

	private void showError(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		text.setText(sw.toString());
		t.printStackTrace();
	}

	/**
	 * Handles the three permission models Android has used over time:
	 *  - API < 23: permission is granted at install time, nothing to do.
	 *  - API 23-29: classic runtime permission (READ/WRITE_EXTERNAL_STORAGE).
	 *  - API 30+: scoped storage - accessing arbitrary paths like
	 *    /storage/emulated/0/AndroidManifest.xml requires the special
	 *    "All files access" (MANAGE_EXTERNAL_STORAGE) permission, which is
	 *    granted through a dedicated system Settings screen, not a normal
	 *    runtime dialog.
	 */
	private void checkPermissionAndRun() {
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				if (Environment.isExternalStorageManager()) {
					runConversion();
				} else {
					text.setText("Need \"All files access\" permission.\nOpening system settings...");
					try {
						Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
						intent.setData(Uri.parse("package:" + getPackageName()));
						startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE);
					} catch (Exception e) {
						Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
						startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE);
					}
				}
			} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				boolean readGranted = checkSelfPermission(
						Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
				boolean writeGranted = checkSelfPermission(
						Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
				if (readGranted && writeGranted) {
					runConversion();
				} else {
					requestPermissions(
							new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
							REQUEST_CODE_STORAGE);
				}
			} else {
				runConversion();
			}
		} catch (Throwable t) {
			showError(t);
		}
	}

	@SuppressLint("SetTextI18n")
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		try {
			if (requestCode == REQUEST_CODE_STORAGE) {
				boolean allGranted = grantResults.length > 0;
				for (int result : grantResults) {
					if (result != PackageManager.PERMISSION_GRANTED) {
						allGranted = false;
						break;
					}
				}
				if (allGranted) {
					runConversion();
				} else {
					text.setText("Storage permission denied. Cannot read " + Input_Path);
				}
			}
		} catch (Throwable t) {
			showError(t);
		}
	}

	@SuppressLint("SetTextI18n")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			if (requestCode == REQUEST_CODE_MANAGE_STORAGE) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
					runConversion();
				} else {
					text.setText("\"All files access\" permission was not granted. Cannot read " + Input_Path);
				}
			}
		} catch (Throwable t) {
			showError(t);
		}
	}

	@SuppressLint("SdCardPath")
	private static final String[] FALLBACK_PATHS = {
			"/storage/emulated/0/AndroidManifest.xml",
			"/storage/emulated/0/Download/AndroidManifest.xml",
			"/storage/emulated/0/Downloads/AndroidManifest.xml",
			"/sdcard/AndroidManifest.xml",
			"/sdcard/Download/AndroidManifest.xml",
	};

	@SuppressLint("SetTextI18n")
	private void runConversion() {
		File inputFile = new File(Input_Path);

		// If the configured path doesn't exist, try a few common locations
		// (e.g. the file was saved to the Download folder instead of the
		// SD card root) before giving up.
		if (!inputFile.exists()) {
			for (String candidate : FALLBACK_PATHS) {
				File candidateFile = new File(candidate);
				if (candidateFile.exists()) {
					inputFile = candidateFile;
					Input_Path = candidate;
					break;
				}
			}
		}

		text.setText("Processing " + Input_Path + " ...");

		if (!inputFile.exists()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Input file not found. Checked:\n");
			sb.append(Input_Path).append("\n");
			for (String candidate : FALLBACK_PATHS) {
				sb.append(candidate).append("\n");
			}
			sb.append("\nPlace a compiled AndroidManifest.xml at one of these paths, "
					+ "or change Input_Path in MainActivity.java.");
			text.setText(sb.toString());
			return;
		}

		try {
			// Read the binary XML file into a byte array.
			// (Not using try-with-resources: java.lang.AutoCloseable only
			// exists from API 19 onward, and this project targets minSdk 14.)
			byte[] byteArray = new byte[(int) inputFile.length()];
			FileInputStream fis = new FileInputStream(inputFile);
			try {
				int offset = 0;
				int read;
				while (offset < byteArray.length
						&& (read = fis.read(byteArray, offset, byteArray.length - offset)) != -1) {
					offset += read;
				}
			} finally {
				fis.close();
			}

			// initialize the axmlprinter class
			AXMLPrinter axmlPrinter = new AXMLPrinter();
			axmlPrinter.setEnableID2Name(true);
			axmlPrinter.setAttrValueTranslation(true);
			axmlPrinter.setExtractPermissionDescription(true);

			// Use the XMLDecompiler to decompile to an XML string.
			// Place your resources.arsc file in the same directory as the xml file
			// if you want custom resource id -> name translation.
			String xmlString = axmlPrinter.readFromFile(Input_Path);

			// Direct process without enabling custom resource id2name:
			// String xmlString = axmlPrinter.convertXml(byteArray);

			// Output the XML string
			saveAsFile(xmlString, outPath);
			text.setText("Processing complete. File saved in " + outPath);
		} catch (Throwable t) {
			showError(t);
		}
	}

	public void saveAsFile(String data, String path) throws IOException {
		File outputFile = new File(path);
		File parent = outputFile.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}
		FileWriter fileWriter = new FileWriter(outputFile);
		try {
			fileWriter.write(data);
		} finally {
			fileWriter.close();
		}
	}

}
