# Barck
Born Again Reports Client for Kony

Allows you to download custom reports from Kony Mobile Fabric.

## Build

Barck is a Groovy + Gradle project. All it's dependencies are packaged with it, so it's stand-alone. Build it by running:

On Unix-like platforms such as Linux and Mac OS:

    ./gradlew clean uberJar

On Windows, using the gradlew.bat batch file

    gradlew clean uberJar
    
## 1. Running Barck

To use Barck run:

    barck [options]
    barck <command> [parameters]

### Options

    -h --help Get help on how to use barck.
    -v --version Display the version of barck.
    -c --credits Print author credits.

### Commands

    get-filters: Lists all the predefined filters for a given custom report.
    get-report: Downloads a given custom report.
    
## 2. Querying a Report's Filters

List the predefined filters for a given report. Basically, to get a clue on how to call the report.

    barck get-filters -t account -u user -p password -m mode -r report
    
### Options
    
    -h,--help                     Get help on how to use get-report.
    -m,--mode <arg>               Whether the report is private or shared.
                                  Possible values are 'private' and 'shared'.
    -n,--noisy                    Print every single little thing.
    -p,--password <arg>           Password for the Kony user.
    -r,--report <arg>             Name of the custom report.
    -t,--account <arg>            9 digit id of the Kony Cloud account -e.g.
                                  100054321.
    -u,--user <arg>               Kony user required for authentication, for
                               	  e.g. jimi.hendrix@rocks.com.

### Example

    barck get-report -t 123456789 -u jimmi.hendrix@rocks.com -p 'my@passwd123!5' -m shared -r 'My Report'

## 3. Downloading a Report

Provide input values to the predifined filters of your report and download the resulting data.

    barck get-report -t account -u user -p password -m mode -r report [-F filter1=foo[,filter2=bar]]

### Options
    
    -F,--filters <filter=value>   Input parameters for the report's
                                  predefined filters.
    -h,--help                     Get help on how to use get-report.
    -m,--mode <arg>               Whether the report is private or shared.
                                  Possible values are 'private' and 'shared'.
    -n,--noisy                    Print every single little thing.
    -p,--password <arg>           Password for the Kony user.
    -r,--report <arg>             Name of the custom report.
    -t,--account <arg>            9 digit id of the Kony Cloud account -e.g.
                                  100054321.
    -u,--user <arg>               Kony user required for authentication, for
                               	  e.g. jimi.hendrix@rocks.com.
                                  
### Example

    barck get-report -t 123456789 -u jimmi.hendrix@rocks.com -p 'my@passwd123!5' -m shared -r 'My Report' -F appmane_1=MyApp,eventtimestamp_utc_1="2017-03-01 00:00:00”

### Notes:

* You will need to enclose your password in single quotes `'` if it contains a bang/exclamation `!` character.
* If the name of the custom report contains blank spaces you will need to either enclose it in single quotes `'` or escape those spaces with back-slashes `\`.
* The name of the custom report is the one it's displayed with at `https://manage.kony.com` when you go to `Reports>Custom Reports>` under the `Custom Reports` tab.
* If you're using Barck on a Unix-like operating system -such as Linux or Mac OS- you'll have to previx `barck` with `./`

    
