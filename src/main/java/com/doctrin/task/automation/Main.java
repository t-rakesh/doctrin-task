package com.doctrin.task.automation;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main {

    private static Properties loadProperties(String propertiesFilename) {
        Properties prop = new Properties();
        ClassLoader loader = com.doctrin.task.covid.Main.class.getClassLoader();
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
        WebDriver webDriver = null;
        String propertiesFilename = "config.properties";
        Properties prop = loadProperties(propertiesFilename);

        if (args.length > 1) {
            switch(args[0]) {
                case "firefox":
                    System.setProperty("webdriver.gecko.driver", System.getProperty("user.dir") + prop.getProperty("webdriver.path.firefox"));
                    webDriver = new FirefoxDriver();
                    break;
                case "chrome":
                    System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + prop.getProperty("webdriver.path.chrome"));
                    webDriver = new ChromeDriver();
                    break;
                default:
                    System.out.println("Invalid browser name. Supported browsers - firefox, chrome.");
                    break;
            }
        } else {
            System.out.println("Insufficient number of argument. Usage Main <browserName> <outputFileName>");
        }

        System.out.println("Arguments: " + args[0] + ", " + args[1]);
        try {
            ArrayList<Employee> employees = new ArrayList<>();
            webDriver.get(prop.getProperty("automation.base.url"));
            List<WebElement> people = webDriver.findElements(By.cssSelector("a[href*='/people/']"));
            for (WebElement employee: people) {
                String name = employee.findElement(By.cssSelector("span[class*='name']")).getText();
                String title = employee.findElement(By.cssSelector("span[class*='title']")).getText();
                employees.add(new Employee(name, title));
            }

            System.out.println("Name and title of people present on the page.");
            ArrayList<String> lines = new ArrayList<>();
            for (Employee employee: employees) {
                System.out.println(employee.toString());
                lines.add(employee.toString());
            }

            File outputFile = Util.createOutputFile(args[1]);
            System.out.println("Output file created: " + outputFile.getAbsolutePath());
            Util.writeToFile(args[1], lines);
        } finally {
            webDriver.quit();
        }
    }

}
