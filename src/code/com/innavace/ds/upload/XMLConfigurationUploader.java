/* Copyright 2013 Stephen Stacha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.innavace.ds.upload;

import com.innavace.ds.config.Configuration;
import com.innavace.ds.config.ConfigurationHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * User: sstacha
 * Date: Mar 5, 2013
 * handles parsing and uploading the configurations provided in an xml file
 */
public class XMLConfigurationUploader extends DefaultHandler {

//    HttpServletRequest request;
    Configuration configuration;
    StringBuilder characters = new StringBuilder(200);
    int imported = 0;
    int skipped = 0;
    public HttpServletRequest request;

    @Override
    public void startElement(String s, String s2, String elementName, Attributes attributes) throws SAXException {
        if (elementName.equalsIgnoreCase("configurations"))
            imported = 0;
        else if (elementName.equalsIgnoreCase("configuration"))
            configuration = new Configuration();
        else
            characters.setLength(0);

    }

    @Override
    public void endElement(String s, String s2, String elementName) throws SAXException {
        if (elementName.equalsIgnoreCase("connectionName"))
            configuration.connectionName = characters.toString();
        else if (elementName.equalsIgnoreCase("path"))
            configuration.path = characters.toString();
        else if (elementName.equalsIgnoreCase("querySql"))
            configuration.queryStatement = characters.toString();
        else if (elementName.equalsIgnoreCase("insertSql"))
            configuration.insertStatement = characters.toString();
        else if (elementName.equalsIgnoreCase("updateSql"))
            configuration.updateStatement = characters.toString();
        else if (elementName.equalsIgnoreCase("deleteSql"))
            configuration.deleteStatement = characters.toString();
        else if (elementName.equalsIgnoreCase("keywords"))
            configuration.keywords = characters.toString();
        else if (elementName.equalsIgnoreCase("configuration")) {
            // using the current systems configurations save the data
            // for now just print it out
//            System.out.println("&&&&&&&    importing: " + configuration.toString());
            boolean updated = false;
            try {updated = ConfigurationHandler.updateConfiguration(configuration);}
            catch (Exception ex) {System.out.println("got exception attempting to upload configuration [" + configuration.path + "]: " + ex);}
            if (updated)
                imported++;
            else
                skipped++;
        }
    }

    @Override
    public void characters(char[] chars, int i, int i2) throws SAXException {
        characters.append(chars, i, i2);
    }

    public String getStatus() {
        return "imported [" + imported + "] records.\nskipped[" + skipped + "] records.";
    }

    public static void main(String[] args) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            XMLConfigurationUploader handler = new XMLConfigurationUploader();
            saxParser.parse("/Users/sstacha/tmp/configurations.xml", handler);
            System.out.println(handler.getStatus());
        }
        catch (Exception ex) {System.out.println("exception in main: "+ ex);}

    }
}
