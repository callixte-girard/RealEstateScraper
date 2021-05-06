package handleVPN;

import myJavaClasses.Disp;
import myJavaClasses.ShellWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    private static List<String> alreadyUsedIPs = new ArrayList<>();
    private static List<String> alreadyUsedRegions = new ArrayList<>();

    private static String buildShellCommandGet(String type) { return "piactl get " + type; }
    private static String buildShellCommandSet(String type, String value) { return "piactl set " + type + " " + value; }
    private static String buildShellCommandMonitor(String type) { return "piactl monitor " + type; }
    private static String buildShellCommandConnect() { return "piactl connect"; }
    private static String buildShellCommandDisconnect() { return "piactl disconnect"; }

    public static void reconnect() {
        String status = "";
        ShellWrapper.execute(buildShellCommandDisconnect());
        while (! status.equals(STATUS_DISCONNECTED)) { status = ShellWrapper.execute(buildShellCommandGet(TYPE_CURRENT_STATE)).get(0); }
        ShellWrapper.execute(buildShellCommandConnect());
        while (! status.equals(STATUS_CONNECTED)) { status = ShellWrapper.execute(buildShellCommandGet(TYPE_CURRENT_STATE)).get(0); }
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

    public static boolean isCurrentRegionSaturated() {
        return false;
    }


    public static String changeIP()
    {
        String oldIP = ShellWrapper.execute(buildShellCommandGet(TYPE_CURRENT_IP)).get(0);
        alreadyUsedIPs.add(oldIP);
        reconnect();
        String newIP = ShellWrapper.execute(buildShellCommandGet(TYPE_CURRENT_IP)).get(0);

        if (alreadyUsedIPs.contains(newIP)) {
            Disp.anyType("The obtained IP has already been used before. Trying another one...");
            return changeIP();
        } else {
//            Disp.anyType("Old IP : " + oldIP);
//            Disp.anyType("IP change : [ " + newIP + " ] <— [ " + oldIP + " ]");
//            Disp.anyType("IP change : [ " + oldIP + " ] —> [ " + newIP + " ]");
            Disp.anyType("A new IP has been obtained :)");
            return newIP;
        }
    }

    public static String changeRegion()
    {
        String oldRegion = ShellWrapper.execute(buildShellCommandGet(TYPE_CURRENT_REGION)).get(0);
        alreadyUsedRegions.add(oldRegion);
        List<String> allRegions = ShellWrapper.execute(buildShellCommandGet(TYPE_ALL_REGIONS));
        int randomIndex = new Random().nextInt(allRegions.size());
        String newRegion = allRegions.get(randomIndex);
        ShellWrapper.execute(buildShellCommandSet(TYPE_CURRENT_REGION, newRegion));

        if (alreadyUsedRegions.contains(newRegion)) {
            Disp.anyType("The obtained region has already been used. Trying another one...");
            return changeRegion();
        } else {
            return newRegion;
        }
    }


}