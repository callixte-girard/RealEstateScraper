package handleVPN;

import myJavaClasses.Disp;
import myJavaClasses.ShellWrapper;

import java.time.LocalDateTime;
import java.util.List;

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

    private static int changeIPCounter = 0;

    private static String buildShellCommandGet(String type) { return "piactl get " + type; }
    private static String buildShellCommandSet(String type) { return "piactl set " + type; }
    private static String buildShellCommandMonitor(String type) { return "piactl monitor " + type; }
    private static String buildShellCommandConnect() { return "piactl connect"; }
    private static String buildShellCommandDisconnect() { return "piactl disconnect"; }

    private static void reconnect() {
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

    public static String getCurrentRegion() { return ShellWrapper.execute(buildShellCommandGet(TYPE_CURRENT_REGION)).get(0); }
    public static String getCurrentIP() { return ShellWrapper.execute(buildShellCommandGet(TYPE_CURRENT_IP)).get(0); }

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
        reconnect();
        String newIP = ShellWrapper.execute(buildShellCommandGet(TYPE_CURRENT_IP)).get(0);
        if (oldIP.equals(newIP)) {
            Disp.anyType("Same IP obtained. Trying again...");
            return changeIP();
        }
//        Disp.anyType("IP change : [ " + newIP + " ] <— [ " + oldIP + " ]");
        return newIP;
    }

    public static String changeRegion()
    {
        String oldRegion = ShellWrapper.execute(buildShellCommandGet(TYPE_CURRENT_REGION)).get(0);
        //////
        String newRegion = ShellWrapper.execute(buildShellCommandGet(TYPE_CURRENT_REGION)).get(0);
        if (oldRegion.equals(newRegion)) return changeIP();
        return newRegion;
    }


}
