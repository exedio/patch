
timestamps
{
	def jdk = 'openjdk-8'
	def isRelease = env.BRANCH_NAME.toString().equals("master")

	properties([
			buildDiscarder(logRotator(
					numToKeepStr         : isRelease ? '1000' : '30',
					artifactNumToKeepStr : isRelease ? '1000' :  '2'
			))
	])

	//noinspection GroovyAssignabilityCheck
	node('docker')
	{
		try
		{
			abortable
			{
				echo("Delete working dir before build")
				deleteDir()

				def buildTag = makeBuildTag(checkout(scm))

				def dockerName = env.JOB_NAME.replace("/", "-").replace(" ", "_") + "-" + env.BUILD_NUMBER
				def dockerDate = new Date().format("yyyyMMdd")
				def mainImage = docker.build(
						'exedio-jenkins:' + dockerName + '-' + dockerDate,
						'--build-arg JDK=' + jdk + ' ' +
						'conf/main')
				mainImage.inside(
						"--name '" + dockerName + "' " +
						"--cap-drop all " +
						"--security-opt no-new-privileges " +
						"--hostname mydockerhostname " + // needed for InetAddress.getLocalHost()
						"--add-host mydockerhostname:127.0.0.1 " + // needed for InetAddress.getLocalHost()
						"--network none")
				{
					sh "java -jar lib/ant/ant-launcher.jar -noinput clean jenkins" +
							' "-Dbuild.revision=${BUILD_NUMBER}"' +
							' "-Dbuild.tag=' + buildTag + '"' +
							' -Dbuild.status=' + (isRelease?'release':'integration') +
							' -Dinstrument.verify=true' +
							' -Ddisable-ansi-colors=true'
				}

				recordIssues(
						failOnError: true,
						enabledForFailure: true,
						ignoreFailedBuilds: false,
						qualityGates: [[threshold: 1, type: 'TOTAL', unstable: true]],
						tools: [
							java(),
						],
				)
				archiveArtifacts 'build/success/*'
				plot(
						csvFileName: 'plots.csv',
						exclZero: false,
						keepRecords: false,
						group: 'Sizes',
						title: 'exedio-cope-patch.jar',
						numBuilds: '1000',
						style: 'line',
						useDescr: false,
						propertiesSeries: [
							[ file: 'build/exedio-cope-patch.jar-plot.properties',     label: 'exedio-cope-patch.jar' ],
							[ file: 'build/exedio-cope-patch-src.zip-plot.properties', label: 'exedio-cope-patch-src.zip' ],
						],
				)
			}
		}
		catch(Exception e)
		{
			//todo handle script returned exit code 143
			throw e
		}
		finally
		{
			// because junit failure aborts ant
			junit(
					allowEmptyResults: false,
					testResults: 'build/testresults/*.xml',
			)
			def to = emailextrecipients([isRelease ? culprits() : developers(), requestor()])
			//TODO details
			step([$class: 'Mailer',
					recipients: to,
					attachLog: true,
					notifyEveryUnstableBuild: true])

			echo("Delete working dir after build")
			deleteDir()
		}
	}
}

def abortable(Closure body)
{
	try
	{
		body.call()
	}
	catch(hudson.AbortException e)
	{
		if(e.getMessage().contains("exit code 143"))
			return
		throw e
	}
}

def makeBuildTag(scmResult)
{
	return 'build ' +
			env.BRANCH_NAME + ' ' +
			env.BUILD_NUMBER + ' ' +
			new Date().format("yyyy-MM-dd") + ' ' +
			scmResult.GIT_COMMIT + ' ' +
			sh (script: "git cat-file -p " + scmResult.GIT_COMMIT + " | grep '^tree ' | sed -e 's/^tree //'", returnStdout: true).trim()
}
