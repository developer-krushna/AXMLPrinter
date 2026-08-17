/*
 * AxmlPrinter - An Advanced Axml Printer available with proper xml style/format feature
 * Copyright 2025-2026, developer-krushna
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


package mt.modder.hub.axmlTools;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;

/*
  Utility class for reading entries from APK/ZIP files that have pseudo-encryption
  (general purpose bit flag bit 0 set, but data is not actually encrypted).
  Also handles non-standard compression method values by attempting deflate decompression.
 */

/**
 * Author: @developer-krushna
 * Fixed and improved and comments by Sarvam AI (<a href="https://www.sarvam.ai/">Sarvam AI</a>)
 * there is also feature for loading pseudo encrypted zip file
 */
public class PseudoEncryptionBypass {

    // ZIP constants
    private static final int LOCAL_FILE_HEADER_SIG = 0x04034b50;
    private static final int CENTRAL_DIR_SIG = 0x02014b50;
    private static final int END_OF_CENTRAL_DIR_SIG = 0x06054b50;
    private static final int DATA_DESCRIPTOR_SIG = 0x08074b50;

    // Compression methods
    private static final int METHOD_STORED = 0;
    private static final int METHOD_DEFLATE = 8;

    /**
     * Reads all entries from a ZIP/APK file, bypassing pseudo-encryption.
     */
    public static Map<String, byte[]> readAllEntries(String apkPath) throws IOException {
        return readAllEntries(new File(apkPath));
    }

    /**
     * Reads all entries from a ZIP/APK file, bypassing pseudo-encryption.
     */
    public static Map<String, byte[]> readAllEntries(File apkFile) throws IOException {
        byte[] fileData = readFileToBytes(apkFile);
        return parseZipEntries(fileData);
    }

    /**
     * Reads a single entry from a ZIP/APK file, bypassing pseudo-encryption.
     */
    public static byte[] readEntry(String apkPath, String entryName) throws IOException {
        byte[] fileData = readFileToBytes(new File(apkPath));
        return extractEntry(fileData, entryName);
    }

    /**
     * Reads a single entry from a ZIP/APK file, bypassing pseudo-encryption.
     *
     * @param apkFile The APK/ZIP file
     * @param entryName Name of the entry to extract (e.g. "AndroidManifest.xml")
     * @return Decompressed byte data for the entry, or null if not found
     * @throws IOException if the file cannot be read
     */
    public static byte[] readEntry(File apkFile, String entryName) throws IOException {
        byte[] fileData = readFileToBytes(apkFile);
        return extractEntry(fileData, entryName);
    }

    /**
     * Checks if a ZIP/APK file has pseudo-encryption (encryption bit set
     * but data is not actually encrypted).
     *
     * @param apkPath Path to the APK/ZIP file
     * @return true if pseudo-encryption is detected
     * @throws IOException if the file cannot be read
     */
    public static boolean isPseudoEncrypted(String apkPath) throws IOException {
        byte[] fileData = readFileToBytes(new File(apkPath));
        return detectPseudoEncryption(fileData);
    }

