package com.doctrin.task.covid;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

public class Main {

    private static Properties loadProperties(String propertiesFilename) {
        Properties prop = new Properties();
        ClassLoader loader = Main.class.getClassLoader();
        try {
            InputStream stream = loader.getResourceAsStream(propertiesFilename);
            if (stream == null) {
                throw new FileNotFoundException();
            }
            prop.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

    public static void main(String[] args) throws IOException {
        String propertiesFilename = "config.properties";
        Properties prop = loadProperties(propertiesFilename);
        ApiClient apiClient = new ApiClient(prop);
        System.out.println("API supported country list (slug):");
        ArrayList<Country> countries = apiClient.getCountries();
        countries.forEach(country -> System.out.print(country.Slug.concat(" ")));
        System.out.println("\nEnter county: ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String slug = reader.readLine();
        ArrayList<Status> statuses = apiClient.getStatus(slug);
        ArrayList<CountryStatus> countryStatuses = apiClient.calculateDailyStatus(statuses);
        CountryStatus highestConfirmedStatus = apiClient.findHighestConfirmedStatus(countryStatuses);
        System.out.println("Highest confirmed: " + highestConfirmedStatus.newConfirmed + ", Date: " + highestConfirmedStatus.date);
        CountryStatus highestDeathsStatus = apiClient.findHighestDeathsStatus(countryStatuses);
        System.out.println("Highest deaths: " + highestDeathsStatus.newDeaths + ", Date: " + highestDeathsStatus.date);
    }
}
