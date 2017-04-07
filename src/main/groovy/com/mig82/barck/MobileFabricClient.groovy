import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.Method.POST
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.TEXT

import groovy.util.slurpersupport.GPathResult

class MobileFabricClient{

	def authBaseUrl = 'https://accounts.auth.konycloud.com'
	def apiBaseUrl = 'https://api.kony.com/api/v1_1'

	def httpRequest(method, reqUri, reqContentType, reqHeaders, urlQuery, reqBody){
		def http = new HTTPBuilder(reqUri)
		def result
		http.request(method){

			if(reqContentType){ requestContentType = reqContentType }
			if(reqHeaders){ headers = reqHeaders }
			if(urlQuery){ uri.query = urlQuery }
			if(reqBody){ body = reqBody }
			
			response.success = { resp, json ->
				assert resp.status == 200
				result = json
			}
			response.'401' = { resp ->
				Echo.say '401 Unauthorised.'
			}

			response.'403' = { resp ->
				Echo.say 'Forbidden'
			}

			response.'404' = { resp ->
				Echo.say 'Not found'
			}

			response.failure = { resp ->
				Echo.say "Unexpected failure: ${resp.statusLine}"
			}
		}
		return result
	}

	def httpDownloadReport(method, reqUri, reqContentType, reqHeaders, urlQuery, reqBody, reportFileName){
		def http = new HTTPBuilder(reqUri)
		http.request(method){

			if(reqContentType){ requestContentType = reqContentType }
			if(reqHeaders){ headers = reqHeaders }
			if(urlQuery){ uri.query = urlQuery }
			if(reqBody){ body = reqBody }
			
			def reportFile = new File(reportFileName)

			response.success = { resp, reader ->
				assert resp.status == 200
				reportFile << reader
			}
			response.'401' = { resp ->
				Echo.say '401 Unauthorised.'
			}

			response.'403' = { resp ->
				Echo.say 'Forbidden'
			}

			response.'404' = { resp ->
				Echo.say 'Not found'
			}

			response.'413' = { resp ->
				Echo.say "he request can't be fulfilled because the payload is too large."
			}

			response.failure = { resp ->
				Echo.say "Unexpected failure: ${resp.statusLine}"
			}
		}
	}

	def logIntoKony(user, password){

		Echo.say '\nLogging into Kony Cloud'
		def konyLoginUrl = "${authBaseUrl}/login"
		def json = httpRequest(
			POST,
			konyLoginUrl,
			JSON,
			null,
			null,
			[ userid: user, password: password ]
		)
		def token = json.claims_token.value
		Echo.say "\tclaims_token: \n${token}"

		return token
	}

	def getUserInfo(token, account){
		
		Echo.say '\nFetching user info'
		def userInfoUrl = "${apiBaseUrl}/whoami"
		def json = httpRequest(
			GET,
			userInfoUrl,
			JSON,
			[ 'X-Kony-Authorization': token ],
			null,
			null
		)
		def userInfo = [:]
		userInfo.userGuid = json.user_guid
		userInfo.accountGuid = json.accounts.findAll{ it.account_id == account }[0].account_guid
		Echo.say "\tuserInfo: ${userInfo}"

		return userInfo
	}

	def logIntoJasper(token, accountGuid, userGuid){

		Echo.say '\nLogging into reports'
		def jasperLoginUrl = "${apiBaseUrl}/accounts/${accountGuid}/reports/login"
		def json = httpRequest(
			GET,
			jasperLoginUrl,
			JSON,
			[ 'X-Kony-Authorization': token ],
			[ user_guid: userGuid ],
			null
		)
		def jasperInfo = [:]
		jasperInfo.jasperSession = json.jasper_session
		jasperInfo.awselbSession = json.awselb_session
		Echo.say "\tjasperInfo: ${jasperInfo}"

		return jasperInfo
	}

	def getReportUri(account, accountGuid, mode, report){
		if(mode == 'shared'){
			return "/${mode}_/${report}"
		}
		else if (mode == 'private'){
			return "/prvt_${accountGuid}_${account}/${report}"
		}
	}

