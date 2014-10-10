package org.openspaces.admin.application.hotredeploy;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;

/**
 * @author Anna_Babich
 */
public class PropertiesParser {
    private Configuration configuration;

    public Configuration parse() throws IOException {
        String rootPath = new File("").getAbsoluteFile().getParentFile().getParent();
        String[] pathToResources = {rootPath, "tool", "src", "main", "resources", "properties.sh"};
        String propPath = StringUtils.join(pathToResources, File.separator);
        File file = new File(propPath);
        configuration = new Configuration();
        InputStream input = new FileInputStream(file.getAbsolutePath());
        setProperties(input);
        parseProcessingUnits(file);
        return configuration;
    }

    private void parseProcessingUnits(File file) throws IOException {
        Reader reader = new FileReader(file);
        StreamTokenizer tokenizer = new StreamTokenizer(reader);
        Map<String,String> pus = new HashMap<String, String>();
        int res;
        int counter = 0;
        while((res = tokenizer.nextToken()) != StreamTokenizer.TT_EOF) {
            if (res == StreamTokenizer.TT_WORD){
                if (tokenizer.sval.equals("PU")) {
                    counter++;
                    if (counter == 2) {
                        int res1;
                        int lineNo = tokenizer.lineno();
                        boolean nameDefine = true;
                        String key = null;
                        while (lineNo == tokenizer.lineno()) {
                            res1 = tokenizer.nextToken();
                            if (res1 == StreamTokenizer.TT_WORD) {
                                if (nameDefine){
                                    nameDefine = false;
                                    key = tokenizer.sval;
                                } else {
                                    nameDefine = true;
                                    pus.put(key, tokenizer.sval);
                                }
                            }
                        }
                    }
                }
            }
        }
        configuration.setPus(pus);
        reader.close();
    }

    public void setProperties(InputStream input) throws IOException {
        Properties prop = new Properties();
        prop.load(input);
        configuration.setGsLocation(prop.getProperty("GIGASPACES_LOCATION"));
        configuration.setGsLocators(prop.getProperty("GIGASPACES_LOCATORS"));
        configuration.setGroup(prop.getProperty("LOOKUP_GROUP"));
        configuration.setIdentPuTimeout(prop.getProperty("IDENT_PU_TIMEOUT"));
        configuration.setIdentSpaceModeTimeout(prop.getProperty("IDENT_SPACE_MODE_TIMEOUT"));
        configuration.setDoubleRestart(prop.getProperty("DOUBLE_RESTART"));
        configuration.setIsSecured(prop.getProperty("IS_SECURED"));
        input.close();

    }
}
