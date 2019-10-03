package com.mlindner;

import org.junit.Assert;
import org.junit.Test;
import java.io.*;

public class AppTest {

    private static final String ROOT_PATH = "test";
    public static String EMAIL_WORKING_DIR = "emailsDir";
    public static String EXPECTED_ARCHIVE_STRUCTURE = "\\sampleEmails\\smallset";

    @Test
    public void iterateOverFiles() {
    }

    @Test
    public void processFile() {
        String args[] = null;
        App app = new App();
        app.main(args);
        BufferedReader br = null;
        String filenameSTR = EMAIL_WORKING_DIR + EXPECTED_ARCHIVE_STRUCTURE + "\\20110401_aamarketinginc_14456749_html.msg"; 
        try {
            System.out.println(filenameSTR);
            br = new BufferedReader(new FileReader(filenameSTR));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assert.assertEquals("survey@mindspaymails.com |  | Thu, 31 Mar 2011 23:19:52 -0500 ", app.processFile(br));
    }

    @Test
    public void getRootPath() {
        App app = new App();
        app.setRootPath(ROOT_PATH);
        Assert.assertEquals(ROOT_PATH, app.getRootPath());
    }

    @Test
    public void setRootPath() {
    }
}