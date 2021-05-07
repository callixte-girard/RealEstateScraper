package handleVPN;

import myJavaClasses.Disp;
import myJavaClasses.ShellWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


// Everything is done here now. Every other classes in this package handleVPN are obsolete.
public class PIA
{
    /*enum TYPES {
        TYPE_ALL_REGIONS,
        TYPE_CURRENT_REGION,
        TYPE_CURRENT_STATE,
        TYPE_CURRENT_IP,
    }*/
    public static final String TYPE_ALL_REGIONS = "regions";
    public static final String TYPE_CURRENT_REGION = "region";
    public static final String TYPE_CURRENT_STATE = "connectionstate";
    public static final String TYPE_CURRENT_IP = "vpnip";

    public static final String STATUS_CONNECTED = "Connected";
    public static final String STATUS_DISCONNECTED = "Disconnected";

    public static final String NO_IP = "Unknown";
    public static final int maxNbIpChanges = 5;
    public static final boolean limitIpChangesPerRegion = true;
    public static final boolean chooseNextRegionRandomly = false;
    public static final boolean fullDebug = false;

    private static int ipChangeCounter = 0;
    private static List<String> alreadyUsedIPs = new ArrayList<>();
    private static List<String> alreadyUsedRegions = new ArrayList<>();

    private static String buildShellCommandGet(String type) { return "piactl get " + type; }
    private static String buildShellCommandSet(String type, String value) { return "piactl set " + type + " " + value; }
//    private static String buildShellCommandMonitor(String type) { return "piactl monitor " + type; } // doesn't work as expected in Java, only in Shell
    private static String buildShellCommandConnect() { return "piactl connect"; }
    private static String buildShellCommandDisconnect() { return "piactl disconnect"; }


    public static void reconnect() {
        String status = "";
        String ip = NO_IP;
        ShellWrapper.execute(buildShellCommandDisconnect());
        while (! status.equals(STATUS_DISCONNECTED)) {
            status = ShellWrapper.execute(buildShellCommandGet(TYPE_CURRENT_STATE)).get(0);
            if (fullDebug) Disp.anyType(status);
        }
        ShellWrapper.execute(buildShellCommandConnect());
        while (! status.equals(STATUS_CONNECTED) || ip.equals(NO_IP)) {
            status = ShellWrapper.execute(buildShellCommandGet(TYPE_CURRENT_STATE)).get(0);
            ip = ShellWrapper.execute(buildShellCommandGet(TYPE_CURRENT_IP)).get(0);
            if (fullDebug) Disp.anyType(status);
        }
    }

//    private static List<Region> saturatedRegions = new ArrayList<>();

//    public static String getRegions() { return buildShellGetCommand(TYPE_ALL_REGIONS); }
//    public static String getNextRegion() { return null; }
//    public static String getNextUnsaturatedRegion() { return null; }

    public static String getCurrentRegion() {
        return ShellWrapper.execute(buildShellCommandGet(TYPE_CURRENT_REGION)).get(0);
    }
    public static String getCurrentIP() {
        return ShellWrapper.execute(buildShellCommandGet(TYPE_CURRENT_IP)).get(0);
    }
    public static boolean isConnected() {
        return ShellWrapper.execute(buildShellCommandGet(TYPE_CURRENT_STATE)).get(0).equals(STATUS_CONNECTED);
    }

    public static List<String> getAlreadyUsedIPs() {
        return alreadyUsedIPs;
    }
    public static List<String> getAlreadyUsedRegions() {
        return alreadyUsedIPs;
    }

    public static void displayCurrentRegionAndIP() {
        // get current region and ip
        Disp.shortMsgStar(
                "Currently connected on [ " + getCurrentRegion()
                        + " ] on IP [ " + getCurrentIP() + " ]", true
        );
    }


    public static boolean isCurrentIpSaturated() {
        return true;
    }

    public static boolean isCurrentRegionSaturated(int nbIPChanges) {
        return nbIPChanges > maxNbIpChanges;
    }


    public static String changeIP()
    {
        String oldIP = ShellWrapper.execute(buildShellCommandGet(TYPE_CURRENT_IP)).get(0);
        alreadyUsedIPs.add(oldIP);
        reconnect();
        String newIP = ShellWrapper.execute(buildShellCommandGet(TYPE_CURRENT_IP)).get(0);

        if (alreadyUsedIPs.contains(newIP)) {
            Disp.anyType(">>> The obtained IP [ " + newIP + " ] has already been used before. Trying another one...");
            ipChangeCounter ++;
//            Disp.exc("IpChangeCounter : " + ipChangeCounter);
            if (ipChangeCounter >= maxNbIpChanges) {
                if (limitIpChangesPerRegion) changeRegion();
                ShellWrapper.appleScriptBeep();
            }
            return changeIP();
        } else {
//            Disp.anyType("Old IP : " + oldIP);
//            Disp.anyType("IP change : [ " + newIP + " ] <— [ " + oldIP + " ]");
//            Disp.anyType("IP change : [ " + oldIP + " ] —> [ " + newIP + " ]");
            Disp.anyType(">>> A new IP has been obtained : [ " + newIP + " ]");
            ipChangeCounter = 0;
            return newIP;
        }
    }

    public static String changeRegion() // this one takes a random int
    {
        ipChangeCounter = 0;

        String oldRegion = ShellWrapper.execute(buildShellCommandGet(TYPE_CURRENT_REGION)).get(0);
        alreadyUsedRegions.add(oldRegion);
        String newRegion = getNextRegion(chooseNextRegionRandomly);
        ShellWrapper.execute(buildShellCommandSet(TYPE_CURRENT_REGION, newRegion));

        if (alreadyUsedRegions.contains(newRegion)) {
            Disp.anyType(">>> The obtained region has already been used. Trying another one...");
            return changeRegion();
        } else {
            return newRegion;
        }
    }

    private static String getNextRegion(boolean random)
    {
        List<String> allRegions = ShellWrapper.execute(buildShellCommandGet(TYPE_ALL_REGIONS));
        String nextRegion = getCurrentRegion();

        if (random) {
            int randomIndex = -1;
            Random r = new Random();
            while (randomIndex <= 1) {
                randomIndex = r.nextInt(allRegions.size());
            }
            nextRegion = allRegions.get(randomIndex);

        } else {
            for (int i=1; i<allRegions.size(); i++) { // avoid i=0 because it's auto mode
                String region = allRegions.get(i);
                if (region.equals(getCurrentRegion())) {
                    if (i == allRegions.size()-1)
                        nextRegion = allRegions.get(1);
                    else
                        nextRegion = allRegions.get(i+1);
                    break;
                }
            }
        }

        if (alreadyUsedRegions.contains(nextRegion)) nextRegion = getNextRegion(random);
        return nextRegion;
    }
}