    /**
     * Removes pseudo-encryption from a ZIP/APK file by clearing the encryption bit
     * in all local file headers and central directory entries.
     * Returns a new byte array with the fixed ZIP data.
     *
     * @param apkPath Path to the APK/ZIP file
     * @return Fixed ZIP data with encryption bits cleared
     * @throws IOException if the file cannot be read
     */
    public static byte[] removePseudoEncryption(String apkPath) throws IOException {
        byte[] fileData = readFileToBytes(new File(apkPath));
        return clearEncryptionBits(fileData);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Internal implementation
    // ═══════════════════════════════════════════════════════════════

    /**
     * Reads an entire file into a byte array.
     */
    private static byte[] readFileToBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
		ByteArrayOutputStream bos = new ByteArrayOutputStream((int) file.length())) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }
            return bos.toByteArray();
        }
    }

    /**
     * Parses ZIP local file headers and extracts all entries.
     * Ignores the encryption bit in the general purpose flag.
     * Handles non-standard compression methods by trying deflate.
     */
    private static Map<String, byte[]> parseZipEntries(byte[] zipData) {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        int pos = 0;

        while (pos < zipData.length - 4) {
            int sig = readLEInt(zipData, pos);

            if (sig == LOCAL_FILE_HEADER_SIG) {
                // Parse local file header
                // Offset 0:  signature (4 bytes)
                // Offset 4:  version needed (2 bytes)
                // Offset 6:  general purpose bit flag (2 bytes)
                // Offset 8:  compression method (2 bytes)
                // Offset 10: last mod time (2 bytes)
                // Offset 12: last mod date (2 bytes)
                // Offset 14: CRC-32 (4 bytes)
                // Offset 18: compressed size (4 bytes)
                // Offset 22: uncompressed size (4 bytes)
                // Offset 26: file name length (2 bytes)
                // Offset 28: extra field length (2 bytes)
                // Offset 30: file name + extra field

                if (pos + 30 > zipData.length) break;

                int gpFlag = readLEShort(zipData, pos + 6);
                int compressionMethod = readLEShort(zipData, pos + 8);
                int compressedSize = readLEInt(zipData, pos + 18);
                int uncompressedSize = readLEInt(zipData, pos + 22);
                int nameLen = readLEShort(zipData, pos + 26);
                int extraLen = readLEShort(zipData, pos + 28);

                int nameStart = pos + 30;
                int nameEnd = nameStart + nameLen;
                if (nameEnd > zipData.length) break;

                String entryName = new String(zipData, nameStart, nameLen, StandardCharsets.UTF_8);

                int dataStart = nameEnd + extraLen;
                int dataEnd = dataStart + compressedSize;

                // Handle data descriptor (bit 3 of GP flag)
                // When bit 3 is set, compressed/uncompressed sizes in local header are 0
                // and the real values appear in a data descriptor after the data
                boolean hasDataDescriptor = (gpFlag & 0x08) != 0;
                if (hasDataDescriptor && compressedSize == 0) {
                    // Need to find end of data by searching for data descriptor signature
                    dataEnd = findDataDescriptor(zipData, dataStart);
                    if (dataEnd == -1) {
                        // Can't find data descriptor, try central directory for size
                        compressedSize = findCompressedSizeFromCentralDir(zipData, entryName);
                        if (compressedSize > 0) {
                            dataEnd = dataStart + compressedSize;
                        } else {
                            // Last resort: read until next local file header or central dir
                            dataEnd = findNextHeader(zipData, dataStart);
                            if (dataEnd == -1) {
                                dataEnd = zipData.length;
                            }
                        }
                    }
                }

                if (dataStart >= zipData.length) break;

                // For non-standard compression methods, the compressed_size field
                // may be wrong. The data is often stored uncompressed, so use
                // uncompressedSize as the actual data length.
                if (compressionMethod != METHOD_STORED && compressionMethod != METHOD_DEFLATE) {
                    if (uncompressedSize > 0 && uncompressedSize != (dataEnd - dataStart)) {
                        dataEnd = dataStart + uncompressedSize;
                    }
                }

                // Clamp data end
                if (dataEnd > zipData.length) {
                    dataEnd = zipData.length;
                }

                int actualCompressedSize = dataEnd - dataStart;
                byte[] compressedData = new byte[actualCompressedSize];
                System.arraycopy(zipData, dataStart, compressedData, 0, actualCompressedSize);

                boolean wasPseudoEncrypted = (gpFlag & 0x01) != 0;

                try {
                    byte[] decompressed = decompressEntry(compressionMethod, compressedData, uncompressedSize, hasDataDescriptor);
                    entries.put(entryName, decompressed);
                } catch (Exception e) {
                    // If decompression fails, store raw data
                    entries.put(entryName, compressedData);
                }

                // Move to next entry
                if (hasDataDescriptor) {
                    // Skip past the data descriptor (12 or 16 bytes)
                    int ddEnd = dataEnd;
                    // Check if data descriptor has signature
                    if (ddEnd + 4 <= zipData.length && readLEInt(zipData, ddEnd) == DATA_DESCRIPTOR_SIG) {
                        ddEnd += 16; // sig(4) + crc(4) + compSize(4) + uncomp_size(4)
                    } else {
                        ddEnd += 12; // crc(4) + comp_size(4) + uncomp_size(4)
                    }
                    pos = ddEnd;
                } else {
                    pos = dataEnd;
                }

            } else if (sig == CENTRAL_DIR_SIG || sig == END_OF_CENTRAL_DIR_SIG) {
                // Reached central directory or end — we're done with local headers
                break;
            } else {
                // Unknown signature, try advancing by 1
                pos++;
            }
        }

        return entries;
    }

    /**
     * Extracts a single entry by name from ZIP data.
     */
    private static byte[] extractEntry(byte[] zipData, String targetName) throws IOException {
        int pos = 0;

        while (pos < zipData.length - 4) {
            int sig = readLEInt(zipData, pos);

            if (sig == LOCAL_FILE_HEADER_SIG) {
                if (pos + 30 > zipData.length) break;

                int gpFlag = readLEShort(zipData, pos + 6);
                int compressionMethod = readLEShort(zipData, pos + 8);
                int compressedSize = readLEInt(zipData, pos + 18);
                int uncompressedSize = readLEInt(zipData, pos + 22);
                int nameLen = readLEShort(zipData, pos + 26);
                int extraLen = readLEShort(zipData, pos + 28);

                int nameStart = pos + 30;
                int nameEnd = nameStart + nameLen;
                if (nameEnd > zipData.length) break;

                String entryName = new String(zipData, nameStart, nameLen, StandardCharsets.UTF_8);

                int dataStart = nameEnd + extraLen;
                int dataEnd = dataStart + compressedSize;

                boolean hasDataDescriptor = (gpFlag & 0x08) != 0;
                if (hasDataDescriptor && compressedSize == 0) {
                    dataEnd = findDataDescriptor(zipData, dataStart);
                    if (dataEnd == -1) {
                        compressedSize = findCompressedSizeFromCentralDir(zipData, entryName);
                        if (compressedSize > 0) {
                            dataEnd = dataStart + compressedSize;
                        } else {
                            dataEnd = findNextHeader(zipData, dataStart);
                            if (dataEnd == -1) dataEnd = zipData.length;
                        }
                    }
                }

                // For non-standard compression methods, the compressed_size field
                // may be wrong. The data is often stored uncompressed, so use
                // uncompressedSize as the actual data length.
                if (compressionMethod != METHOD_STORED && compressionMethod != METHOD_DEFLATE) {
                    if (uncompressedSize > 0 && uncompressedSize != (dataEnd - dataStart)) {
                        dataEnd = dataStart + uncompressedSize;
                    }
                }

                if (dataEnd > zipData.length) dataEnd = zipData.length;

                if (entryName.equals(targetName)) {
                    int actualSize = dataEnd - dataStart;
                    byte[] compressedData = new byte[actualSize];
                    System.arraycopy(zipData, dataStart, compressedData, 0, actualSize);

                    return decompressEntry(compressionMethod, compressedData, uncompressedSize, hasDataDescriptor);
                }

                // Move to next entry
                if (hasDataDescriptor) {
                    int ddEnd = dataEnd;
                    if (ddEnd + 4 <= zipData.length && readLEInt(zipData, ddEnd) == DATA_DESCRIPTOR_SIG) {
                        ddEnd += 16;
                    } else {
                        ddEnd += 12;
                    }
                    pos = ddEnd;
                } else {
                    pos = dataEnd;
                }

            } else if (sig == CENTRAL_DIR_SIG || sig == END_OF_CENTRAL_DIR_SIG) {
                break;
            } else {
                pos++;
            }
        }

        return null;
    }

    /**
     * Decompresses entry data based on compression method.
     * Handles standard methods and non-standard methods by trying deflate.
     */
    private static byte[] decompressEntry(int compressionMethod, byte[] compressedData,
										  int uncompressedSize, boolean hasDataDescriptor) throws IOException {

        if (compressionMethod == METHOD_STORED) {
            // No compression — data is stored as-is
            return compressedData;
        }

        if (compressionMethod == METHOD_DEFLATE) {
            // Standard deflate
            return inflate(compressedData, uncompressedSize);
        }

        // Non-standard compression method (e.g. 0x4450 / 17488)
        // Some pseudo-encryption tools set a weird compression method but the data
        // is actually stored raw (uncompressed). Check for known magic bytes first.

        // Check if data looks like a valid AXML file (starts with 0x00080003 in LE)
        if (compressedData.length >= 4) {
            int magic = readLEInt(compressedData, 0);
            if (magic == 0x00080003) {
                // Raw AXML data — stored uncompressed despite non-standard method
                return compressedData;
            }
        }

        // Check if data looks like a DEX file (starts with "dex\n" magic)
        if (compressedData.length >= 4) {
            String magicStr = new String(compressedData, 0, Math.min(4, compressedData.length), StandardCharsets.US_ASCII);
            if (magicStr.startsWith("dex")) {
                return compressedData;
            }
        }

        // Check if data looks like a PNG file
        if (compressedData.length >= 4) {
            if ((compressedData[0] & 0xFF) == 0x89 && (compressedData[1] & 0xFF) == 0x50 &&
                (compressedData[2] & 0xFF) == 0x4E && (compressedData[3] & 0xFF) == 0x47) {
                return compressedData;
            }
        }

        // Check if data looks like a ZIP file
        if (compressedData.length >= 4) {
            int sig = readLEInt(compressedData, 0);
            if (sig == LOCAL_FILE_HEADER_SIG || sig == 0x06054b50) {
                return compressedData;
            }
        }

        // Try deflate decompression — but verify the result is valid
        try {
            byte[] result = inflate(compressedData, uncompressedSize);
            // Validate: result should be non-empty and match expected size (if known)
            if (result.length > 0) {
                if (uncompressedSize > 0 && result.length != uncompressedSize) {
                    // Size mismatch — deflate probably produced garbage, treat as stored
                    return compressedData;
                }
                return result;
            }
            // Deflate produced 0 bytes — data is probably stored raw
            return compressedData;
        } catch (IOException e1) {
            // Deflate failed entirely — treat as stored
            return compressedData;
        }
    }

    /**
     * Decompresses raw deflate data.
     * Uses NOWRAP mode (wbits = -15) since ZIP stores raw deflate without zlib headers.
     */
    private static byte[] inflate(byte[] compressedData, int expectedSize) throws IOException {
        Inflater inflater = new Inflater(true); // true = raw deflate (no zlib header)
        inflater.setInput(compressedData);

        int bufferSize = expectedSize > 0 ? expectedSize : compressedData.length * 4;
        if (bufferSize < 256) bufferSize = 256;
        if (bufferSize > 65536) bufferSize = 65536;

        ByteArrayOutputStream bos = new ByteArrayOutputStream(bufferSize);
        byte[] buffer = new byte[4096];

        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                if (count == 0) {
                    if (inflater.needsInput() || inflater.needsDictionary()) {
                        break;
                    }
                }
                bos.write(buffer, 0, count);
            }
        } catch (DataFormatException e) {
            throw new IOException("Deflate decompression failed: " + e.getMessage(), e);
        } finally {
            inflater.end();
        }

        return bos.toByteArray();
    }

    /**
     * Searches for a data descriptor signature after the compressed data.
     * Returns the position of the data descriptor (before the signature if present).
     */
    private static int findDataDescriptor(byte[] data, int start) {
        for (int i = start; i < data.length - 4; i++) {
            int sig = readLEInt(data, i);
            if (sig == DATA_DESCRIPTOR_SIG) {
                return i;
            }
            // Also check for central directory or next local file header
            if (sig == CENTRAL_DIR_SIG || sig == LOCAL_FILE_HEADER_SIG || sig == END_OF_CENTRAL_DIR_SIG) {
                // Data might end just before this, with a descriptor without signature
                // Check if 12 bytes before this looks like a valid descriptor
                if (i >= 12) {
                    return i - 12; // No signature, just 3 ints
                }
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the next local file header or central directory signature.
     */
    private static int findNextHeader(byte[] data, int start) {
        for (int i = start; i < data.length - 4; i++) {
            int sig = readLEInt(data, i);
            if (sig == LOCAL_FILE_HEADER_SIG || sig == CENTRAL_DIR_SIG || sig == END_OF_CENTRAL_DIR_SIG) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Looks up the compressed size from the central directory for a given entry name.
     * Used as fallback when local header has 0 size (data descriptor case).
     */
    private static int findCompressedSizeFromCentralDir(byte[] data, String entryName) {
        int pos = 0;
        while (pos < data.length - 4) {
            int sig = readLEInt(data, pos);
            if (sig == CENTRAL_DIR_SIG) {
                if (pos + 46 > data.length) break;

                int nameLen = readLEShort(data, pos + 28);
                int extraLen = readLEShort(data, pos + 30);
                int commentLen = readLEShort(data, pos + 32);
                int compressedSize = readLEInt(data, pos + 20);

                int nameStart = pos + 46;
                if (nameStart + nameLen > data.length) break;

                String name = new String(data, nameStart, nameLen, java.nio.charset.StandardCharsets.UTF_8);
                if (name.equals(entryName)) {
                    return compressedSize;
                }

                pos = nameStart + extraLen + commentLen;
            } else if (sig == END_OF_CENTRAL_DIR_SIG) {
                break;
            } else {
                pos++;
            }
        }
        return -1;
    }

    /**
     * Detects pseudo-encryption by checking if the encryption bit is set
     * in any local file header while the data appears to be unencrypted deflate.
     */
    private static boolean detectPseudoEncryption(byte[] zipData) {
        int pos = 0;
        while (pos < zipData.length - 4) {
            int sig = readLEInt(zipData, pos);
            if (sig == LOCAL_FILE_HEADER_SIG) {
                if (pos + 30 > zipData.length) break;

                int gpFlag = readLEShort(zipData, pos + 6);
                int compressionMethod = readLEShort(zipData, pos + 8);
                int compressedSize = readLEInt(zipData, pos + 18);
                int nameLen = readLEShort(zipData, pos + 26);
                int extraLen = readLEShort(zipData, pos + 28);

                boolean encrypted = (gpFlag & 0x01) != 0;

                if (encrypted) {
                    // Check if the data is actually deflate (not encrypted)
                    int dataStart = pos + 30 + nameLen + extraLen;
                    if (dataStart < zipData.length) {
                        // Try to decompress as deflate — if it works, it's pseudo-encryption
                        int dataEnd = Math.min(dataStart + compressedSize, zipData.length);
                        byte[] testData = new byte[Math.min(dataEnd - dataStart, 32)];
                        System.arraycopy(zipData, dataStart, testData, 0, testData.length);
                        try {
                            Inflater inf = new Inflater(true);
                            inf.setInput(testData);
                            byte[] buf = new byte[64];
                            inf.inflate(buf);
                            inf.end();
                            return true; // Successfully decompressed → pseudo-encryption
                        } catch (Exception e) {
                            // Could be genuinely encrypted
                        }
                    }
                }

                int dataEnd = pos + 30 + nameLen + extraLen + compressedSize;
                pos = dataEnd;
            } else if (sig == CENTRAL_DIR_SIG || sig == END_OF_CENTRAL_DIR_SIG) {
                break;
            } else {
                pos++;
            }
        }
        return false;
    }

    /**
     * Clears the encryption bit (bit 0) in all local file headers and
     * central directory entries. Also normalizes non-standard compression
     * methods to DEFLATE (8) where appropriate.
     *
     * @return New byte array with encryption bits cleared
     */
    private static byte[] clearEncryptionBits(byte[] zipData) {
        byte[] result = new byte[zipData.length];
        System.arraycopy(zipData, 0, result, 0, zipData.length);

        // Clear in local file headers
        int pos = 0;
        while (pos < result.length - 4) {
            int sig = readLEInt(result, pos);
            if (sig == LOCAL_FILE_HEADER_SIG) {
                if (pos + 30 > result.length) break;

                // Clear encryption bit (bit 0) in general purpose flag at offset 6
                int gpFlag = readLEShort(result, pos + 6);
                gpFlag &= ~0x01; // Clear bit 0
                writeLEShort(result, pos + 6, gpFlag);

                int compressionMethod = readLEShort(result, pos + 8);
                int compressedSize = readLEInt(result, pos + 18);
                int uncompressedSize = readLEInt(result, pos + 22);
                int nameLen = readLEShort(result, pos + 26);
                int extraLen = readLEShort(result, pos + 28);

                // For non-standard compression methods, fix the entry so standard
                // ZipFile can read it. Try deflate first; if that fails, treat as STORED.
                if (compressionMethod != METHOD_STORED && compressionMethod != METHOD_DEFLATE) {
                    int dataStart = pos + 30 + nameLen + extraLen;
                    boolean fixedAsDeflate = false;
                    if (compressedSize > 0 && dataStart + compressedSize <= result.length) {
                        try {
                            byte[] testData = new byte[compressedSize];
                            System.arraycopy(result, dataStart, testData, 0, compressedSize);
                            byte[] inflated = inflate(testData, uncompressedSize);
                            if (inflated.length > 0 && (uncompressedSize == 0 || inflated.length == uncompressedSize)) {
                                // Data is valid deflate — keep as DEFLATE
                                writeLEShort(result, pos + 8, METHOD_DEFLATE);
                                fixedAsDeflate = true;
                            }
                        } catch (IOException e) {
                            // Not deflate — fall through to STORED
                        }
                    }
                    if (!fixedAsDeflate) {
                        // Data is stored uncompressed — set method to STORED
                        // and fix compressed_size to match uncompressed_size
                        writeLEShort(result, pos + 8, METHOD_STORED);
                        writeLEInt(result, pos + 18, uncompressedSize);
                        compressedSize = uncompressedSize;
                    }
                }

                boolean hasDataDescriptor = (gpFlag & 0x08) != 0;
                if (hasDataDescriptor && compressedSize == 0) {
                    pos = findNextHeader(result, pos + 30 + nameLen + extraLen);
                    if (pos == -1) break;
                } else {
                    pos = pos + 30 + nameLen + extraLen + compressedSize;
                    // Skip data descriptor if present
                    if (hasDataDescriptor) {
                        if (pos + 4 <= result.length && readLEInt(result, pos) == DATA_DESCRIPTOR_SIG) {
                            pos += 16;
                        } else {
                            pos += 12;
                        }
                    }
                }
            } else if (sig == CENTRAL_DIR_SIG) {
                if (pos + 46 > result.length) break;

                // Clear encryption bit in central directory entry
                int gpFlag = readLEShort(result, pos + 8);
                gpFlag &= ~0x01;
                writeLEShort(result, pos + 8, gpFlag);

                // Normalize compression method — match local header fix
                int compressionMethod = readLEShort(result, pos + 10);
                int cdCompressedSize = readLEInt(result, pos + 20);
                int cdUncompressedSize = readLEInt(result, pos + 24);
                if (compressionMethod != METHOD_STORED && compressionMethod != METHOD_DEFLATE) {
                    // Set to STORED and fix compressed_size to match uncompressed_size
                    writeLEShort(result, pos + 10, METHOD_STORED);
                    writeLEInt(result, pos + 20, cdUncompressedSize);
                }

                int nameLen = readLEShort(result, pos + 28);
                int extraLen = readLEShort(result, pos + 30);
                int commentLen = readLEShort(result, pos + 32);
                pos = pos + 46 + nameLen + extraLen + commentLen;
            } else if (sig == END_OF_CENTRAL_DIR_SIG) {
                break;
            } else {
                pos++;
            }
        }

        return result;
    }

    // ═══════════════════════════════════════════════════════════════
    //  AXML corruption repair
    // ═══════════════════════════════════════════════════════════════

    // AXML chunk type constants
    private static final int CHUNK_AXML_FILE = 0x00080003;
    private static final int CHUNK_STRING_POOL = 0x001C0001;
    private static final int CHUNK_RESOURCEIDS = 0x00080180;
    private static final int CHUNK_XML_START_NAMESPACE = 0x00100100;
    private static final int CHUNK_XML_END_NAMESPACE = 0x00100101;
    private static final int CHUNK_XML_START_TAG = 0x00100102;
    private static final int CHUNK_XML_END_TAG = 0x00100103;
    private static final int CHUNK_XML_TEXT = 0x00100104;

    // Standard AXML attribute size (5 ints × 4 bytes)
    private static final int ATTR_SIZE = 20;

    /**
     * Detects if AXML binary data has corruption caused by pseudo-encryption
     * or APK protection tools. Checks for:
     * - Corrupted magic byte (byte 0 changed from 0x03 to 0x00)
     * - Inflated stringCount (larger than what fits before stringDataOffset)
     * - Wrong attributeSize in START_TAG chunks (not 20)
     * - Wrong chunkSize in START_TAG chunks (doesn't match headerSize + attrStart + attrCount × 20)
     *
     * @param axmlData Raw AXML binary data
     * @return true if any corruption is detected
     */
    public static boolean isAxmlCorrupted(byte[] axmlData) {
        if (axmlData == null || axmlData.length < 36) return false;

        // Check magic byte corruption
        if (axmlData[1] == 0x00 && axmlData[2] == 0x08 && axmlData[3] == 0x00
			&& axmlData[0] != 0x03) {
            return true;
        }

        int magic = readLEInt(axmlData, 0);
        if (magic != CHUNK_AXML_FILE) return false;

        // Check stringCount
        int stringCount = readLEInt(axmlData, 16);
        int styleCount = readLEInt(axmlData, 20);
        int stringDataOffset = readLEInt(axmlData, 28);
        if (stringDataOffset >= 28 && stringDataOffset < axmlData.length) {
            int maxSc = (stringDataOffset - 28 - styleCount * 4) / 4;
            if (stringCount > maxSc) return true;
        }

        // Check START_TAG chunks
        int spChunkSize = readLEInt(axmlData, 12);
        if (spChunkSize < 8 || 8 + spChunkSize > axmlData.length) return false;

        int pos = 8 + spChunkSize;
        while (pos + 8 <= axmlData.length) {
            int chunkType = readLEInt(axmlData, pos);
            int chunkSize = readLEInt(axmlData, pos + 4);
            if (chunkSize < 8 || pos + chunkSize > axmlData.length) break;

            if (chunkType == CHUNK_XML_START_TAG) {
                int headerSize = readLEShort(axmlData, pos + 2);
                int attrStart = readLEShort(axmlData, pos + 24);
                int attrSize = readLEShort(axmlData, pos + 26);
                int attrCount = readLEShort(axmlData, pos + 28);
                int expectedChunkSize = headerSize + attrStart + attrCount * ATTR_SIZE;
                if (attrSize != ATTR_SIZE || chunkSize != expectedChunkSize) {
                    return true;
                }
            }
            pos += chunkSize;
        }
        return false;
    }

    /**
     * Fixes corrupted AXML binary data caused by pseudo-encryption tools.
     * Handles four types of corruption:
     * <p>
     * 1. Magic byte: byte 0 changed from 0x03 to 0x00 (or other values).
     *    Fixed by restoring byte 0 to 0x03.
     * <p>
     * 2. stringCount in string pool header — inflated to a value larger
     *    than what fits before stringDataOffset, causing StringBlock.read()
     *    to read past string data into XML chunks.
     *    Fixed by recalculating: stringCount = (stringDataOffset - 28 - styleCount×4) / 4.
     * <p>
     * 3. attributeSize in START_TAG chunks — changed from 20 to another value
     *    (e.g. 24), adding extra padding bytes per attribute. Fixed by stripping
     *    padding and setting attrSize=20.
     * <p>
     * 4. chunkSize in START_TAG chunks — wrong because of extra padding added
     *    after attributes (even when attrSize is correct). Fixed by recalculating
     *    chunkSize = headerSize + attrStart + attrCount × 20.
     * <p>
     * This method rebuilds the entire AXML binary with corrected fields and
     * stripped padding, producing data that standard AXML parsers can consume.
     *
     * @param axmlData Raw AXML binary data (possibly corrupted)
     * @return Fixed AXML binary data (new array if repairs were needed, same array if clean)
     */
    public static byte[] fixAxmlCorruption(byte[] axmlData) {
        if (axmlData == null || axmlData.length < 36) {
            return axmlData;
        }

        byte[] data = axmlData;
        boolean magicFixed = false;

        // ── Fix 1: Magic byte ──
        // Expected magic: 0x00080003 = bytes [03, 00, 08, 00]
        // Some tools change byte 0 from 0x03 to 0x00
        if (data[1] == 0x00 && data[2] == 0x08 && data[3] == 0x00
			&& data[0] != 0x03) {
            data = new byte[axmlData.length];
            System.arraycopy(axmlData, 0, data, 0, axmlData.length);
            data[0] = 0x03;
            magicFixed = true;
        }

        int magic = readLEInt(data, 0);
        if (magic != CHUNK_AXML_FILE) {
            return axmlData; // Not AXML at all
        }

        // ── Fix 2: stringCount ──
        int stringCount = readLEInt(data, 16);
        int styleCount = readLEInt(data, 20);
        int stringDataOffset = readLEInt(data, 28);

        boolean stringCountFixed = false;
        if (stringDataOffset >= 28 && stringDataOffset < data.length) {
            int availableSpace = stringDataOffset - 28;
            int maxStringCount = (availableSpace - styleCount * 4) / 4;
            if (maxStringCount < 0) maxStringCount = 0;
            if (stringCount > maxStringCount) {
                stringCount = maxStringCount;
                stringCountFixed = true;
            }
        }

        // ── Fix 3 & 4: Walk chunks, fix START_TAG attrSize + chunkSize ──
        int spChunkSize = readLEInt(data, 12);
        if (spChunkSize < 8 || 8 + spChunkSize > data.length) {
            // String pool size is wrong — just fix stringCount in place and return
            if (stringCountFixed) {
                if (data == axmlData) {
                    data = new byte[axmlData.length];
                    System.arraycopy(axmlData, 0, data, 0, axmlData.length);
                }
                writeLEInt(data, 16, stringCount);
            }
            return data;
        }

        // Scan for chunk corruption first (to decide if we need to rebuild)
        boolean needsRebuild = stringCountFixed || magicFixed;
        int scanPos = 8 + spChunkSize;
        while (scanPos + 8 <= data.length) {
            int chunkType = readLEInt(data, scanPos);
            int chunkSize = readLEInt(data, scanPos + 4);
            if (chunkSize < 8 || scanPos + chunkSize > data.length) break;

            if (chunkType == CHUNK_XML_START_TAG) {
                int headerSize = readLEShort(data, scanPos + 2);
                int attrStart = readLEShort(data, scanPos + 24);
                int attrSize = readLEShort(data, scanPos + 26);
                int attrCount = readLEShort(data, scanPos + 28);
                int expectedChunkSize = headerSize + attrStart + attrCount * ATTR_SIZE;
                if (attrSize != ATTR_SIZE || chunkSize != expectedChunkSize) {
                    needsRebuild = true;
                    break;
                }
            }
            scanPos += chunkSize;
        }

        if (!needsRebuild) {
            return data;
        }

        // Rebuild the AXML with all fixes applied
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length);

        // Write AXML header (magic + filesize) — filesize updated later
        out.write(data, 0, 8);

        // Write string pool chunk with fixed stringCount
        byte[] spChunk = new byte[spChunkSize];
        System.arraycopy(data, 8, spChunk, 0, spChunkSize);
        if (stringCountFixed) {
            writeLEInt(spChunk, 8, stringCount); // offset 8 in spChunk = offset 16 in AXML
        }
        out.write(spChunk, 0, spChunk.length);

        // Walk remaining chunks and fix START_TAG chunks
        int pos = 8 + spChunkSize;
        while (pos + 8 <= data.length) {
            int chunkType = readLEInt(data, pos);
            int chunkSize = readLEInt(data, pos + 4);

            if (chunkSize < 8 || pos + chunkSize > data.length) {
                break;
            }

            if (chunkType == CHUNK_XML_START_TAG) {
                int headerSize = readLEShort(data, pos + 2);
                int attrStart = readLEShort(data, pos + 24);
                int attrSize = readLEShort(data, pos + 26);
                int attrCount = readLEShort(data, pos + 28);

                int expectedChunkSize = headerSize + attrStart + attrCount * ATTR_SIZE;

                if (attrSize != ATTR_SIZE || chunkSize != expectedChunkSize) {
                    // Chunk is corrupted — rebuild it
                    int attrDataStart = pos + headerSize + attrStart;
                    int newChunkSize = expectedChunkSize;
                    int headerLen = attrDataStart - pos;

                    byte[] fixedChunk = new byte[newChunkSize];
                    // Copy header up to attribute data
                    System.arraycopy(data, pos, fixedChunk, 0, headerLen);
                    // Fix attrSize field (offset 26 in chunk = 0x0014 = 20)
                    fixedChunk[26] = 0x14;
                    fixedChunk[27] = 0x00;
                    // Fix chunkSize
                    writeLEInt(fixedChunk, 4, newChunkSize);

                    // Copy each attribute — only first 20 bytes, skip padding
                    int srcAttrSize = (attrSize > 0) ? attrSize : ATTR_SIZE;
                    for (int a = 0; a < attrCount; a++) {
                        int srcOff = attrDataStart + a * srcAttrSize;
                        int dstOff = headerLen + a * ATTR_SIZE;
                        int copyLen = Math.min(ATTR_SIZE, data.length - srcOff);
                        if (copyLen > 0) {
                            System.arraycopy(data, srcOff, fixedChunk, dstOff, copyLen);
                        }
                    }

                    out.write(fixedChunk, 0, newChunkSize);
                } else {
                    // Normal chunk — copy as-is
                    out.write(data, pos, chunkSize);
                }
            } else {
                // Non-START_TAG chunk — copy as-is
                out.write(data, pos, chunkSize);
            }

            pos += chunkSize;
        }

        byte[] result = out.toByteArray();
        // Update AXML file size field
        writeLEInt(result, 4, result.length);
        return result;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Little-endian helpers
    // ═══════════════════════════════════════════════════════════════

    private static int readLEInt(byte[] data, int offset) {
        return (data[offset] & 0xFF) |
			((data[offset + 1] & 0xFF) << 8) |
			((data[offset + 2] & 0xFF) << 16) |
			((data[offset + 3] & 0xFF) << 24);
    }

    private static int readLEShort(byte[] data, int offset) {
        return (data[offset] & 0xFF) |
			((data[offset + 1] & 0xFF) << 8);
    }

    private static void writeLEShort(byte[] data, int offset, int value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
    }

    private static void writeLEInt(byte[] data, int offset, int value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
        data[offset + 2] = (byte) ((value >> 16) & 0xFF);
        data[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }
}

