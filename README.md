# Barck
Born Again Reports Client for Kony

Allows you to download custom reports from Kony Mobile Fabric.

## Build

Barck is a Gradle project. All it's dependencies are packaged with it, so it's stand-alone. Build it by running:

    gradle clean uberJar
    
## Run

To use Barck run:

    barck [options]
    barck <command> [parameters]

### OPTIONS
    -h --help Get help on how to use barck.
    -v --version Display the version of barck.
    -c --credits Print author credits.

### COMMANDS
    get-filters: Lists all the predefined filters for a given custom report.
    get-report: Downloads a given custom report.
    
## Get Filters

List the predefined filters for a given report. Basically, to get a clue on how to call the report.

    barck get-filters -t account -u user -p password -m mode -r report
    
**Options**
    
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

**EXAMPLE**

    barck get-report -t 123456789 -u jimmi.hendrix@rocks.com -p ******* -m shared -r MyReport

## Download a Report

Provide input values to the predifined filters of your report and download the resulting data.

    barck get-report -t account -u user -p password -m mode -r report [-F filter1=foo[,filter2=bar]]

**Options**
    
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
                                  
**EXAMPLE**

    barck get-report -t 123456789 -u jimmi.hendrix@rocks.com -p ******* -m shared -r MyReport -F appmane_1=MyApp,eventtimestamp_utc_1="2017-03-01 00:00:00”
