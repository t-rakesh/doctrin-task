package com.doctrin.task.covid;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;

public class ApiClient {
    Properties prop;
    CloseableHttpClient httpClient;

    final String HTTP_REQ_RETRY = "httpclient.request.retry";
    final String HTTP_REQ_RETRY_TIMEOUT = "httpclient.request.retry.timeout";
    final String API_HOST = "api.host";
    final String PATH_GET_COUNTRIES = "api.path.get.countries";
    final String PATH_GET_STATUS = "api.path.get.country.status";

    public ApiClient(Properties properties) throws IOException {
        prop = properties;
        httpClient = HttpClientBuilder.create()
                .setServiceUnavailableRetryStrategy(new ServiceUnavailableRetryStrategy() {
                    @Override
                    public boolean retryRequest(HttpResponse response, int exeCount, HttpContext context) {
                        int httpStatus = response.getStatusLine().getStatusCode();
                        // 429 - received sometimes from covid19 api.
                        boolean httpStatusCondition =
                                (httpStatus == HttpStatus.SC_SERVICE_UNAVAILABLE) || (httpStatus == 429);
                        return (exeCount <= Integer.parseInt(prop.getProperty(HTTP_REQ_RETRY)) && httpStatusCondition);
                    }

                    @Override
                    public long getRetryInterval() {
                        return Long.parseLong(prop.getProperty(HTTP_REQ_RETRY_TIMEOUT));
                    }
                })
                .build();
    }

    private String execute(HttpUriRequest request) throws IOException {
        HttpResponse response = httpClient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        if (HttpStatus.SC_OK == statusCode) {
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, StandardCharsets.UTF_8);
        } else {
            throw new IOException("Request failed: HTTP status code " + statusCode);
        }
    }

    public ArrayList<Country> getCountries() throws IOException {
        URI uri = URI.create(prop.getProperty(API_HOST) + prop.getProperty(PATH_GET_COUNTRIES));
        String responseBody = execute(new HttpGet(uri));
        Type listType = new TypeToken<ArrayList<Country>>() {}.getType();
        ArrayList<Country> countries = new Gson().fromJson(responseBody, listType);
        return countries;
    }

    public ArrayList<Status> getStatus(String slug) throws IOException {
        URI uri = URI.create(prop.getProperty(API_HOST) + String.format(prop.getProperty(PATH_GET_STATUS), slug));
        String responseBody = execute(new HttpGet(uri));
        Type listType = new TypeToken<ArrayList<Status>>() {}.getType();
        ArrayList<Status> statuses = new Gson().fromJson(responseBody, listType);
        return statuses;
    }

    public ArrayList<CountryStatus> calculateDailyStatus(ArrayList<Status> statuses) {
        ArrayList<CountryStatus> countryStatuses = new ArrayList<>();
        int size = statuses.size();
        for (int i = 0; i < size -1; i++) {
            Status prevStatus = statuses.get(i);
            Status nextStatus = statuses.get(i + 1);
            if (i == 0) {
                CountryStatus firstCountryStatus = new CountryStatus(
                        Integer.parseInt(prevStatus.Confirmed),
                        Integer.parseInt(prevStatus.Deaths),
                        Integer.parseInt(prevStatus.Confirmed),
                        Integer.parseInt(prevStatus.Deaths),
                        prevStatus.Date
                );
                countryStatuses.add(firstCountryStatus);
            }
            int newConfirmed = Integer.parseInt(nextStatus.Confirmed) - Integer.parseInt(prevStatus.Confirmed);
            int newDeaths = Integer.parseInt(nextStatus.Deaths) - Integer.parseInt(prevStatus.Deaths);
            CountryStatus countryStatus = new CountryStatus(
                    Integer.parseInt(nextStatus.Confirmed),
                    Integer.parseInt(nextStatus.Deaths),
                    newConfirmed,
                    newDeaths,
                    nextStatus.Date
            );
            countryStatuses.add(countryStatus);
        }
        return countryStatuses;
    }

    public CountryStatus findHighestConfirmedStatus(ArrayList<CountryStatus> statuses) {
        Collections.sort(statuses, Comparator.comparingInt(prev -> prev.newConfirmed));
        CountryStatus highestConfirmed = statuses.get(statuses.size() - 1);
        return highestConfirmed;
    }


    public CountryStatus findHighestDeathsStatus(ArrayList<CountryStatus> statuses) {
        Collections.sort(statuses, Comparator.comparingInt(prev -> prev.newDeaths));
        CountryStatus highestDeaths = statuses.get(statuses.size() - 1);
        return highestDeaths;
    }
}

class Country { String Slug; }
class Status { String Confirmed; String Deaths; String Date;}
class CountryStatus {
    int confirmed;
    int deaths;
    int newConfirmed;
    int newDeaths;
    String date;

    public CountryStatus(int confirmed, int deaths, int newConfirmed, int newDeaths, String date) {
        this.confirmed = confirmed;
        this.deaths = deaths;
        this.newConfirmed = newConfirmed;
        this.newDeaths = newDeaths;
        this.date = date;
    }

    @Override
    public String toString() {
        return "CountryStatus{" +
                "confirmed=" + confirmed +
                ", deaths=" + deaths +
                ", newConfirmed=" + newConfirmed +
                ", newDeaths=" + newDeaths +
                ", date='" + date + '\'' +
                '}';
    }
}
