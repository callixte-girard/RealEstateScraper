package parseImmo;

import myJavaClasses.*;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import parseCity.City;
import parseCity.ExtractLocalInfos;
import parseCity.ParseCity;
import parseCity.Prices;
import parseDepartment.Department;
import parseDepartment.ParseDepartment;
import handleVPN.*;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Main {
    private static final String projectName = "RealEstateScraper" ;
    public static final String NO_DATA = "null";

    // urls
    private static final String url_main = "https://www.meilleursagents.com" ;
    private static final String url_sub = "/prix-immobilier" ;

    // folder names
    private static final String save_folder = "data_saved/";
    private static final String output_folder = "data_output/";
    // paths
    private static final String root_path = "/Users/c/Documents/Local Code/—Java/";
    private static final String save_path = root_path + projectName + "/" + save_folder;
    private static final String output_path = root_path + projectName + "/" + output_folder;
    // extensions
    public static final String extension_save = ".immo";
    public static final String extension_csv = ".csv";
    // filenames
    public static final String filename_cities_urls = "urls";
    public static final String filename_cities_list = "cities";
    public static final String filename_vpn_state = "regions";

    // program parameters
    public static String[] filterDepartments = {
            // IDF
            "75", // Paris
            "92", "93", "94", // petite couronne
            "77", "78", "91", "95", // grande couronne
//            "60", // Oise : Creil & co
    };
    public static boolean onlyExport = false;


    public static void main_(String[] args) throws Exception
    {
//        HandleVPN.initAllRegions();
        // check if VPN is connected or not.

        // if not, connect it.
        ////////
        // else, get region and ip is it connected to.
//        HandleVPN.displayCurrentRegionAndIP();
//        HandleVPN.displayGlobalState();
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) throws Exception
    {
        Disp.shortMsgLine(projectName, false);
        double start = System.currentTimeMillis(); // start counter

        SaveManager.setSavePath(save_path);
        ReadWriteFile.createFolderIfNotExists(save_path);
        ReadWriteFile.createFolderIfNotExists(output_path);

        if (! onlyExport) {
            HandleVPN.initAllRegions();
            HandleVPN.displayCurrentRegionAndIP();
        }

        // restart infinitely until everything is scraped
        try {
            ArrayList<City> allCities = scrapeAllFrenchCities(); // includes writing cities to output_data/filename.csv when all is finished

            // finally, write cities as .csv to be exported to Excel
            writeCitiesAsCSV(filename_cities_list + extension_csv , allCities , true);

        } catch (Exception | UncheckedIOException e1) {
            Disp.exc("Exception level 1 : [ v" + e1 + " ]");
//            Disp.exc(e.getCause() + " | " + e.getMessage());
            e1.printStackTrace();
            Disp.star();

            // now let's start again and again and again ... FOREVAH ;)
            /*try {
//                main(args);
            } catch (Exception | UncheckedIOException e2) {
//                main(args);

                Disp.exc("Exception level 2 : [ " + e2 + " ]");
//                Disp.exc(e1.getCause() + " | " + e1.getMessage());
                e2.printStackTrace();
                Disp.star();
            }*/
        }

        double end = System.currentTimeMillis(); // end counter
        System.out.println("Total time : " + (end-start) + " ms");
    }


    private static ArrayList<City> scrapeAllFrenchCities() throws Exception
    {
        // little fix for encoding problems
//        EncodingCorrecter.refreshEncodingAtStartup("UTF-8");

        // 1. get cities urls from disk or from the net
        ArrayList<String> urls_cities;
        Disp.anyTypeThenLine(">>> Getting all cities urls from saved content.");

        // load list cities urls
        urls_cities = (ArrayList<String>) SaveManager.objectLoad
                (filename_cities_urls + extension_save , true);

        if (urls_cities == null) { // which means : if save file does not exist
            Disp.anyTypeThenStar(">>> Now scraping all cities urls from all departments.");

            // get urls of all 96 departments
            try {
                Document main_page = ParseHtml.fetchHtmlAsDocumentFromUrl(url_main + url_sub);
                ArrayList<String> urls_dpts = ParseDepartment.getUrls(main_page);

                // add all cities urls for each dpt in main list
                urls_cities = new ArrayList<>();
                for (String url_dpt : urls_dpts) {
                    // filter by department number
                    if (Arrays.asList(filterDepartments).isEmpty() ||
                            Arrays.asList(filterDepartments).contains(ParseDepartment.getDptNumberFromUrl(url_dpt))
                    ) {
                        String url_to_parse = url_main + url_dpt; // assemble partial url with main part
                        Department dpt = ParseDepartment.parseCities(url_to_parse);

                        urls_cities.addAll(dpt.getUrlsCities());
//                    Disp.anyTypeThenStar(dpt.getUrlsCities().size()); // just to check we never get null
                    }
                }
            } catch (Exception e) {
                Disp.exc("Can't download departments list containing urls to cities... Program can't start.");
            }

            // now write it to a file so we can recover it later
            SaveManager.objectSave(filename_cities_urls + extension_save , urls_cities);
        }

        // 2. parse cities from disk or from the net
        // parse main city list from urls
        ArrayList<City> cities;
        Disp.anyTypeThenLine(">>> Getting all cities from saved content.");

        // load list cities for doing only the ones that haven't been already done
        cities = (ArrayList<City>) SaveManager.objectLoad
                (filename_cities_list + extension_save , true);

        if (cities == null) { // which means : if save does not exist
            cities = new ArrayList<>();
            Disp.anyType(">>> Now scraping all cities from all departments.");
        } else {
            int actual_progress = cities.size();
            int total_cities = urls_cities.size();
            int remaining_cities = total_cities - actual_progress;
            Disp.anyType(">>> " + actual_progress + " / " + total_cities + " cities are already parsed.");
            Disp.anyType(">>> Will now parse " + remaining_cities + " remaining cities.");
        }
//        HandleVPN.displayGlobalState();

        Disp.htag(); Disp.htag(); Disp.htag();
        int nbRetries_thisSess = 0;
        int nbDone = 0;
//        int nbIPChanges = 0;
        boolean needsSave = false;

        // the loop ;)
        for (int index_city=1 ; index_city < urls_cities.size() ; index_city ++)
        {
            String url_city = urls_cities.get(index_city);
            String url_to_parse = url_main + url_city; // assemble partial url with main part

            if ( !City.exists(url_to_parse, cities) ) { // try to download it until it's done.

                try {
                    City city = ParseCity.parse(url_to_parse);
//                    Disp.anyType(city);
                    Disp.anyType(
                            ">>> City n°" + index_city + " : [ "
                                    + city.getName() + " ("
                                    + city.getPostalCodeAsDptNumber() + ") ] ——> done."
                    );
                    cities.add(city);

                    // success : resets try counter and increments done counter
                    nbRetries_thisSess = 0;
                    nbDone++;
                    // puts the trigger on after a city has been scraped since last save
                    needsSave = true;

                    // then, display progress for cities BUT ONLY IF IT HAS NOT BEEN ALREADY PARSED
                    Disp.progress("Scraped cities", nbDone, urls_cities.size());

                } catch (Exception | UncheckedIOException e) {
                    // here is da list of Exceptions that happened :
                    /*
                    - ArrayIndexOutOfBoundsException
                    - SocketTimeoutException
                    - SSLHandshakeException
                    - ConnectionException
                    - ConnectException
                    - UncheckedIOException
                    */

                    // failure : increments try counter
                    nbRetries_thisSess ++;

                    // disp err msg
                    Disp.exc("Quota maximum reached for [" + IP.getCurrent().getAddress() + "] -- Let's try again with another one ;)");
//                    Disp.progress("consecutive max retries for " + IP.getCurrent().getAddress(), nbRetries_thisSess, Region.getCurrent().getGlobalSaturationIndicator());
//                    Disp.exc(e.getCause() + " | " + e.getMessage());
//                    e.printStackTrace();

                    // save actual progress only first time, only if changes have been made
                    if (nbRetries_thisSess == 1)
                    {
                        // mark current IP as used & update last try date
                        IP.getCurrent().setTimesUsed(IP.getCurrent().getTimesUsed() + 1);
                        IP.getCurrent().setLastTry(LocalDateTime.now());
                        SaveManager.objectSave(filename_vpn_state + extension_save, Region.getRegions());

                        if (needsSave)
                        {
                            Disp.star();
                            SaveManager.objectSave(filename_cities_list + extension_save, cities);
                            needsSave = false; // puts the trigger off until a new city gets scraped
//                        nbIPChanges ++; // increment for further count
                        }
                    }


                    // change IP as much as possible...
                    if (! Region.getCurrent().isSaturated(true)) {
                        // first try to fix the problem by changing IP in the same region.
                        IP.handleChange();

                        // is this IP already blocked ?
                        if (false) // #to-complete
                        {
                            while (IP.getCurrent().isBlocked()) {
                                Disp.anyType(">>> [" + IP.getCurrent().getAddress() + "] has already failed more than " + IP.nb_max_each_ip + " times —> trying another one");
                                IP.handleChange();
                            }
                        }

                    // ...when limit reached go to next region
                    } else {
                        // then try to switch to the next region
                        Region.handleChange();
                        // resets counters : not useful anymore
                        nbRetries_thisSess = 0;
//                        nbIPChanges = 0;
                    }
                    // show current VPN state
                    HandleVPN.displayCurrentRegionAndIP();
//                    HandleVPN.displayGlobalState();

                    index_city--; // stay on that level. Must be parsed
                }

//                Disp.separatorStar();
//                Disp.separatorLine();
                Disp.line();

            } else {
                // success : resets try counter and increment done counter
                nbRetries_thisSess = 0;
                nbDone++;
            }

        } // end of main for loop

        // save actual progress when download has finished too.
//        if (cities.size() == urls_cities.size() - 2)
        if (needsSave)
            SaveManager.objectSave(filename_cities_list + extension_save, cities);

        return cities;
    }

    //////////////////////////////////////////////////////////



    private static void writeCitiesAsCSV(String filename, ArrayList<City> citiesToWrite, boolean with_headers)
    {
        String save_to = output_path + filename ;
        System.out.println(save_to);

        /* TODO:
            - export all data in series with clean labels as they appear on the website
            - clean data values so that Excel recognises it without any further processing
        */

        try {
            BufferedWriter bw = ReadWriteFile.outputWriter(save_to);

            // I) makes header first
            if (with_headers) {
                List<String> headers = new ArrayList<>();
                String[] basicHeaders = {
                        // main infos
//                        "url",
                        City.label_dptNumber,
                        City.label_name,
                        // prices
                        Prices.label_pricesMeanBuyFlat,
                        Prices.label_pricesMeanRentFlat,
                        Prices.label_pricesTrends1y,
                        Prices.label_pricesTrends2y,
                        Prices.label_pricesTrends5y,
                };
                String[] localInfosHeaders = {
                        // population
                        ExtractLocalInfos.label01_population,
                        ExtractLocalInfos.label02_rateDemographicGrowth,
                        ExtractLocalInfos.label03_medianAge,
                        ExtractLocalInfos.label04_lessThan25yo,
                        ExtractLocalInfos.label05_moreThan25yo,
                        ExtractLocalInfos.label06_popDensity,
                        ExtractLocalInfos.label07_area,
                        // homes
                        ExtractLocalInfos.label08_totalNbHomes,
                        ExtractLocalInfos.label09_rateMainHomes,
                        ExtractLocalInfos.label10_rateSecondaryHomes,
                        ExtractLocalInfos.label11_rateVacantHomes,
                        ExtractLocalInfos.label12_rateSocialHomes,
                        ExtractLocalInfos.label13_rateMainHomeOwner,
                        ExtractLocalInfos.label14_rateMainHomeTenant,
                        ExtractLocalInfos.label15_rateMainHome1Room,
                        ExtractLocalInfos.label16_rateMainHome2Rooms,
                        ExtractLocalInfos.label17_rateMainHome3Rooms,
                        ExtractLocalInfos.label18_rateMainHome4Rooms,
                        ExtractLocalInfos.label19_rateMainHome5RoomsMore,
                        // revenue & employment
                        ExtractLocalInfos.label20_medianAnnualRevenue,
                        ExtractLocalInfos.label21_employmentRate15To64,
                        ExtractLocalInfos.label22_employmentRateEvol,
                        ExtractLocalInfos.label23_unemploymentRate15To64,
                        ExtractLocalInfos.label24_unemploymentRateEvol,
                };
                headers.addAll(Arrays.asList(basicHeaders));
                headers.addAll(Arrays.asList(localInfosHeaders));
                writeLineCSV(bw, headers, true);
            }

            // II) then writes data cell by cell
            for (City city : citiesToWrite)
            {
//                Disp.anyTypeThenLine(city);

                String[] basicAttrs = {
                        // main infos
//                        city.getUrl(),
                        city.getPostalCodeAsDptNumber(),
                        city.getName(),
                };

                // II)1) gets values
                // prices
                String pricesMeanBuyAppt = city.getPricesBuyAppt().getMeanAsAmount();
                String pricesMeanRentAppt = city.getPricesRentAppt().getMeanAsAmount();
//                String pricesMeanBuyHouse = city.getPricesBuyHouse().getMeanAsAmount(); // NOTE: we'll just ignore houses for now and limit ourselves to apartments
                String pricesTrends1y = city.getTrends(1);
                String pricesTrends2y = city.getTrends(2);
                String pricesTrends5y = city.getTrends(5);

                String[] pricesAttrs = { pricesMeanBuyAppt, pricesMeanRentAppt, pricesTrends1y, pricesTrends2y, pricesTrends5y };

                // population
                String population = city.getLocalPop().get(ExtractLocalInfos.label01_population);
                String rateDemograhicGrowth = city.getLocalPop().get(ExtractLocalInfos.label02_rateDemographicGrowth);
                String medianAge = city.getLocalPop().get(ExtractLocalInfos.label03_medianAge);
                String lessThan25yo = city.getLocalPop().get(ExtractLocalInfos.label04_lessThan25yo);
                String moreThan25yo = city.getLocalPop().get(ExtractLocalInfos.label05_moreThan25yo);
                String popDensity = city.getLocalPop().get(ExtractLocalInfos.label06_popDensity);
                String area = city.getLocalPop().get(ExtractLocalInfos.label07_area);
                // homes
                String totalNbHomes = city.getLocalHomes().get(ExtractLocalInfos.label08_totalNbHomes);
                String rateMainHomes = city.getLocalHomes().get(ExtractLocalInfos.label09_rateMainHomes);
                String rateSecondaryHomes = city.getLocalHomes().get(ExtractLocalInfos.label10_rateSecondaryHomes);
                String rateVacantHomes = city.getLocalHomes().get(ExtractLocalInfos.label11_rateVacantHomes);
                String rateSocialHomes = city.getLocalHomes().get(ExtractLocalInfos.label12_rateSocialHomes);
                String rateMainHomeOwner = city.getLocalHomes().get(ExtractLocalInfos.label13_rateMainHomeOwner);
                String rateMainHomeTenant = city.getLocalHomes().get(ExtractLocalInfos.label14_rateMainHomeTenant);
                String rateMainHome1Room = city.getLocalHomes().get(ExtractLocalInfos.label15_rateMainHome1Room);
                String rateMainHome2Rooms = city.getLocalHomes().get(ExtractLocalInfos.label16_rateMainHome2Rooms);
                String rateMainHome3Rooms = city.getLocalHomes().get(ExtractLocalInfos.label17_rateMainHome3Rooms);
                String rateMainHome4Rooms = city.getLocalHomes().get(ExtractLocalInfos.label18_rateMainHome4Rooms);
                String rateMainHome5RoomsMore = city.getLocalHomes().get(ExtractLocalInfos.label19_rateMainHome5RoomsMore);
                // revenue / employment
                String medianAnnualRevenue = city.getLocalRevenueEmpl().get(ExtractLocalInfos.label20_medianAnnualRevenue);
                String employmentRate15To64 = city.getLocalRevenueEmpl().get(ExtractLocalInfos.label21_employmentRate15To64);
                String employmentRateEvol = city.getLocalRevenueEmpl().get(ExtractLocalInfos.label22_employmentRateEvol);
                String unemploymentRate15To64 = city.getLocalRevenueEmpl().get(ExtractLocalInfos.label23_unemploymentRate15To64);
                String unemploymentRateEvol = city.getLocalRevenueEmpl().get(ExtractLocalInfos.label24_unemploymentRateEvol);

                // II)2) treats values that need lil formatting before exporting to excel
                // population
                try { population = Misc.valueNormalise(population.replace(" habitants", "")); } catch (NullPointerException e) { population = null; }
                try { rateDemograhicGrowth = Misc.valueNormalise(rateDemograhicGrowth); } catch (NullPointerException e) { rateDemograhicGrowth = null; }
                try { medianAge = medianAge.replace(" ans", ""); } catch (NullPointerException e) { medianAge = null; }
                try { lessThan25yo = lessThan25yo.replace(" ans", ""); } catch (NullPointerException e) { lessThan25yo = null; }
                try { moreThan25yo = moreThan25yo.replace(" ans", ""); } catch (NullPointerException e) { moreThan25yo = null; }
                try { popDensity = Misc.valueNormalise(popDensity.replace(" hab. / km²", "")); } catch (NullPointerException e) { popDensity = null; }
                try { area = Misc.valueNormalise(area.replace(" hab. / km²", "")); } catch (NullPointerException e) { area = null; }
                // homes
                try { totalNbHomes = Misc.valueNormalise(totalNbHomes.replace(" logements", "")); } catch (NullPointerException e) { totalNbHomes = null; }
                try { rateMainHomes = Misc.valueNormalise(rateMainHomes); } catch (NullPointerException e) { rateMainHomes = null; }
                try { rateSecondaryHomes = Misc.valueNormalise(rateSecondaryHomes); } catch (NullPointerException e) { rateSecondaryHomes = null; }
                try { rateVacantHomes = Misc.valueNormalise(rateVacantHomes); } catch (NullPointerException e) { rateVacantHomes = null; }
                try { rateSocialHomes = Misc.valueNormalise(rateSocialHomes); } catch (NullPointerException e) { rateSocialHomes = null; }
                try { rateMainHomeOwner = Misc.valueNormalise(rateMainHomeOwner); } catch (NullPointerException e) { rateMainHomeOwner = null; }
                try { rateMainHomeTenant = Misc.valueNormalise(rateMainHomeTenant); } catch (NullPointerException e) { rateMainHomeTenant = null; }
                try { rateMainHome1Room = Misc.valueNormalise(rateMainHome1Room); } catch (NullPointerException e) { rateMainHome1Room = null; }
                try { rateMainHome2Rooms = Misc.valueNormalise(rateMainHome2Rooms); } catch (NullPointerException e) { rateMainHome2Rooms = null; }
                try { rateMainHome3Rooms = Misc.valueNormalise(rateMainHome3Rooms); } catch (NullPointerException e) { rateMainHome3Rooms = null; }
                try { rateMainHome4Rooms = Misc.valueNormalise(rateMainHome4Rooms); } catch (NullPointerException e) { rateMainHome4Rooms = null; }
                try { rateMainHome5RoomsMore = Misc.valueNormalise(rateMainHome5RoomsMore); } catch (NullPointerException e) { rateMainHome5RoomsMore = null; }
                // revenue / employment
                try { medianAnnualRevenue = Misc.valueNormalise(Misc.removeNonNumericalCharsFromString(medianAnnualRevenue)); } catch (NullPointerException e) { medianAnnualRevenue = null; }
                try { employmentRate15To64 = employmentRate15To64.replace(" pt.", ""); } catch (NullPointerException e) { employmentRate15To64 = null; }
                try { employmentRateEvol = employmentRateEvol.replace(" pt.", ""); } catch (NullPointerException e) { employmentRateEvol = null; }
                try { unemploymentRate15To64 = Misc.valueNormalise(unemploymentRate15To64); } catch (NullPointerException e) { unemploymentRate15To64 = null; }
                try { unemploymentRateEvol = unemploymentRateEvol.replace(" pt.", ""); } catch (NullPointerException e) { unemploymentRateEvol = null; }

                // II)3) inserts values into array for exporting
                String[] localAttrs = {
                        // population
                        population,
                        rateDemograhicGrowth,
                        medianAge,
                        lessThan25yo,
                        moreThan25yo,
                        popDensity,
                        area,
                        // homes
                        totalNbHomes,
                        rateMainHomes,
                        rateSecondaryHomes,
                        rateVacantHomes,
                        rateSocialHomes,
                        rateMainHomeOwner,
                        rateMainHomeTenant,
                        rateMainHome1Room,
                        rateMainHome2Rooms,
                        rateMainHome3Rooms,
                        rateMainHome4Rooms,
                        rateMainHome5RoomsMore,
                        // revenue / employment
                        medianAnnualRevenue,
                        employmentRate15To64,
                        employmentRateEvol,
                        unemploymentRate15To64,
                        unemploymentRateEvol,
                };

                writeLineCSV(bw, Arrays.asList(basicAttrs), false);
                writeLineCSV(bw, Arrays.asList(pricesAttrs), false);
                writeLineCSV(bw, Arrays.asList(localAttrs), true);
            }

            bw.close();
            Disp.shortMsgStar("WRITING TO .csv FILE FINISHED", true);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static String pipou(String value) {
        try {
            value = Misc.valueNormalise(value.replace(" habitants", ""));
        } catch (NullPointerException e) {
            value = null;
        }
        return value;
    }


    private static void writeLineCSV(BufferedWriter bw, List<String> attrsToWrite, boolean endOfLine) {
        try
        {
            int attr_index = 0;
            for (String attr : attrsToWrite)
            {
                if (attr != null) bw.write(attr);
                else bw.write(NO_DATA);

                attr_index ++;
                if (attr_index < attrsToWrite.size()) {
                    bw.write(",");
                } else if (! endOfLine) {
                    bw.write(",");
                }
            }
            if (endOfLine) bw.newLine();

        } catch (IOException e) { Disp.exc(e); }
    }

}
