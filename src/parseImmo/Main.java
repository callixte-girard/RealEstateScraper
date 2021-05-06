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

import static myJavaClasses.Misc.clean;


public class Main {
    private static final String projectName = "RealEstateScraper" ;
    // urls
    private static final String url_main = "https://www.meilleursagents.com" ;
    private static final String url_sub = "/prix-immobilier" ;
    // folder names
    private static final String save_folder = "data_saved/";
    private static final String output_folder = "data_output/";
    // paths
    private static final String root_path = "/Users/c/Documents/Saved Data/";
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
    public static String csvSeparator = ";"; // because dots are replaced by commas
    // NOTE: by default, those two parameters must be set to false (to scrape and export only after everything is finished)
    public static boolean skipVpnInit = true; // says if VPN must be handled or just ignored (if only needs to export already scraped data, for example)
    public static boolean shortcutMode = false; // scrapes only 20 first cities and then saves immediately


    public static void main_(String[] args) throws Exception
    {
        String test = "...";
        test = test.replace(".","o");
        Disp.anyType(test);
        // test passed ! .replace(String target) replace ALL occurences.

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

        ReadWriteFile.createFolderIfNotExists(save_path);
        ReadWriteFile.createFolderIfNotExists(output_path);
        SaveManager.setSavePath(save_path);

        if (!skipVpnInit) {
            HandleVPN.initAllRegions();
            HandleVPN.displayCurrentRegionAndIP();
        }

        // restart infinitely until everything is scraped
//        try {
            ArrayList<City> allCities = scrapeAllFrenchCities();
            // finally, write cities as .csv to be exported to Excel
            writeCitiesAsCSV(filename_cities_list + extension_csv , allCities , true);

        /*} catch (Exception | UncheckedIOException e1) {
            Disp.exc("Exception level 1 : [ v" + e1 + " ]");
//            Disp.exc(e.getCause() + " | " + e.getMessage());
            e1.printStackTrace();
            Disp.star();

            // now let's start again and again and again ... FOREVAH ;)
            *//*try {
//                main(args);
            } catch (Exception | UncheckedIOException e2) {
//                main(args);

                Disp.exc("Exception level 2 : [ " + e2 + " ]");
//                Disp.exc(e1.getCause() + " | " + e1.getMessage());
                e2.printStackTrace();
                Disp.star();
            }*//*
        }*/

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
//            try {
                Document main_page = ParseHtml.fetchHtmlAsDocumentFromUrl(url_main + url_sub);
                Disp.anyType(url_main + url_sub);
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
//            } catch (Exception e) {
//                Disp.exc("Can't download departments list containing urls to cities... Program can't start.");
//                e.printStackTrace();
//            }

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

        if (cities != null)  // which means : if save file exists
        {
            int actual_progress = cities.size();
            int total_cities = urls_cities.size();
            int remaining_cities = total_cities - actual_progress;
            Disp.anyType(">>> " + actual_progress + " / " + total_cities + " cities are already parsed.");
            Disp.anyType(">>> Will now parse " + remaining_cities + " remaining cities.");
        }
        else {
            cities = new ArrayList<>();
            Disp.anyType(">>> Now scraping all cities from all departments.");
        }

        Disp.htag(); Disp.htag(); Disp.htag();
        int nbRetries_thisSess = 0;
        int nbDone = 0;
//        int nbIPChanges = 0;
        boolean needsSave = false;

        // the loop ;)
        for (int index_city = 0 ; index_city < urls_cities.size() ; index_city ++)
        {
            String url_city = urls_cities.get(index_city);
            String url_to_parse = url_main + url_city; // assemble partial url with main part

            if ( !City.exists(url_to_parse, cities) ) { // try to download it until it's done.

                try {
                    City city = ParseCity.parse(url_to_parse);
//                    Disp.anyType(city);
                    Disp.anyType(
                            ">>> City n°" + (index_city+1) + " : [ "
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
//                        IP.getCurrent().setTimesUsed(IP.getCurrent().getTimesUsed() + 1);
                        IP.getCurrent().setBlocked(true);
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
                        IP.handleChange(); // includes marking the IP as saturated and try again until it's good
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

            if (shortcutMode && index_city > 20) break;

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
        Disp.anyType("Full export path is : ", save_to);

        try {
            BufferedWriter bw = ReadWriteFile.outputWriter(save_to);

            // I. make header first
            if (with_headers) {
                List<String> headers = new ArrayList<>();
                String[] basicHeaders = {
                        // main infos
                        "URL",
                        City.label_dptNumber,
                        City.label_name,
                        // prices
                        Prices.label_pricesMeanBuyFlat,
                        Prices.label_pricesMeanRentFlat,
                        Prices.label_pricesMeanBuyHouse,
                        // trends
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
                ReadWriteFile.writeLineCSV(bw, headers, csvSeparator, true);
            }

            // II. then writes data cell by cell
            for (City city : citiesToWrite)
            {
//                Disp.anyTypeThenLine(city);

                // II.A. basic infos
                String cityUrl = city.getUrl();
                String cityName = city.getName();
                String cityDpt = city.getPostalCodeAsDptNumber();
                String[] basicAttrs = { cityUrl, cityDpt, cityName };

                // define clean process methods
                // 1) just remove text (numbers are already integers)
                Process cleanPopulation = value -> value.replace(" habitants", "");
                Process cleanHomes = value -> value.replace(" logements", "");
                // 2) convert floating numbers and amounts
                Process cleanAmount = value -> Misc.cleanAmount(value, "€", true, true);
                // 3) convert floating numbers and amounts + remove text
                Process cleanAge = value -> cleanAmount.forExcel(value.replace(" ans", ""));
                Process cleanPopulationByArea = value -> cleanAmount.forExcel(value.replace(" hab. / km²", ""));
                Process cleanArea = value -> cleanAmount.forExcel(value.replace(" km²", ""));
                Process cleanToPureNumber = value -> cleanAmount.forExcel(Misc.removeNonNumericalCharsFromString(value));
                Process cleanPoints = value -> cleanAmount.forExcel(value.replace(" pt.", ""));

                // II.B. prices
                // II.B.a. mean prices for buy & rent
                String pricesMeanBuyAppt = Misc.clean(city.getMeanPrice(false, true), cleanAmount);
                String pricesMeanRentAppt = Misc.clean(city.getMeanPrice(true, true), cleanAmount);
                String pricesMeanBuyHouse = Misc.clean(city.getMeanPrice(false, false), cleanAmount); // NOTE: we'll just ignore houses for now and limit ourselves to apartments
                String[] pricesAttrs = { pricesMeanBuyAppt, pricesMeanRentAppt, pricesMeanBuyHouse, };
                // II.B.b. prices trends for last years
                String pricesTrends1y = Misc.clean(city.getTrends(1), cleanAmount);
                String pricesTrends2y = Misc.clean(city.getTrends(2), cleanAmount);
                String pricesTrends5y = Misc.clean(city.getTrends(5), cleanAmount);
                String[] trendsAttrs = { pricesTrends1y, pricesTrends2y, pricesTrends5y };

                // II.C. local infos
                // II.C.b. homes
                String population = Misc.clean(city.getLocalPop().get(ExtractLocalInfos.label01_population), cleanPopulation);
                String rateDemograhicGrowth = Misc.clean(city.getLocalPop().get(ExtractLocalInfos.label02_rateDemographicGrowth), cleanAmount);
                String medianAge = Misc.clean(city.getLocalPop().get(ExtractLocalInfos.label03_medianAge), cleanAge);
                String lessThan25yo = Misc.clean(city.getLocalPop().get(ExtractLocalInfos.label04_lessThan25yo), cleanAge);
                String moreThan25yo = Misc.clean(city.getLocalPop().get(ExtractLocalInfos.label05_moreThan25yo), cleanAge);
                String popDensity = Misc.clean(city.getLocalPop().get(ExtractLocalInfos.label06_popDensity), cleanPopulationByArea);
                String area = Misc.clean(city.getLocalPop().get(ExtractLocalInfos.label07_area), cleanArea);
                String[] popAttrs = {
                        population, rateDemograhicGrowth,
                        medianAge, lessThan25yo, moreThan25yo,
                        popDensity, area,
                };
                // II.C.b. homes
                String totalNbHomes = Misc.clean(city.getLocalHomes().get(ExtractLocalInfos.label08_totalNbHomes), cleanHomes);
                String rateMainHomes = clean(city.getLocalHomes().get(ExtractLocalInfos.label09_rateMainHomes), cleanAmount);
                String rateSecondaryHomes = clean(city.getLocalHomes().get(ExtractLocalInfos.label10_rateSecondaryHomes), cleanAmount);
                String rateVacantHomes = clean(city.getLocalHomes().get(ExtractLocalInfos.label11_rateVacantHomes), cleanAmount);
                String rateSocialHomes = clean(city.getLocalHomes().get(ExtractLocalInfos.label12_rateSocialHomes), cleanAmount);
                String rateMainHomeOwner = clean(city.getLocalHomes().get(ExtractLocalInfos.label13_rateMainHomeOwner), cleanAmount);
                String rateMainHomeTenant = clean(city.getLocalHomes().get(ExtractLocalInfos.label14_rateMainHomeTenant), cleanAmount);
                String rateMainHome1Room = clean(city.getLocalHomes().get(ExtractLocalInfos.label15_rateMainHome1Room), cleanAmount);
                String rateMainHome2Rooms = clean(city.getLocalHomes().get(ExtractLocalInfos.label16_rateMainHome2Rooms), cleanAmount);
                String rateMainHome3Rooms = clean(city.getLocalHomes().get(ExtractLocalInfos.label17_rateMainHome3Rooms), cleanAmount);
                String rateMainHome4Rooms = clean(city.getLocalHomes().get(ExtractLocalInfos.label18_rateMainHome4Rooms), cleanAmount);
                String rateMainHome5RoomsMore = clean(city.getLocalHomes().get(ExtractLocalInfos.label19_rateMainHome5RoomsMore), cleanAmount);
                String[] homesAttrs = {
                        totalNbHomes, rateMainHomes, rateSecondaryHomes, rateVacantHomes,
                        rateSocialHomes, rateMainHomeOwner, rateMainHomeTenant,
                        rateMainHome1Room, rateMainHome2Rooms, rateMainHome3Rooms, rateMainHome4Rooms, rateMainHome5RoomsMore,
                };
                // II.C.c. revenue / employment
                String medianAnnualRevenue = Misc.clean(city.getLocalRevenueEmpl().get(ExtractLocalInfos.label20_medianAnnualRevenue), cleanToPureNumber);
                String employmentRate15To64 = clean(city.getLocalRevenueEmpl().get(ExtractLocalInfos.label21_employmentRate15To64), cleanAmount);
                String employmentRateEvol = Misc.clean(city.getLocalRevenueEmpl().get(ExtractLocalInfos.label22_employmentRateEvol), cleanPoints);
                String unemploymentRate15To64 = clean(city.getLocalRevenueEmpl().get(ExtractLocalInfos.label23_unemploymentRate15To64), cleanAmount);
                String unemploymentRateEvol = Misc.clean(city.getLocalRevenueEmpl().get(ExtractLocalInfos.label24_unemploymentRateEvol), cleanPoints);
                String[] revenueEmploymentAttrs = {
                        medianAnnualRevenue,
                        employmentRate15To64, employmentRateEvol,
                        unemploymentRate15To64, unemploymentRateEvol,
                };

                // III. put everything together
                // 1) basic : always present
                ReadWriteFile.writeLineCSV(bw, Arrays.asList(basicAttrs), csvSeparator,false);
                // 2) prices & trends
                ReadWriteFile.writeLineCSV(bw, Arrays.asList(pricesAttrs), csvSeparator,false);
                ReadWriteFile.writeLineCSV(bw, Arrays.asList(trendsAttrs), csvSeparator,false);
                // 3) local infos
                ReadWriteFile.writeLineCSV(bw, Arrays.asList(popAttrs), csvSeparator,false);
                ReadWriteFile.writeLineCSV(bw, Arrays.asList(homesAttrs), csvSeparator,false);
                ReadWriteFile.writeLineCSV(bw, Arrays.asList(revenueEmploymentAttrs), csvSeparator,true);
            }
            bw.close();
            Disp.shortMsgStar("WRITING TO .csv FILE FINISHED", true);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
