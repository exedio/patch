#!'groovy'

def projectName = env.JOB_NAME.substring(0, env.JOB_NAME.indexOf("/")) // depends on name and location of multibranch pipeline in jenkins
def jdk = 'openjdk-11'
def idea = '2023.1'
def ideaSHA256 = 'e6fe45c9df8e763ee3278444b5fb1003910c436752e83221e0303a62c5e81eaa'
def isRelease = env.BRANCH_NAME=="master"
def dockerNamePrefix = env.JOB_NAME.replace("/", "-").replace(" ", "_") + "-" + env.BUILD_NUMBER
def dockerDate = new Date().format("yyyyMMdd")
def ant = 'java -jar lib/ant/ant-launcher.jar -noinput'

properties([
		gitLabConnection(env.GITLAB_CONNECTION),
		buildDiscarder(logRotator(
				numToKeepStr         : isRelease ? '1000' : '30',
				artifactNumToKeepStr : isRelease ?  '100' :  '2'
		))
])

tryCompleted = false
try
{
	def parallelBranches = [:]

	//noinspection GroovyAssignabilityCheck
	parallelBranches["Main"] =
	{
		//noinspection GroovyAssignabilityCheck
		nodeCheckoutAndDelete
		{
			scmResult ->
			def buildTag = makeBuildTag(scmResult)

			def dockerName = dockerNamePrefix + "-Main"
			def mainImage = docker.build(
					'exedio-jenkins:' + dockerName + '-' + dockerDate,
					'--build-arg JDK=' + jdk + ' ' +
					'--build-arg JENKINS_OWNER=' + env.JENKINS_OWNER + ' ' +
					'conf/main')
			mainImage.inside(
					"--name '" + dockerName + "' " +
					"--cap-drop all " +
					"--security-opt no-new-privileges " +
					"--hostname mydockerhostname " + // needed for InetAddress.getLocalHost()
					"--add-host mydockerhostname:127.0.0.1 " + // needed for InetAddress.getLocalHost()
					"--network none")
			{
				shSilent ant + " clean jenkins" +
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
					skipPublishingChecks: true,
			)
			junit(
					allowEmptyResults: false,
					testResults: 'build/testresults/*.xml',
					skipPublishingChecks: true
			)
			if(isRelease || env.BRANCH_NAME.contains("archiveSuccessArtifacts"))
				archiveArtifacts fingerprint: true, artifacts: 'build/success/*'
			plot(
					csvFileName: 'plots.csv',
					exclZero: false,
					keepRecords: false,
					group: 'Sizes',
					title: 'exedio-cope-patch.jar',
					numBuilds: '150',
					style: 'line',
					useDescr: false,
					propertiesSeries: [
						[ file: 'build/exedio-cope-patch.jar-plot.properties',     label: 'exedio-cope-patch.jar' ],
						[ file: 'build/exedio-cope-patch-src.zip-plot.properties', label: 'exedio-cope-patch-src.zip' ],
						[ file: 'build/exedio-cope-patch-console.jar-plot.properties', label: 'exedio-cope-patch-console.jar' ],
					],
			)
		}
	}

	//noinspection GroovyAssignabilityCheck
	parallelBranches["Idea"] =
	{
		//noinspection GroovyAssignabilityCheck
		nodeCheckoutAndDelete
		{
			recordIssues(
					failOnError: true,
					enabledForFailure: true,
					ignoreFailedBuilds: false,
					qualityGates: [[threshold: 1, type: 'TOTAL_HIGH', unstable: true]],
					tools: [
							taskScanner(
									excludePattern:
											'.git/**,lib/**,' +
											'**/*.jar,**/*.zip,**/*.tgz,**/*.jpg,**/*.gif,**/*.png,**/*.tif,**/*.webp,**/*.pdf,**/*.eot,**/*.ttf,**/*.woff,**/*.woff2,**/keystore', // binary file types
									highTags: 'FIX' + 'ME', // causes build to become unstable, concatenation prevents matching this line
									normalTags: 'TODO', // does not cause build to become unstable
									ignoreCase: true),
					],
			)

			def dockerName = dockerNamePrefix + "-Idea"
			docker.
				build(
					'exedio-jenkins:' + dockerName + '-' + dockerDate,
					'--build-arg JDK=' + jdk + ' ' +
					'--build-arg IDEA=' + idea + ' ' +
					'--build-arg IDEA_SHA256=' + ideaSHA256 + ' ' +
					'conf/idea').
				inside(
					"--name '" + dockerName + "' " +
					"--cap-drop all " +
					"--security-opt no-new-privileges " +
					"--network none")
				{
					shSilent ant + " src"
					shSilent "/opt/idea/bin/inspect.sh " + env.WORKSPACE + " 'Project Default' idea-inspection-output"
				}
			archiveArtifacts 'idea-inspection-output/**'
			// replace project dir to prevent UnsupportedOperationException - will not be exposed in artifacts
			shSilent "find idea-inspection-output -name '*.xml' | xargs --no-run-if-empty sed --in-place -- 's=\\\$PROJECT_DIR\\\$="+env.WORKSPACE+"=g'"
			recordIssues(
					failOnError: true,
					enabledForFailure: true,
					ignoreFailedBuilds: false,
					qualityGates: [[threshold: 1, type: 'TOTAL', unstable: true]],
					tools: [
						ideaInspection(pattern: 'idea-inspection-output/**'),
					],
			)
		}
	}

	//noinspection GroovyAssignabilityCheck
	parallelBranches["Ivy"] =
	{
		def cache = 'jenkins-build-survivor-' + projectName + "-Ivy"
		//noinspection GroovyAssignabilityCheck
		lockNodeCheckoutAndDelete(cache)
		{
			def dockerName = dockerNamePrefix + "-Ivy"
			def mainImage = docker.build(
					'exedio-jenkins:' + dockerName + '-' + dockerDate,
					'--build-arg JDK=' + jdk + ' ' +
					'--build-arg JENKINS_OWNER=' + env.JENKINS_OWNER + ' ' +
					'conf/main')
			mainImage.inside(
					"--name '" + dockerName + "' " +
					"--cap-drop all " +
					"--security-opt no-new-privileges " +
					"--mount type=volume,src=" + cache + ",target=/var/jenkins-build-survivor")
			{
				shSilent ant +
					" -buildfile ivy" +
					" -Divy.user.home=/var/jenkins-build-survivor"
			}
			archiveArtifacts 'ivy/artifacts/report/**'

			def gitStatus = sh (script: "git status --porcelain --untracked-files=normal", returnStdout: true).trim()
			if(gitStatus!='')
			{
				error 'FAILURE because fetching dependencies produces git diff:\n' + gitStatus
			}
			// There should be an assertIvyExtends for each <conf name="abc" extends="def" /> in ivy/ivy.xml.
			assertIvyExtends("console", "runtime")
			assertIvyExtends("test", "runtime")
			assertIvyExtends("test", "console")
			assertIvyExtends("example", "runtime")
			assertIvyExtends("example", "console")
			assertIvyExtends("ide", "runtime")
			assertIvyExtends("ide", "test")
		}
	}

	parallel parallelBranches

	tryCompleted = true
}
finally
{
	if(!tryCompleted)
		currentBuild.result = 'FAILURE'

	// workaround for Mailer plugin: set result status explicitly to SUCCESS if empty, otherwise no mail will be triggered if a build gets successful after a previous unsuccessful build
	if(currentBuild.result==null)
		currentBuild.result = 'SUCCESS'
	node('email')
	{
		step([$class: 'Mailer',
				recipients: emailextrecipients([isRelease ? culprits() : developers(), requestor()]),
				notifyEveryUnstableBuild: true])
	}
	updateGitlabCommitStatus state: currentBuild.resultIsBetterOrEqualTo("SUCCESS") ? "success" : "failed" // https://docs.gitlab.com/ee/api/commits.html#post-the-build-status-to-a-commit
}