	def requestFilters(token, account, accountGuid, jasperSession, awselbSession, mode, report){
		
		Echo.say "\nQuerying filters for ${mode} report ${report}"
		def getFiltersUrl = "${apiBaseUrl}/accounts/${accountGuid}/reports/inputControls"
		def resourceUri = getReportUri(account, accountGuid, mode, report)
		Echo.say "\tresourceUri '${resourceUri}'"

		def json = httpRequest(
			GET,
			getFiltersUrl,
			JSON,
			[ 'X-Kony-Authorization': token ],
			[
				jasperSession: jasperSession,
				awselbSession: awselbSession,
				resourceUri: resourceUri
			],
			null
		)
		def filters = []
		if(json && json.size() > 0 && json[0] != null){	 //When nothing is found the service returns [null]
			//For each filter found we extract only what we need.

			Echo.say "inputControls: ${json}"

			filters = json.collect{[
				'name' : it.id,
				'type': it.type,
				'desc': it.label,
				'required': it.mandatory,
				'read-only': it.readOnly,
				'default': it.state?.options?.findAll{it.selected}.collect{it.value}, //The currently selected option.
				'format': it.validationRules.collect{ //Collect the formats for each validatioin rule.
					it.values().collect{ //Collect all the rules.
						it.format //Return the format for each rule.
					}
				} 
			]}
		}
		Echo.say "\tfilters: ${filters}"

		return filters
	}

	def requestReport(token, account, accountGuid, jasperSession, awselbSession, mode, report, filters){
		Echo.say "\nRequesting ${mode} report ${report}"
		def getReportUrl = "${apiBaseUrl}/accounts/${accountGuid}/reports/view"
		def resourceUri = getReportUri(account, accountGuid, mode, report)
		Echo.say "\tresourceUri '${resourceUri}'"

		//Transform the filters from an array of strings [foo=1, bar=2] to a map [foo:1, bar:2] 
		def filtersMap = filters.collectEntries{ it -> 
			def t = it.split('=')
			[ (t[0]): t[1] ]
		}
		Echo.say "\tfiltersMap: $filtersMap"

		//Now compose a string concatenation of the keys in the map 'foo,bar'
		def filterKeyList = filtersMap.keySet().join(',')
		Echo.say "\tfilterKeyList: $filterKeyList"

		def query =[
			jasperSession: jasperSession,
			awselbSession: awselbSession,
			resourceUri: resourceUri,
			file_extension: 'csv',
			type: 'csv',
			export: true,
			filters: filterKeyList,
		] << filtersMap
		Echo.say "\tquery: $query"
		
		def reportFileName = "${account}-${report}-${filters}.csv"
		def text = httpDownloadReport(
			GET,
			getReportUrl,
			TEXT,
			[ 'X-Kony-Authorization': token ],
			query,
			null,
			reportFileName
		)
	}

	def getFilters(account, report, user, password, mode){
		def token = logIntoKony(user, password)
		def filters

		if(!token){
			Echo.say "Failed to log in with user $user"
		}
		else{
			def userInfo = getUserInfo(token, account)
			def jasperInfo = logIntoJasper(token, userInfo.accountGuid, userInfo.userGuid)
			filters = requestFilters(token, account, userInfo.accountGuid, jasperInfo.jasperSession, jasperInfo.awselbSession, mode, report)
		}
		return filters
	}

	def getReport(account, report, user, password, mode, filters){
		def token = logIntoKony(user, password)
		def data

		if(!token){
			Echo.say "Failed to log in with user $user"
		}
		else{
			def userInfo = getUserInfo(token, account)
			def jasperInfo = logIntoJasper(token, userInfo.accountGuid, userInfo.userGuid)
			data = requestReport(token, account, userInfo.accountGuid, jasperInfo.jasperSession, jasperInfo.awselbSession, mode, report, filters)
		}
		return data
	}
}


