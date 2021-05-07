package myJavaClasses;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ShellWrapper {
    private static final int duration = 2500;

    public static ArrayList<String> execute(String command)
    {
        try {
            Runtime runtime = Runtime.getRuntime();
            Process p = runtime.exec(command);

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            ArrayList<String> returns = new ArrayList<>();
            while ((line = br.readLine()) != null) {
//                Disp.anyType(line);
                returns.add(line);
            }

            if (! returns.isEmpty()) return returns;
            else return null;

        } catch (IOException ioEx) {
            return null;
        }
    }

    public static ArrayList<String> appleScript(String command)
    {
        try {
            Runtime runtime = Runtime.getRuntime();
            String[] args = { "osascript", "-e", command };
            Process p = runtime.exec(args);

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            ArrayList<String> returns = new ArrayList<>();
            while ((line = br.readLine()) != null) {
//                Disp.anyType(line);
                returns.add(line);
            }

            if (! returns.isEmpty()) return returns;
            else return null;

        } catch (IOException ioException) {
            return null;
        }
    }

    public static void appleScriptBeep()
    {
        /*for (int i=0; i<5; i++) {
            ShellWrapper.appleScript("beep");
            ShellWrapper.appleScript("delay " + duration);
        }*/
    }
}
