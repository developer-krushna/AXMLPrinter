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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends Activity {

    private static final int REQUEST_CODE_PICK_FILE = 1003;

    // High-performance single-pass regex for XML components
    private static final Pattern PATTERN_XML_TOKEN = Pattern.compile(
            "(<!--[\\s\\S]*?-->)|" +                             // 1: Comment
                    "(<\\?)([a-zA-Z0-9._:-]+)|" +                        // 2,3: Decl start
                    "(\\?>)|" +                                          // 4: Decl end
                    "(<!\\[CDATA\\[[\\s\\S]*?\\]\\]>|<!DOCTYPE)|" +     // 5: Meta/CDATA
                    "(</?|/?>|=)|" +                                    // 6: Operator
                    "\\b([a-zA-Z0-9._-]+:)?([a-zA-Z0-9._-]+)(?=[\\s/>])|" + // 7,8: TagName
                    "\\b([a-zA-Z0-9._-]+:)?([a-zA-Z0-9._-]+)(?=\\s*=)|" +   // 9,10: AttrName
                    "(['\"])([^'\"]*?)(\\11)|" +                         // 11,12,13: String
                    "(&[a-zA-Z0-9#]+;)|" +                              // 14: Entity
                    "\\b(true|false)\\b|" +                             // 15: Boolean
                    "(?i)(?<![;&])#(?:[0-9A-F]{3,4}|[0-9A-F]{6}|[0-9A-F]{8})\\b" // 16: Color
    );

    // Colors from colors.json (Day theme)
    private static final int COLOR_OPERATOR = 0xFF205060;
    private static final int COLOR_KEYWORD = 0xFF0033B3;
    private static final int COLOR_STRING = 0xFF067D17;
    private static final int COLOR_COMMENT = 0xFF8C8C8C;
    private static final int COLOR_META = 0xFF9E880D;
    private static final int COLOR_NUMBER = 0xFF1750EB;
    private static final int COLOR_TAG_NAME = 0xFF0030B3;
    private static final int COLOR_ATTR_NAME = 0xFF174AD4;
    private static final int COLOR_NAMESPACE = 0xFF871094;
    private static final int COLOR_PROP_VAL = 0xFF067D17;
    private static final int COLOR_STR_ESCAPE = 0xFF0037A6;

    private TextView tvPath;
    private TextView tvOutput;
    private Button btnToggleWrap;
    private boolean isWrapEnabled = false;
    private Uri selectedFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvPath = findViewById(R.id.tvPath);
        tvOutput = findViewById(R.id.tvOutput);
        Button btnProcess = findViewById(R.id.btnProcess);
        btnToggleWrap = findViewById(R.id.btnToggleWrap);
        Button btnShare = findViewById(R.id.btnShare);
        Button btnSelectFile = findViewById(R.id.btnSelectFile);

        // Initial state: Wrap OFF (Scrolled horizontally)
        tvOutput.setHorizontallyScrolling(true);

        btnSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFile();
            }
        });

        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedFileUri != null) {
                    processFile(selectedFileUri);
                } else {
                    tvOutput.setText("Please select a file first.");
                }
            }
        });

        btnToggleWrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isWrapEnabled = !isWrapEnabled;
                updateWrapMode();
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareOutput();
            }
        });
    }

    private void shareOutput() {
        CharSequence text = tvOutput.getText();
        if (text == null || text.length() == 0) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text.toString());
        startActivity(Intent.createChooser(intent, "Share Decompiled XML"));
    }

    private void updateWrapMode() {
        if (isWrapEnabled) {
            // Wrap ON: Fixed width forces wrapping
            tvOutput.setHorizontallyScrolling(false);
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int totalPadding = (int) (48 * getResources().getDisplayMetrics().density);
            int targetWidth = screenWidth - totalPadding;

            tvOutput.setMaxWidth(targetWidth);
            tvOutput.setMinWidth(targetWidth);
        } else {
            // Wrap OFF: Remove width limits and allow horizontal expansion
            tvOutput.setHorizontallyScrolling(true);
            tvOutput.setMaxWidth(Integer.MAX_VALUE);
            tvOutput.setMinWidth(0);
            tvOutput.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        btnToggleWrap.setText(isWrapEnabled ? "Wrap: ON" : "Wrap: OFF");
        tvOutput.requestLayout();
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            tvPath.setText("Selected: " + selectedFileUri.toString());
            tvOutput.setText("");
        }
    }

    private void processFile(Uri uri) {
        tvOutput.setText("Processing...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File tempFile = copyUriToTempFile(uri);
                    String fileName = getFileName(uri).toLowerCase();

                    AXMLPrinter axmlPrinter = new AXMLPrinter();
                    axmlPrinter.setEnableID2Name(false);
                    axmlPrinter.setAttrValueTranslation(true);
                    axmlPrinter.setExtractPermissionDescription(true);

                    final String result;
                    if (fileName.endsWith(".apk")) {
                        result = axmlPrinter.readFromApk(tempFile.getAbsolutePath());
                    } else {
                        result = axmlPrinter.readFromFile(tempFile.getAbsolutePath());
                    }

                    // Apply heavy regex highlighting in background thread to avoid UI lag
                    final CharSequence highlighted = highlightXml(result);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvOutput.setText(highlighted);
                        }
                    });
                } catch (final Throwable t) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showError(t);
                        }
                    });
                }
            }
        }).start();
    }

    private CharSequence highlightXml(String xml) {
        if (xml == null || xml.isEmpty()) return "";

        // For extremely large files, limit highlighting to prevent UI freeze during text layout
        boolean isMassive = xml.length() > 100000;
        SpannableStringBuilder spannable = new SpannableStringBuilder(xml);

        // If it's massive, only process a portion to keep it responsive
        String textToMatch = isMassive ? xml.substring(0, 100000) : xml;
        Matcher m = PATTERN_XML_TOKEN.matcher(textToMatch);

        while (m.find()) {
            if (m.group(1) != null) { // Comment
                spannable.setSpan(new ForegroundColorSpan(COLOR_COMMENT), m.start(1), m.end(1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (m.group(2) != null) { // Decl start (<?)
                spannable.setSpan(new ForegroundColorSpan(COLOR_OPERATOR), m.start(2), m.end(2), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new ForegroundColorSpan(COLOR_KEYWORD), m.start(3), m.end(3), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (m.group(4) != null) { // Decl end (?>)
                spannable.setSpan(new ForegroundColorSpan(COLOR_OPERATOR), m.start(4), m.end(4), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (m.group(5) != null) { // CDATA/Meta
                spannable.setSpan(new ForegroundColorSpan(COLOR_META), m.start(5), m.end(5), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (m.group(6) != null) { // Operator
                spannable.setSpan(new ForegroundColorSpan(COLOR_OPERATOR), m.start(6), m.end(6), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (m.group(8) != null) { // TagName
                if (m.group(7) != null) {
                    spannable.setSpan(new ForegroundColorSpan(COLOR_NAMESPACE), m.start(7), m.end(7), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                spannable.setSpan(new ForegroundColorSpan(COLOR_TAG_NAME), m.start(8), m.end(8), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (m.group(10) != null) { // AttrName
                if (m.group(9) != null) {
                    spannable.setSpan(new ForegroundColorSpan(COLOR_NAMESPACE), m.start(9), m.end(9), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                spannable.setSpan(new ForegroundColorSpan(COLOR_ATTR_NAME), m.start(10), m.end(10), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (m.group(12) != null) { // String/Value
                spannable.setSpan(new ForegroundColorSpan(COLOR_STRING), m.start(11), m.start(11) + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new ForegroundColorSpan(COLOR_PROP_VAL), m.start(12), m.end(12), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new ForegroundColorSpan(COLOR_STRING), m.end(13) - 1, m.end(13), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (m.group(14) != null) { // Entity
                spannable.setSpan(new ForegroundColorSpan(COLOR_STR_ESCAPE), m.start(14), m.end(14), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (m.group(15) != null) { // Boolean
                spannable.setSpan(new ForegroundColorSpan(COLOR_NUMBER), m.start(15), m.end(15), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (m.group(16) != null) { // Color
                spannable.setSpan(new ForegroundColorSpan(COLOR_NUMBER), m.start(16), m.end(16), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        return spannable;
    }

    private File copyUriToTempFile(Uri uri) throws IOException {
        File tempFile = new File(getCacheDir(), "temp_input");
        InputStream is = getContentResolver().openInputStream(uri);
        if (is == null) throw new IOException("Failed to open input stream");
        try {
            OutputStream os = new FileOutputStream(tempFile);
            try {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }
        return tempFile;
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void showError(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        tvOutput.setText("Error occurred:\n" + sw.toString());
    }
}
