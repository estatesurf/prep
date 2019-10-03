package com.mlindner;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class App {

    public static String FROM = "From:";
    public static String SUBJECT = "Subject:";
    public static String DATE = "Date:";
    public static String OUTPUT_FILENAME = "results.txt";
    public static String INPUT_FILENAME = "sampleEmails.tar.gz";
    public static String EMAIL_WORKING_DIR = "emailsDir";

    //The "expected" structure of tar files should really be determined at run time as archive structures may change
    public static String EXPECTED_ARCHIVE_STRUCTURE = "\\sampleEmails\\smallset";

    public static void main(String[] args) {
        //Pick a dir to stage the working area for the email archive
        App app = new App(EMAIL_WORKING_DIR);

        //open up the email archive into the working area dir
        File tarball = new File(INPUT_FILENAME);
        try {
            String newTarFile = app.decompressGzip(tarball, app.getRootPath());
            File tarFile = new File(newTarFile);
            app.unTarFile(tarFile, app.getRootPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //create/replace the results file
        setupOutputFile(OUTPUT_FILENAME);

        //parse each email from the email archive
        app.iterateOverFiles();

        //cleanup and exit
        closeOutputFile();
    }

    private String rootPath;
    private static FileWriter writer;

    App() {

    }

    App(String rootPath) {
        setRootPath(rootPath);
    }


    public void iterateOverFiles() {
        //assemble path to where the actual email files will be.  
        String targetFile = EMAIL_WORKING_DIR + EXPECTED_ARCHIVE_STRUCTURE;
        File f = new File(targetFile);
        BufferedReader br = null;

        File[] files = f.listFiles();

        //iterate over and process each email file
        for(File file : files) {

            if(file.isFile()) {
                try {
                    br = new BufferedReader(new FileReader(file));
                    writeLineToOutputFile(file.getName() + " | " + processFile(br));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                finally {
                    if(br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public String processFile(BufferedReader input) {

        String from = "", subject = "", date = "";

        //Setup search patterns
        Pattern fromPattern = Pattern.compile("From: .*<(.*)>");
        Pattern subjectPattern = Pattern.compile("Subject: (.*)");
        Pattern datePattern = Pattern.compile("Date: (.*)");

        try {
            String line;
            boolean foundEverything = false;

            //look for patterns until EOF reached
            //NOTE: This code assumes only one instance of each "pattern" per file exists
            while(!foundEverything && ((line = input.readLine()) != null)) {

                Matcher m = fromPattern.matcher(line);
                if(m.find()) {
                    from = m.group(1);
                }
                m = subjectPattern.matcher(line);
                if(m.find()) {
                    subject = m.group(1);
                }
                m = datePattern.matcher(line);
                if(m.find()) {
                    date = m.group(1);
                }
                if (!from.isEmpty() && !subject.isEmpty() && !date.isEmpty())
                {
                    foundEverything = true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //return the result of pattern search.
        return String.format("%s | %s | %s ", from, subject, date);
    }


    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    private String decompressGzip(File input, String targetDir) throws IOException {
        String[] filenameParts = input.getName().split("\\.gz");
        String filenamePrefix = filenameParts[0];
        String slash = System.getProperty("file.separator");
        String targetFile = filenamePrefix;
        File output = new File(targetFile);
        try (GZIPInputStream in = new GZIPInputStream(new FileInputStream(input))) {
            try (FileOutputStream out = new FileOutputStream(output)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }
        }
        return targetFile;
    }

    private void unTarFile(File tarFile, String targetDir) throws IOException {
        String[] filenameParts = tarFile.getName().split("\\.tar");
        String filenamePrefix = filenameParts[0];
        String slash = System.getProperty("file.separator");
        File output = new File(targetDir + slash + filenamePrefix);
        FileInputStream fis = new FileInputStream(tarFile);
        TarArchiveInputStream tis = new TarArchiveInputStream(fis);
        TarArchiveEntry tarEntry = null;

        while ((tarEntry = tis.getNextTarEntry()) != null) {
            File outputFile = new File(output + File.separator + tarEntry.getName());

            if (tarEntry.isDirectory()) {

                System.out.println("email Directory ---- "
                        + outputFile.getAbsolutePath());
                if (!outputFile.exists()) {
                    outputFile.mkdirs();
                }
            } else {
                System.out.println("email File created ---- " + outputFile.getAbsolutePath());
                outputFile.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(outputFile);
                IOUtils.copy(tis, fos);
                fos.close();
            }
        }
        tis.close();
    }

   private static void setupOutputFile(String filename) {
       try {
           writer = new FileWriter(filename, false);
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

   private static void writeLineToOutputFile(String line) {
       System.out.println("Writing to " + OUTPUT_FILENAME + " ==> " + line + "\n"); 
       try {
           writer.write(line);
           writer.write("\r\n");   // write new line
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

   private static void closeOutputFile() {
       try {
           writer.close();
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

}
