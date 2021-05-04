package parseCity;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;

public class ExtractLocalInfos {
//    private final static boolean preprocessData = true;

    // might change on the website...
    // !!! IMPORTANT TO KEEP THIS VALUE UPDATED
    // !!! MUST TAKE FORMAT : "(YYYY-YYYY)"
    private final static String datesInterval = "(2006-2011)";
    // might probably not change on the website, but always check when big update.
    // ----- LABELS -----
    // 1) population
    public final static String label01_population = "Population";
    public final static String label02_rateDemographicGrowth = "Croissance démographique" + " " + datesInterval;
    public final static String label03_medianAge = "Age médian";
    public final static String label04_lessThan25yo = "Part des moins de 25 ans";
    public final static String label05_moreThan25yo = "Part des plus de 25 ans";
    public final static String label06_popDensity = "Densité de la population (nombre d'habitants au km²)";
    public final static String label07_area = "Superficie (en km²)";
    // 2) homes
    public final static String label08_totalNbHomes = "Nombre total de logements";
    public final static String label09_rateMainHomes = "Part des résidences principales";
    public final static String label10_rateSecondaryHomes = "Part des résidences secondaires";
    public final static String label11_rateVacantHomes = "Part des logements vacants";
    public final static String label12_rateSocialHomes = "Part des logements sociaux / HLM";
    public final static String label13_rateMainHomeOwner = "Part des ménages propriétaires de leur résidence principale";
    public final static String label14_rateMainHomeTenant = "Part des ménages locataires de leur résidence principale";
    public final static String label15_rateMainHome1Room = "Part des résidences principales 1 pièce";
    public final static String label16_rateMainHome2Rooms = "Part des résidences principales 2 pièces";
    public final static String label17_rateMainHome3Rooms = "Part des résidences principales 3 pièces";
    public final static String label18_rateMainHome4Rooms = "Part des résidences principales 4 pièces";
    public final static String label19_rateMainHome5RoomsMore = "Part des résidences principales 5 pièces ou plus";
    // 3) revenue & employment
    public final static String label20_medianAnnualRevenue = "Revenu annuel médian par ménage";
    public final static String label21_employmentRate15To64 = "Taux d'activité des 15 à 64 ans";
    public final static String label22_employmentRateEvol = "Evolution du taux d'activité" + " " + datesInterval;
    public final static String label23_unemploymentRate15To64 = "Taux de chômage des 15 à 64 ans";
    public final static String label24_unemploymentRateEvol = "Evolution du taux de chômage" + " " + datesInterval;


    public static LinkedHashMap<String, String> all(Document doc)
    {
        Element mainLocalContainer = doc
                .getElementById("local_stats");
//        Disp.anyTypeThenLine(mainLocalContainer);

        Elements localContainerSections = mainLocalContainer
                .getElementsByClass("accordion-panel container--row accordion-panel--price");
//        Disp.anyTypeThenLine(localContainerSections);

        LinkedHashMap<String, String> localInfos = new LinkedHashMap<>();
        for (Element el : localContainerSections) {
//            Disp.anyTypeThenLine(el);

            Element section = el.getElementsByTag("tbody").first();
//            Disp.anyTypeThenLine(section);

            Elements pairs = section.getElementsByTag("tr");

            // @TO-DO finish ;)
            for (Element info : pairs) {
//                Disp.anyTypeThenLine(info);

                Elements infoCells = info.getElementsByTag("td");
//                Disp.anyTypeThenLine(infoCells);

                String infoName_raw = infoCells.get(0).text().trim();
                String infoValue_raw = infoCells.get(1).text().trim();
//                Disp.anyTypeThenLine(infoName_raw + " | " + infoValue_raw);
//
                infoValue_raw = infoValue_raw.replaceAll("," , "."); // quick fix for , and .
                localInfos.put(infoName_raw, infoValue_raw);
            }
        }
        return localInfos;
    }


    private static LinkedHashMap<String, String> extractSelectedLabelsFromLocalInfos(
            LinkedHashMap<String, String> allLocalInfos,
            String[] labels
    ) {
        LinkedHashMap<String, String> infos = new LinkedHashMap<>();
        for (int i=0 ; i<labels.length ; i++) {
            String label = labels[i];
            String value = allLocalInfos.get(label);

            // pre-process data // NOTE: outdated, already present somewhere else
            boolean preprocessData = false;
            if (preprocessData) {
                switch (label) {
                    // 1) population
                    case label01_population:
                        value = value.replace(" habitants", "");
                        break;
                    case label03_medianAge:
                        value = value.replace(" ans", "");
                        break;
                    case label22_employmentRateEvol:
                    case label24_unemploymentRateEvol:
                        value = value.replace(" pt.", "");
                        break;
                    case label06_popDensity:
                        value = value.replace(" hab. / km²", "");
                        break;
                    // 2) homes
                    case label08_totalNbHomes:
                        value = value.replace(" logements²", "");
                        break;
                    // 3) revenue & employment
                    case label20_medianAnnualRevenue:
                        value = value.replace(" €", "");
                        break;
                }
                value = value.replaceAll(",", "."); // quick fix for , and .
            }

            infos.put(label, value);
        }
        return infos;
    }

    protected static LinkedHashMap<String, String> localPop(LinkedHashMap<String, String> allLocalInfos)
    {
        String[] labels_localPop = {
                label01_population,
                label02_rateDemographicGrowth,
                label03_medianAge,
                label04_lessThan25yo,
                label05_moreThan25yo,
                label06_popDensity,
                label07_area,
        };
        LinkedHashMap<String, String> localPop = extractSelectedLabelsFromLocalInfos(allLocalInfos, labels_localPop);
        return localPop;
    }

    protected static LinkedHashMap<String, String> localHomes(LinkedHashMap<String, String> allLocalInfos)
    {
        String[] labels_localHomes = {
                label08_totalNbHomes,
                label09_rateMainHomes,
                label10_rateSecondaryHomes,
                label11_rateVacantHomes,
                label12_rateSocialHomes,
                label13_rateMainHomeOwner,
                label14_rateMainHomeTenant,
                label15_rateMainHome1Room,
                label16_rateMainHome2Rooms,
                label17_rateMainHome3Rooms,
                label18_rateMainHome4Rooms,
                label19_rateMainHome5RoomsMore,
        };
        LinkedHashMap<String, String> localHomes = extractSelectedLabelsFromLocalInfos(allLocalInfos, labels_localHomes);
        return localHomes;
    }

    protected static LinkedHashMap<String, String> localRevenueEmployment(LinkedHashMap<String, String> allLocalInfos)
    {
        String[] labels_localRevenueEmpl = {
                label20_medianAnnualRevenue,
                label21_employmentRate15To64,
                label22_employmentRateEvol,
                label23_unemploymentRate15To64,
                label24_unemploymentRateEvol,
        };
        LinkedHashMap<String, String> localRevenueEmpl = extractSelectedLabelsFromLocalInfos(allLocalInfos, labels_localRevenueEmpl);
        return localRevenueEmpl;
    }


}
