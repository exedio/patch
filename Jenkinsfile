
timestamps
{
	def isRelease = env.BRANCH_NAME.toString().equals("master")

	properties([
			buildDiscarder(logRotator(
					numToKeepStr         : isRelease ? '1000' : '30',
					artifactNumToKeepStr : isRelease ? '1000' :  '2'
			))
	])

	//noinspection GroovyAssignabilityCheck
	node('OpenJdk18Debian9')
	{
		try
		{
			abortable
			{
				echo("Delete working dir before build")
				deleteDir()

				def scmResult = checkout scm
				computeGitTree(scmResult)

				env.BUILD_TIMESTAMP = new Date().format("yyyy-MM-dd_HH-mm-ss")
				env.JAVA_HOME = "${tool 'openjdk 1.8 debian9'}"
				env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"

				sh 'echo' +
						' scmResult=' + scmResult +
						' BUILD_TIMESTAMP -${BUILD_TIMESTAMP}-' +
						' BRANCH_NAME -${BRANCH_NAME}-' +
						' BUILD_NUMBER -${BUILD_NUMBER}-' +
						' BUILD_ID -${BUILD_ID}-' +
						' isRelease=' + isRelease

				sh "ant/bin/ant clean jenkins" +
						' "-Dbuild.revision=${BUILD_NUMBER}"' +
						' "-Dbuild.tag=git ${BRANCH_NAME} ' + scmResult.GIT_COMMIT + ' ' + scmResult.GIT_TREE + ' jenkins ${BUILD_NUMBER} ${BUILD_TIMESTAMP}"' +
						' -Dbuild.status=' + (isRelease?'release':'integration') +
						' -Dinstrument.verify=true' +
						' -Ddisable-ansi-colors=true' +
						' -Dfindbugs.output=xml'

				recordIssues(
						enabledForFailure: true,
						ignoreFailedBuilds: false,
						qualityGates: [[threshold: 1, type: 'TOTAL', unstable: true]],
						tools: [
							java(),
							spotBugs(pattern: 'build/findbugs.xml', useRankAsPriority: true),
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
			def to = emailextrecipients([culprits(), requestor()])
			//TODO details
			step([$class: 'Mailer',
					recipients: to,
					attachLog: true,
					notifyEveryUnstableBuild: true])

			if('SUCCESS'.equals(currentBuild.result) ||
				'UNSTABLE'.equals(currentBuild.result))
			{
				echo("Delete working dir after " + currentBuild.result)
				deleteDir()
			}
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

def computeGitTree(scmResult)
{
	sh "git cat-file -p " + scmResult.GIT_COMMIT + " | grep '^tree ' | sed -e 's/^tree //' > .git/jenkins-head-tree"
	scmResult.GIT_TREE = readFile('.git/jenkins-head-tree').trim()
}
