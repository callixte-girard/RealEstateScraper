package myJavaClasses;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ReadWriteFile
{


    public static ArrayList<File> getFolderContentsAsFileArray(String full_path)
    {
        File folder = new File(full_path);
        File[] files = folder.listFiles();
        ArrayList<?> out = Misc.arrayToArrayList(files);
        return (ArrayList<File>)out ;
    }

    public static ArrayList<String> getFileContentAsStringArray(String full_path)
    {
        try {
            BufferedReader br = ReadWriteFile.outputReader(full_path);

            ArrayList<String> out = new ArrayList<>();
            String line;

            while((line = br.readLine()) != null){
                out.add(line);
            }
            return out;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static boolean createFolderIfNotExists(String pathToInspect)
    {
        File f = new File(pathToInspect);
        if (f.exists() && f.isDirectory()) f.delete();
        else if (! f.isFile()) f.mkdir();
        // inspect before returning
        return f.exists() && f.isDirectory();
    }


    public static BufferedWriter outputWriter(String path)
    {
        try
        {
            File f = new File(path);

            if (!f.exists()) f.createNewFile();

            FileWriter fw = new FileWriter(f);
            return new BufferedWriter(fw);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null ;
        }
    }

    
    public static BufferedReader outputReader(String path) throws FileNotFoundException
    {
        File f = new File(path);

        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);

        return br ;
    }


    public static void writeLineCSV(BufferedWriter bw, List<String> attrsToWrite, String separator, boolean endOfLine)
    {
        try
        {
            int attr_index = 0;
            for (String attr : attrsToWrite)
            {
                if (attr != null) bw.write(attr);
                else bw.write(Misc.NO_DATA);

                attr_index ++;
                if (attr_index < attrsToWrite.size()) {
                    bw.write(separator);
                } else if (! endOfLine) {
                    bw.write(separator);
                }
            }
            if (endOfLine) bw.newLine();

        } catch (IOException e) { Disp.exc(e); }
    }

}