def lockNodeCheckoutAndDelete(resource, Closure body)
{
	lock(resource)
	{
		nodeCheckoutAndDelete(body)
	}
}

def nodeCheckoutAndDelete(Closure body)
{
	node('GitCloneExedio && docker')
	{
		env.JENKINS_OWNER =
			sh (script: "id --user",  returnStdout: true).trim() + ':' +
			sh (script: "id --group", returnStdout: true).trim()
		try
		{
			deleteDir()
			def scmResult = checkout scm
			updateGitlabCommitStatus state: 'running'

			body.call(scmResult)
		}
		finally
		{
			deleteDir()
		}
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

def assertIvyExtends(extendingConf, parentConf)
{
	def status = sh (
			script: "LC_ALL=C" +
					" diff --recursive lib/" + parentConf + " lib/" + extendingConf +
					" | grep --invert-match '^Only in lib/" + extendingConf + ": '" +
					" > ivy/artifacts/ivyExtends" + extendingConf + ".txt",
			returnStatus: true)
	if(status!=0 && status!=1) // https://www.man7.org/linux/man-pages/man1/diff.1.html
	{
		error 'FAILURE because diff had trouble'
	}
	def result = readFile "ivy/artifacts/ivyExtends" + extendingConf + ".txt"
	if(result!='')
	{
		error 'FAILURE because ivy conf "' + extendingConf + '" does not just add jar-files to "' + parentConf + '":\n' + result
	}
}

def shSilent(script)
{
	try
	{
		sh script
	}
	catch(Exception ignored)
	{
		currentBuild.result = 'FAILURE'
	}
}
