package com.mig82.barck
import org.apache.commons.cli.Option
import groovy.time.TimeCategory 
import groovy.time.TimeDuration

def version = '1.0'
def msg = [:]
msg.credits = '''
**************************************************
*       Born Again Reports Client for Kony       *
**************************************************
Welcome!

	Written by: Miguelangel Fernandez

	barck is written in Groovy.
'''

msg.usage = '''\
barck [options]
barck <command> [parameters]
'''

msg.opt = [:]
msg.opt.h = 'Get help on how to use barck.'
msg.opt.v = 'Display the version of barck.'
msg.opt.c = 'Print author credits.'

msg.unknownCmd = '''\
For the availabe commands, run:

	barck -h
'''

msg.help = """
NAME
		barck - Born Again Reports Client for Kony.

DESCRIPTION
		A command line utility to download Kony APM custom reports.

USAGE
		${msg.usage}

OPTIONS
		-h --help ${msg.opt.h}
		-v --version ${msg.opt.v}
		-c --credits ${msg.opt.c}

COMMANDS
		get-filters: Lists all the predefined filters for a given custom report.
		get-report: Downloads a given custom report.
"""

def filtersMsg = [:]
filtersMsg.usage = '\tbarck get-filters -t account -u user -p password -m mode -r report'

filtersMsg.help = '''
EXAMPLE
	barck get-report -t 123456789 -u jimmi.hendrix@rocks.com -p ******* -m shared -r MyReport
'''

def reportMsg = [:]
reportMsg.usage = '\tbarck get-report -t account -u user -p password -m mode -r report [-F filter1=foo[,filter2=bar]]'

reportMsg.help = '''
EXAMPLE
	barck get-report -t 123456789 -u jimmi.hendrix@rocks.com -p ******* -m shared -r MyReport -F appmane_1=MyApp,eventtimestamp_utc_1="2017-03-01 00:00:00” 
'''

def mainArgs = args.size()>0?args[0..0]:[] //The first arg is always the command.
def commandArgs = args.size()>1?args[1..args.size()-1]:[] //Args from the second on are the args of the command.

def mainCli = new CliBuilder(
	usage: msg.usage
)

//If the user is asking for help, print it out and stop execution.
if(!args || args[0] in ['-h', '--help']){
	println '\nusage: ' + mainCli.usage
	println msg.help
	return
}
//If the user is asking for the version, print it and stop execution.
else if(args[0] in ['-v', '--version']){
	println "\nbarck version: $version\n"
	return
}
//If the user is asking for the credits, print them and stop execution.
else if(args[0] in ['-c', '--credits']){
	println msg.credits
	return
}

mainCli.with{
	h( longOpt: 'help', required: false, msg.opt.h )
	v( longOpt: 'version', required: false, msg.opt.v )
	c( longOpt: 'credits', required: false, msg.opt.c )
}

def mainOpt = mainCli.parse(mainArgs)
def commands = mainOpt.arguments()

if(!commands || commands.size()!=1){
	println '\nusage: ' + mainCli.usage
	return
}
else{
	switch(commands[0]) {
		case 'get-filters':
			execCmd(commandArgs, filtersMsg, commands[0])
			break
		case 'get-report':
			execCmd(commandArgs, reportMsg, commands[0])
			break
		default:
			println "Unknown command '${commands[0]}'\n"
			println msg.unknownCmd
			break
	}
	return
}

def execCmd(args, msg, cmd){
	//println "\nAttempting to $cmd\n"

	def cli = new CliBuilder(
		usage: msg.usage
	)

	//If the user is asking for help on the command, print the help out and stop execution.
	if(args[0] in ['-h', '--help']){
		println '\nusage: ' + cli.usage
		println msg.help
		return
	}

	cli.with{
		h( longOpt: 'help', required: false, "Get help on how to use $cmd." )
		n( longOpt: 'noisy', required: false, 'Print every single little thing.' )
		t( longOpt: 'account', required: true, args: 1, '9 digit id of the Kony Cloud account -e.g. 100054321.' )
		r( longOpt: 'report', required: true, args: 1, 'Name of the custom report.' )
		u( longOpt: 'user', required: true, args: 1, 'Kony user required for authentication, for e.g. jimi.hendrix@rocks.com.' )
		p( longOpt: 'password', required: true, args: 1, 'Password for the Kony user.' )
		m( longOpt: 'mode', required: true, args: 1, "Whether the report is private or shared. Possible values are 'private' and 'shared'." )
	}

	if(cmd == 'get-report'){
		cli.F(
			longOpt: 'filters',
			required: false,
			args:  Option.UNLIMITED_VALUES,
			argName:'filter=value',
			valueSeparator:',',
			"Input parameters for the report's predefined filters."
		)
	}

	def opt = cli.parse(args)
	if (!opt) {
		println "You've typed no options.\n"
		return
	}
	else{
		def noisy = opt.n
		def account = opt.t
		def report = opt.r.replaceAll(' ', '_')
		def user = opt.u
		def password = opt.p
		def mode = opt.m
		def filters = opt.Fs   // append 's' to end of opt.F to get all filters

		if(noisy){Echo.setNoisy()}

		if (cmd == 'get-filters'){
			getFilters(account, report, user, password, mode)
		}
		else if(cmd == 'get-report'){
			getReport(account, report, user, password, mode, filters)
		}
		
	}

}

def getFilters(account, report, user, password, mode){
	def mfcli = new MobileFabricClient()
	def filters = mfcli.getFilters(account, report, user, password, mode)
	
	if(filters && filters.size() > 0){
		println "\nFilters for ${mode} report ${report}:\n"
		if(filters){filters.each { println "\t${it}" }}
		println ''
	}
	else{
		println "\nNo filters found for ${mode} report ${report}.\n"
	}
}

def getReport(account, report, user, password, mode, filters){
	def mfcli = new MobileFabricClient()
	def start = new Date()
	mfcli.getReport(account, report, user, password, mode, filters)
	def stop = new Date()
	TimeDuration te = TimeCategory.minus( stop, start )
	println "Done. Time elapsed ${te}"

}

def dateToMiliseconds(String format, String date){
	Date d = Date.parse(format, date)
	println d.getTime()
}





