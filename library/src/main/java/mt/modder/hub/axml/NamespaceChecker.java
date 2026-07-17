package mt.modder.hub.axml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class NamespaceChecker {

    private Set<String> attributes;

    public NamespaceChecker() {
        attributes = new HashSet<>();
        loadAttributesFromFile();
    }

    private void loadAttributesFromFile() {
        // On real Android devices files under src/main/assets are NOT reachable
        // via Class.getResourceAsStream() (they are not on the classpath, only
        // accessible through android.content.res.AssetManager). Guard against
        // a null stream so a missing/unreachable resource degrades gracefully
        // instead of throwing a NullPointerException out of this constructor
        // (which would otherwise abort AXMLPrinter construction entirely).
        InputStream inputStream = null;
        try {
            inputStream = NamespaceChecker.class.getResourceAsStream("/assets/no_nameSpace_attrs.txt");
            if (inputStream == null) {
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            // Read each line and add it to the set
            while ((line = reader.readLine()) != null) {
                attributes.add(line.trim());
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public String getNamespace(String attributeName) {
        if (attributes.contains(attributeName)) {
            return ""; // Empty namespace for specified attributes
        } else {
            return "android"; // Default namespace for others
        }
    }
	
	public boolean isAttributeExist(String str){
		if(attributes.contains(str)){
			return true;
		} else {
			return false;
		}
	}
}

